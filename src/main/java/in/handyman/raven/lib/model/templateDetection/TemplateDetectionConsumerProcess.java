package in.handyman.raven.lib.model.templateDetection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.audit.ExecutionStatus;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.TemplateDetectionAction;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TemplateDetectionConsumerProcess implements CoproProcessor.ConsumerProcess<TemplateDetectionInputTable, TemplateDetectionOutputTable> {

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final TemplateDetectionAction aAction;
    private final  String TEMPLATE_DETECTION = "TEMPLATE_DETECTION";
    private final int timeOut;

    public TemplateDetectionConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, TemplateDetectionAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.aAction = aAction;
        this.timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder()
                .connectTimeout(this.timeOut, TimeUnit.MINUTES)

                .writeTimeout(this.timeOut, TimeUnit.MINUTES)
                .readTimeout(this.timeOut, TimeUnit.MINUTES)
                .build();
    }


    @Override
    public List<TemplateDetectionOutputTable> process(URL endpoint, TemplateDetectionInputTable entity) throws Exception {

        List<TemplateDetectionOutputTable> outputObjectList = new ArrayList<>();
        List<String> attributes = entity.getQuestions();
        String inputFilePath = entity.getFilePath();
        Long rootPipelineId = entity.getRootPipelineId();
        Long actionId = action.getActionId();

        ObjectMapper objectMapper = new ObjectMapper();


        //payload
        TemplateDetectionData templateDetectionDataInput = new TemplateDetectionData();
        templateDetectionDataInput.setAttributes(attributes);
        templateDetectionDataInput.setPaperType("Printed");
        templateDetectionDataInput.setInputFilePath(inputFilePath);
        templateDetectionDataInput.setRootPipelineId(rootPipelineId);
        templateDetectionDataInput.setActionId(actionId);
        templateDetectionDataInput.setProcess(TEMPLATE_DETECTION);
        String jsonInputRequest = objectMapper.writeValueAsString(templateDetectionDataInput);



        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("ERNIE START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));
        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);


        log.info(aMarker, "Input request object for template detection filePath is {} and questions size {}", inputFilePath, attributes.size());

        Request request = new Request.Builder().url(endpoint)
                .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();

        log.info(aMarker, "Request object  endpoint {}", endpoint);

        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        Integer groupId = entity.getGroupId();

        try (Response response = httpclient.newCall(request).execute()) {
            Timestamp createdOn = Timestamp.valueOf(LocalDateTime.now());
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                TemplateDetectionResponse templateDetectionResponse = objectMapper.readValue(responseBody, TemplateDetectionResponse.class);

                if (templateDetectionResponse.getOutputs() != null && !templateDetectionResponse.getOutputs().isEmpty()) {
                    templateDetectionResponse.getOutputs().forEach(output -> {
                        output.getData().forEach(templateDetectionData -> {
                            try {
                                TemplateDetectionDataItem templateDetectionDataItem=objectMapper.readValue(templateDetectionData,TemplateDetectionDataItem.class);
                                templateDetectionDataItem.getAttributes().forEach(attribute -> {
                                    String bboxStr = String.valueOf(attribute.getBboxes());
                                    String question = attribute.getQuestion();
                                    Float scores = attribute.getScores();
                                    String predictedAttributionValue = attribute.getPredictedAttributionValue();

                                    outputObjectList.add(
                                            TemplateDetectionOutputTable.builder()
                                                    .processId(processId)
                                                    .tenantId(tenantId)
                                                    .templateId(templateId)
                                                    .predictedAttributionValue(predictedAttributionValue)
                                                    .question(question)
                                                    .scores(scores)
                                                    .bboxes(bboxStr)
                                                    .imageWidth(templateDetectionDataItem.getImageWidth())
                                                    .imageDPI(templateDetectionDataItem.getImageDPI())
                                                    .extractedImageUnit(templateDetectionDataItem.getExtractedImageUnit())
                                                    .rootPipelineId(rootPipelineId)
                                                    .groupId(groupId)
                                                    .originId(originId)
                                                    .paperNo(paperNo)
                                                    .createdOn(createdOn)
                                                    .status(ExecutionStatus.COMPLETED.toString())
                                                    .stage(TEMPLATE_DETECTION)
                                                    .modelName(templateDetectionResponse.getModelName())
                                                    .modelVersion(templateDetectionResponse.getModelVersion())
                                                    .message("Template detection completed for group_id " + groupId + " and origin_id " + originId)
                                                    .processedFilePath(inputFilePath)
                                                    .build()
                                    );
                                });
                            } catch (JsonProcessingException e) {
                                outputObjectList.add(
                                        TemplateDetectionOutputTable.builder()
                                                .processId(processId)
                                                .tenantId(tenantId)
                                                .templateId(templateId)
                                                .rootPipelineId(rootPipelineId)
                                                .groupId(groupId)
                                                .originId(originId)
                                                .paperNo(paperNo)
                                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                                .status(ExecutionStatus.FAILED.toString())
                                                .stage(TEMPLATE_DETECTION)
                                                .message("Template detection response processing failed for group_id " + groupId + " and origin_id " + originId + " and Exception ")
                                                .processedFilePath(inputFilePath)
                                                .build()
                                );
                                log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
                                HandymanException handymanException = new HandymanException(e);
                                HandymanException.insertException("Template detection consumer failed for batch/group " + groupId,
                                        handymanException,
                                        this.action);
                                log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
                            }

                        });
                    });
                } else {
                    outputObjectList.add(
                            TemplateDetectionOutputTable.builder()
                                    .processId(processId)
                                    .tenantId(tenantId)
                                    .templateId(templateId)
                                    .rootPipelineId(rootPipelineId)
                                    .groupId(groupId)
                                    .originId(originId)
                                    .paperNo(paperNo)
                                    .createdOn(createdOn)
                                    .status(ExecutionStatus.COMPLETED.toString())
                                    .stage(TEMPLATE_DETECTION)
                                    .message("Template detection completed and response is empty for group_id " + groupId + " and origin_id " + originId)
                                    .processedFilePath(inputFilePath)
                                    .build()
                    );
                }
            } else {
                outputObjectList.add(
                        TemplateDetectionOutputTable.builder()
                                .processId(processId)
                                .tenantId(tenantId)
                                .templateId(templateId)
                                .rootPipelineId(rootPipelineId)
                                .groupId(groupId)
                                .originId(originId)
                                .paperNo(paperNo)
                                .createdOn(createdOn)
                                .status(ExecutionStatus.FAILED.toString())
                                .stage(TEMPLATE_DETECTION)
                                .message("Template detection failed for group_id " + groupId + " and origin_id " + originId + " and Exception")
                                .processedFilePath(inputFilePath)
                                .build()
                );
            }
        } catch (Exception e) {
            outputObjectList.add(
                    TemplateDetectionOutputTable.builder()
                            .processId(processId)
                            .tenantId(tenantId)
                            .templateId(templateId)
                            .rootPipelineId(rootPipelineId)
                            .groupId(groupId)
                            .originId(originId)
                            .paperNo(paperNo)
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .status(ExecutionStatus.FAILED.toString())
                            .stage(TEMPLATE_DETECTION)
                            .message("Template detection failed for group_id " + groupId + " and origin_id " + originId + " and Exception ")
                            .processedFilePath(inputFilePath)
                            .build()
            );
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Template detection consumer failed for batch/group " + groupId,
                    handymanException,
                    this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }


        return outputObjectList;
    }



}
