package in.handyman.raven.lib.model.hwDectection;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.lib.HwDetectionAction.httpClientTimeout;

public class HwClassificationConsumerProcess implements CoproProcessor.ConsumerProcess<HwClassificationInputTable,HwClassificationOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private final String STAGE="PAPER_CLASSIFICATION";
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    public final ActionExecutionAudit action;

    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
            .writeTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
            .readTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
            .build();

    public HwClassificationConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }

    @Override
    public List<HwClassificationOutputTable> process(URL endpoint, HwClassificationInputTable entity) throws Exception {

        List<HwClassificationOutputTable> parentObj = new ArrayList<>();
        String entityFilePath = entity.getFilePath();
        Long actionId=action.getActionId();
        final String PAPER_CLASSIFICATION_PROCESS="PAPER_CLASSIFICATION";
        long rootpipelineId= entity.getRootPipelineId();
        String modelPath = action.getModelPath();
        String outputDir = String.valueOf(entity.getOutputDir());
        ObjectMapper objectMapper = new ObjectMapper();

        //payload
        HwDetectionPayload HwDetectionpayload = new HwDetectionPayload();
        HwDetectionpayload.setRootPipelineId(Long.valueOf(rootpipelineId));
        HwDetectionpayload.setActionId(actionId);
        HwDetectionpayload.setProcess(PAPER_CLASSIFICATION_PROCESS);
        HwDetectionpayload.setInputFilePath(entityFilePath);
        HwDetectionpayload.setOutputDir(outputDir);
        HwDetectionpayload.setModelPath(modelPath);

        String jsonInputRequest = objectMapper.writeValueAsString(HwDetectionpayload);


        HwDetectionRequest requests = new HwDetectionRequest();
        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("PAPER CLASSIFIER START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest=new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        Request request = new Request.Builder().url(endpoint)
                .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();

        if(log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n coproUrl  {} ,inputFilePath : {} modelPath  {}  outputDir {} ", endpoint,entityFilePath,modelPath,outputDir);
        }
        String createdUserId = entity.getCreatedUserId();
        String lastUpdatedUserId = entity.getLastUpdatedUserId();
        Long tenantId = entity.getTenantId();
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();
        Integer groupId = entity.getGroupId();
        try (Response response = httpclient.newCall(request).execute()){
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                ObjectMapper objectMappers = new ObjectMapper();
                HwDetectionResponse hwDetectionResponse = objectMappers.readValue(responseBody, HwDetectionResponse.class);
                if (hwDetectionResponse.getOutputs() != null && !hwDetectionResponse.getOutputs().isEmpty()) {
                    hwDetectionResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(hwDetectionDataItem -> {
                            log.info("copro api response body {}", responseBody);
                            String documentStatus = null;
                            try {
                                documentStatus = Optional.ofNullable(mapper.readTree(responseBody).get("document_status")).map(JsonNode::asText).orElse(null);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                            Long score = null;
                            try {
                                score = Optional.ofNullable(mapper.readTree(responseBody).get("confidence_score")).map(JsonNode::asLong).orElse(null);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }

                            parentObj.add(HwClassificationOutputTable.builder().createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                                    .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                                    .tenantId(Optional.ofNullable(tenantId).map(String::valueOf).map(Long::valueOf).orElse(null))
                                    .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                    .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                                    .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                                    .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Long::parseLong).orElse(null))
                                    .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                                    .documentType(documentStatus)
                                    .confidenceScore(score)
                                    .status("COMPLETED")
                                    .stage(STAGE)
                                    .message("Paper Classification Finished")
                                    .groupId(entity.getGroupId())
                                    .rootPipelineId(entity.getRootPipelineId())
                                    .modelName(hwDetectionResponse.getModelName())
                                    .modelVersion(hwDetectionResponse.getModelVersion())
                                    .build());
                        });
                    });
                }
            }
            else {
                parentObj.add(HwClassificationOutputTable.builder()
                        .createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                        .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                        .tenantId(Optional.ofNullable(tenantId).map(String::valueOf).map(Long::valueOf).orElse(null))
                        .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                        .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                        .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                        .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Long::parseLong).orElse(null))
                        .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                        .status("FAILED")
                        .stage(STAGE)
                        .message(response.message())
                        .groupId(entity.getGroupId())
                        .rootPipelineId(entity.getRootPipelineId())
                        .build());
                log.info(aMarker, "The Exception occurred in paper classification response");
            }
        } catch (Exception e) {
            parentObj.add(HwClassificationOutputTable.builder()
                    .createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                    .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                    .tenantId(Optional.ofNullable(tenantId).map(String::valueOf).map(Long::valueOf).orElse(null))
                    .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                    .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                    .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                    .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Long::parseLong).orElse(null))
                    .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                    .status("FAILED")
                    .stage(STAGE)
                    .message(ExceptionUtil.toString(e))
                    .groupId(entity.getGroupId())
                    .rootPipelineId(entity.getRootPipelineId())
                    .build());
            log.error(aMarker, "The Exception occurred in paper classification request", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Paper classification (hw-detection) consumer failed for batch/group "+ groupId, handymanException, this.action);

        }
        return parentObj;
    }
}





