package in.handyman.raven.lib.model.templatedetection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.audit.ExecutionStatus;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.TemplateDetectionAction;
import in.handyman.raven.lib.model.templatedetection.copro.TemplateDetectionDataItemCopro;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TemplateDetectionConsumerProcess implements CoproProcessor.ConsumerProcess<TemplateDetectionInputTable, TemplateDetectionOutputTable> {

    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType mediaTypeJSON = MediaType
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
        templateDetectionDataInput.setOriginId(entity.getOriginId());
        templateDetectionDataInput.setPaperNo(entity.getPaperNo());
        templateDetectionDataInput.setProcessId(entity.getProcessId());
        templateDetectionDataInput.setGroupId(entity.getGroupId());
        templateDetectionDataInput.setTenantId(entity.getTenantId());
        String jsonInputRequest = objectMapper.writeValueAsString(templateDetectionDataInput);



        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("ARGON VQA START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));
        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);


        if (Objects.equals("false", tritonRequestActivator)) {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonInputRequest, mediaTypeJSON)).build();
            coproRequestBuider(entity, request, objectMapper ,outputObjectList);
        } else {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonRequest, mediaTypeJSON)).build();
            tritonRequestBuilder(entity, request, objectMapper, outputObjectList);
        }


        log.info(aMarker, "Input request object for template detection filePath is {} and questions size {}", inputFilePath, attributes.size());
        return outputObjectList;
    }

    private void coproRequestBuider(TemplateDetectionInputTable entity, Request request, ObjectMapper objectMapper, List<TemplateDetectionOutputTable> outputObjectList) throws IOException {
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
                extractedCoproOutputResponse(entity,  responseBody, outputObjectList, "", ",", objectMapper);

            } else {
                outputObjectList.add(
                        TemplateDetectionOutputTable.builder()
                                .processId(processId)
                                .tenantId(tenantId)
                                .templateId(templateId)
                                .rootPipelineId(entity.getRootPipelineId())
                                .groupId(groupId)
                                .originId(originId)
                                .paperNo(paperNo)
                                .createdOn(createdOn)
                                .status(ExecutionStatus.COMPLETED.toString())
                                .stage(TEMPLATE_DETECTION)
                                .message("Template detection completed and response is empty for group_id " + groupId + " and origin_id " + originId)
                                .processedFilePath(entity.getFilePath())
                                .batchId(entity.getBatchId())
                                .build()
                );
            }
        } catch (Exception e) {
            outputObjectList.add(
                    TemplateDetectionOutputTable.builder()
                            .processId(processId)
                            .tenantId(tenantId)
                            .templateId(templateId)
                            .rootPipelineId(entity.getRootPipelineId())
                            .groupId(groupId)
                            .originId(originId)
                            .paperNo(paperNo)
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .status(ExecutionStatus.FAILED.toString())
                            .stage(TEMPLATE_DETECTION)
                            .message("Template detection failed for group_id " + groupId + " and origin_id " + originId + " and Exception ")
                            .processedFilePath(entity.getFilePath())
                            .batchId(entity.getBatchId())
                            .build()
            );
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Template detection consumer failed for batch/group " + groupId,
                    handymanException,
                    this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));

        }

    }
    private void tritonRequestBuilder(TemplateDetectionInputTable entity, Request request, ObjectMapper objectMapper, List<TemplateDetectionOutputTable> outputObjectList) {
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
                            extractOutputDataRequest(entity,  templateDetectionData, outputObjectList, templateDetectionResponse.getModelName(), templateDetectionResponse.getModelVersion(), objectMapper);

                        });
                    });
                } else {
                    outputObjectList.add(
                            TemplateDetectionOutputTable.builder()
                                    .processId(processId)
                                    .tenantId(tenantId)
                                    .templateId(templateId)
                                    .rootPipelineId(entity.getRootPipelineId())
                                    .groupId(groupId)
                                    .originId(originId)
                                    .paperNo(paperNo)
                                    .createdOn(createdOn)
                                    .status(ExecutionStatus.COMPLETED.toString())
                                    .stage(TEMPLATE_DETECTION)
                                    .message("Template detection completed and response is empty for group_id " + groupId + " and origin_id " + originId)
                                    .processedFilePath(entity.getFilePath())
                                    .batchId(entity.getBatchId())
                                    .build()
                    );
                }
            } else {
                outputObjectList.add(
                        TemplateDetectionOutputTable.builder()
                                .processId(processId)
                                .tenantId(tenantId)
                                .templateId(templateId)
                                .rootPipelineId(entity.getRootPipelineId())
                                .groupId(groupId)
                                .originId(originId)
                                .paperNo(paperNo)
                                .createdOn(createdOn)
                                .status(ExecutionStatus.FAILED.toString())
                                .stage(TEMPLATE_DETECTION)
                                .message("Template detection failed for group_id " + groupId + " and origin_id " + originId + " and Exception")
                                .processedFilePath(entity.getFilePath())
                                .batchId(entity.getBatchId())
                                .build()
                );
            }
        } catch (Exception e) {
            outputObjectList.add(
                    TemplateDetectionOutputTable.builder()
                            .processId(processId)
                            .tenantId(tenantId)
                            .templateId(templateId)
                            .rootPipelineId(entity.getRootPipelineId())
                            .groupId(groupId)
                            .originId(originId)
                            .paperNo(paperNo)
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .status(ExecutionStatus.FAILED.toString())
                            .stage(TEMPLATE_DETECTION)
                            .message("Template detection failed for group_id " + groupId + " and origin_id " + originId + " and Exception ")
                            .processedFilePath(entity.getFilePath())
                            .batchId(entity.getBatchId())
                            .build()
            );
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Template detection consumer failed for batch/group " + groupId,
                    handymanException,
                    this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }
    }

    private void extractOutputDataRequest(TemplateDetectionInputTable entity, String templateDetectionData, List<TemplateDetectionOutputTable> outputObjectList, String modelName, String modelVersion, ObjectMapper objectMapper) {
        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        Integer groupId = entity.getGroupId();
        try {
            TemplateDetectionDataItem templateDetectionDataItem= objectMapper.readValue(templateDetectionData,TemplateDetectionDataItem.class);
            templateDetectionDataItem.getAttributes().forEach(attribute -> {
                String bboxStr = String.valueOf(attribute.getBboxes());
                String question = attribute.getQuestion();
                Float scores = attribute.getScores();
                String predictedAttributionValue = attribute.getPredictedAttributionValue();

                outputObjectList.add(
                        TemplateDetectionOutputTable.builder()
                                .processId(attribute.getProcessId())
                                .tenantId(attribute.getTenantId())
                                .templateId(templateId)
                                .predictedAttributionValue(predictedAttributionValue)
                                .question(question)
                                .scores(scores)
                                .bboxes(bboxStr)
                                .imageWidth(templateDetectionDataItem.getImageWidth())
                                .imageDPI(templateDetectionDataItem.getImageDPI())
                                .extractedImageUnit(templateDetectionDataItem.getExtractedImageUnit())
                                .rootPipelineId(entity.getRootPipelineId())
                                .groupId(attribute.getGroupId())
                                .originId(attribute.getOriginId())
                                .paperNo(attribute.getPaperNo())
                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                .status(ExecutionStatus.COMPLETED.toString())
                                .stage(TEMPLATE_DETECTION)
                                .modelName(modelName)
                                .modelVersion(modelVersion)
                                .message("Template detection completed for group_id " + groupId + " and origin_id " + originId)
                                .processedFilePath(entity.getFilePath())
                                .batchId(entity.getBatchId())
                                .build()
                );
            });
        } catch (JsonProcessingException e) {
            outputObjectList.add(
                    TemplateDetectionOutputTable.builder()
                            .processId(processId)
                            .tenantId(tenantId)
                            .templateId(templateId)
                            .rootPipelineId(entity.getRootPipelineId())
                            .groupId(groupId)
                            .originId(originId)
                            .paperNo(paperNo)
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .status(ExecutionStatus.FAILED.toString())
                            .stage(TEMPLATE_DETECTION)
                            .message("Template detection response processing failed for group_id " + groupId + " and origin_id " + originId + " and Exception ")
                            .processedFilePath(entity.getFilePath())
                            .batchId(entity.getBatchId())
                            .build()
            );
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Template detection consumer failed for batch/group " + groupId,
                    handymanException,
                    this.action);
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));


        }

    }
    private void extractedCoproOutputResponse(TemplateDetectionInputTable entity, String templateDetectionData, List<TemplateDetectionOutputTable> outputObjectList, String modelName, String modelVersion, ObjectMapper objectMapper) {
        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        Integer groupId = entity.getGroupId();
        try {
            TemplateDetectionDataItemCopro templateDetectionDataItemCopro = objectMapper.readValue(templateDetectionData, TemplateDetectionDataItemCopro.class);
            templateDetectionDataItemCopro.getAttributes().forEach(attribute -> {
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
                                .imageWidth(templateDetectionDataItemCopro.getImageWidth())
                                .imageDPI(templateDetectionDataItemCopro.getImageDPI())
                                .extractedImageUnit(templateDetectionDataItemCopro.getExtractedImageUnit())
                                .rootPipelineId(entity.getRootPipelineId())
                                .groupId(groupId)
                                .originId(originId)
                                .paperNo(paperNo)
                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                .status(ExecutionStatus.COMPLETED.toString())
                                .stage(TEMPLATE_DETECTION)
                                .modelName(modelName)
                                .modelVersion(modelVersion)
                                .message("Template detection completed for group_id " + groupId + " and origin_id " + originId)
                                .processedFilePath(entity.getFilePath())
                                .batchId(entity.getBatchId())
                                .build()
                );
            });
        } catch (JsonProcessingException e) {
            outputObjectList.add(
                    TemplateDetectionOutputTable.builder()
                            .processId(processId)
                            .tenantId(tenantId)
                            .templateId(templateId)
                            .rootPipelineId(entity.getRootPipelineId())
                            .groupId(groupId)
                            .originId(originId)
                            .paperNo(paperNo)
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .status(ExecutionStatus.FAILED.toString())
                            .stage(TEMPLATE_DETECTION)
                            .message("Template detection response processing failed for group_id " + groupId + " and origin_id " + originId + " and Exception ")
                            .processedFilePath(entity.getFilePath())
                            .batchId(entity.getBatchId())
                            .build()
            );
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Template detection consumer failed for batch/group " + groupId,
                    handymanException,
                    this.action);
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));


        }
    }


    }
