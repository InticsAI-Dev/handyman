package in.handyman.raven.lib.model.hwdectection;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.hwdectection.copro.HwDetectionDataItemCopro;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class HwClassificationConsumerProcess implements CoproProcessor.ConsumerProcess<HwClassificationInputTable, HwClassificationOutputTable> {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    private final Logger log;
    private final Marker aMarker;
    private final String outputDir;
    private final String STAGE = PipelineName.PAPER_CLASSIFICATION.getProcessName();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType mediaTypeJson = MediaType
            .parse("application/json; charset=utf-8");
    public final ActionExecutionAudit action;

    private final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.MINUTES)
            .writeTimeout(100, TimeUnit.MINUTES)
            .readTimeout(100, TimeUnit.MINUTES)
            .build();

    public HwClassificationConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, String outputDir) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.outputDir = outputDir;
    }

    @Override
    public List<HwClassificationOutputTable> process(URL endpoint, HwClassificationInputTable entity) throws Exception {

        List<HwClassificationOutputTable> parentObj = new ArrayList<>();
        String entityFilePath = entity.getFilePath();
        Long rootpipelineId = action.getRootPipelineId();
        Long actionId = action.getActionId();
        String filePath = String.valueOf(entity.getFilePath());
        ObjectMapper objectMapper = new ObjectMapper();

        //payload
        HwDetectionPayload hwDetectionPayload = new HwDetectionPayload();
        hwDetectionPayload.setRootPipelineId(rootpipelineId);
        hwDetectionPayload.setActionId(actionId);
        hwDetectionPayload.setProcess(STAGE);
        hwDetectionPayload.setInputFilePath(filePath);
        hwDetectionPayload.setOutputDir(this.outputDir);
        hwDetectionPayload.setGroupId(entity.getGroupId());
        hwDetectionPayload.setTenantId(entity.getTenantId());
        hwDetectionPayload.setOriginId(entity.getOriginId());
        hwDetectionPayload.setProcessId(entity.getProcessId());
        hwDetectionPayload.setPaperNo(entity.getPaperNo());

        String jsonInputRequest = objectMapper.writeValueAsString(hwDetectionPayload);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("PAPER CLASSIFIER START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);


        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);


        if (Objects.equals("false", tritonRequestActivator)) {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonInputRequest, mediaTypeJson)).build();
            coproRequestBuilder(entity, request, parentObj);
        } else {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonRequest, mediaTypeJson)).build();
            tritonRequestBuilder(entity, request, parentObj);
        }


        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n coproUrl  {} ,inputFilePath : {} outputDir {} ", endpoint, entityFilePath, outputDir);
        }

        return parentObj;
    }

    private void coproRequestBuilder(HwClassificationInputTable entity, Request request, List<HwClassificationOutputTable> parentObj) {
        String createdUserId = entity.getCreatedUserId();
        String lastUpdatedUserId = entity.getLastUpdatedUserId();
        Long tenantId = entity.getTenantId();
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();
        Integer groupId = entity.getGroupId();
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {

                String responseBody = Objects.requireNonNull(response.body()).string();

                extractedCoproOutputResponse(entity, responseBody, parentObj, "", "");
            } else {

                parentObj.add(HwClassificationOutputTable.builder()
                        .createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                        .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                        .tenantId(Optional.ofNullable(tenantId).map(String::valueOf).map(Long::valueOf).orElse(null))
                        .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                        .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                        .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                        .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Long::parseLong).orElse(null))
                        .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(STAGE)
                        .message(response.message())
                        .groupId(entity.getGroupId())
                        .rootPipelineId(entity.getRootPipelineId())
                        .batchId(entity.getBatchId())
                                .createdOn(entity.getCreatedOn())
                                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
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
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(STAGE)
                    .message(ExceptionUtil.toString(e))
                    .groupId(entity.getGroupId())
                    .batchId(entity.getBatchId())
                    .rootPipelineId(action.getRootPipelineId())
                    .createdOn(entity.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .build());
            log.error(aMarker, "The Exception occurred in paper classification request", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Paper classification (hw-detection) consumer failed for batch/group " + groupId, handymanException, this.action);

        }
    }


    private void tritonRequestBuilder(HwClassificationInputTable entity, Request request, List<HwClassificationOutputTable> parentObj) {
        String createdUserId = entity.getCreatedUserId();
        String lastUpdatedUserId = entity.getLastUpdatedUserId();
        Long tenantId = entity.getTenantId();
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();
        Integer groupId = entity.getGroupId();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                ObjectMapper objectMappers = new ObjectMapper();
                HwDetectionResponse hwDetectionResponse = objectMappers.readValue(responseBody, HwDetectionResponse.class);
                if (hwDetectionResponse.getOutputs() != null && !hwDetectionResponse.getOutputs().isEmpty()) {
                    hwDetectionResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(hwDetectionDataItem -> {
                            try {
                                extractOutputDataRequest(entity, hwDetectionDataItem, parentObj, hwDetectionResponse.getModelName(), hwDetectionResponse.getModelVersion());
                            } catch (JsonProcessingException e) {
                                throw new HandymanException("Handwritten classification failed in processing response", e);
                            }

                        });
                    });
                }
            } else {
                parentObj.add(HwClassificationOutputTable.builder()
                        .createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                        .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                        .tenantId(Optional.ofNullable(tenantId).map(String::valueOf).map(Long::valueOf).orElse(null))
                        .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                        .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                        .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                        .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Long::parseLong).orElse(null))
                        .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(STAGE)
                        .message(response.message())
                        .groupId(entity.getGroupId())
                        .rootPipelineId(entity.getRootPipelineId())
                        .batchId(entity.getBatchId())
                        .createdOn(entity.getCreatedOn())
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
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
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(STAGE)
                    .message(ExceptionUtil.toString(e))
                    .groupId(entity.getGroupId())
                    .rootPipelineId(entity.getRootPipelineId())
                    .batchId(entity.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .build());
            log.error(aMarker, "The Exception occurred in paper classification request", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Paper classification (hw-detection) consumer failed for batch/group " + groupId, handymanException, this.action);

        }
    }

    private void extractOutputDataRequest(HwClassificationInputTable entity, String responseBody, List<HwClassificationOutputTable> parentObj, String modelName, String modelVersion) throws JsonProcessingException {
        String createdUserId = entity.getCreatedUserId();
        String lastUpdatedUserId = entity.getLastUpdatedUserId();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();
        log.info("copro api response body {}", responseBody);

        HwDetectionDataItem hwDetectionDataItem = mapper.readValue(responseBody, new TypeReference<>() {
        });

        parentObj.add(HwClassificationOutputTable.builder().createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                .tenantId(hwDetectionDataItem.getTenantId())
                .originId(hwDetectionDataItem.getOriginId())
                .paperNo(hwDetectionDataItem.getPaperNo())
                .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Long::parseLong).orElse(null))
                .groupId(hwDetectionDataItem.getGroupId())
                .documentType(hwDetectionDataItem.getDocumentStatus())
                .confidenceScore(hwDetectionDataItem.getConfidenceScore())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(STAGE)
                .message("Paper Classification Finished")
                .processId(hwDetectionDataItem.getProcessId())
                .rootPipelineId(entity.getRootPipelineId())
                .modelName(modelName)
                .modelVersion(modelVersion)
                .createdOn(entity.getCreatedOn())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .batchId(entity.getBatchId())
                .build());
    }

    private void extractedCoproOutputResponse(HwClassificationInputTable entity, String responseBody, List<HwClassificationOutputTable> parentObj, String modelName, String modelVersion) throws JsonProcessingException {
        String createdUserId = entity.getCreatedUserId();
        String lastUpdatedUserId = entity.getLastUpdatedUserId();
        Long tenantId = entity.getTenantId();
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();
        Integer groupId = entity.getGroupId();
        log.info("copro api response body {}", responseBody);

        HwDetectionDataItemCopro hwDetectionDataItem = mapper.readValue(responseBody, new TypeReference<>() {
        });

        parentObj.add(HwClassificationOutputTable.builder().createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                .tenantId(Optional.ofNullable(tenantId).map(String::valueOf).map(Long::valueOf).orElse(null))
                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Long::parseLong).orElse(null))
                .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                .documentType(hwDetectionDataItem.getDocumentStatus())
                .confidenceScore(hwDetectionDataItem.getConfidenceScore())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(STAGE)
                .message("Paper Classification Finished")
                .groupId(entity.getGroupId())
                .rootPipelineId(entity.getRootPipelineId())
                .modelName(modelName)
                .modelVersion(modelVersion)
                .createdOn(entity.getCreatedOn())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .batchId(entity.getBatchId())
                .build());
    }
}





