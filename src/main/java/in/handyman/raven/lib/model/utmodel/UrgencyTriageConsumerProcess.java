package in.handyman.raven.lib.model.utmodel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.lib.model.utmodel.copro.UrgencyTriageModelDataItemCopro;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.lib.UrgencyTriageModelAction.urgencyTriageModel;

public class UrgencyTriageConsumerProcess implements CoproProcessor.ConsumerProcess<UrgencyTriageInputTable, UrgencyTriageOutputTable> {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    public static final String URGENCY_TRIAGE_PROCESS_NAME = PipelineName.URGENCY_TRIAGE.getProcessName();
    public static final String UT_START = "UT START";
    public static final String TRITON_ACTIVATOR_FALSE = "false";
    public static final String URGENCY_TRIAGE_MODEL = "URGENCY_TRIAGE_MODEL";
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private final MediaType mediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();


    public UrgencyTriageConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }

    @Override
    public List<UrgencyTriageOutputTable> process(URL endpoint, UrgencyTriageInputTable entity) throws Exception {
        List<UrgencyTriageOutputTable> parentObj = new ArrayList<>();

        String inputFilePath = entity.getInputFilePath();
        String outputDir = urgencyTriageModel.getOutputDir();

        ObjectMapper objectMapper = new ObjectMapper();

//payload
        UrgencyTriageModelPayload urgencyTriageModelPayload = new UrgencyTriageModelPayload();
        urgencyTriageModelPayload.setRootPipelineId(entity.getRootPipelineId());
        urgencyTriageModelPayload.setProcess(PipelineName.URGENCY_TRIAGE.getProcessName());
        urgencyTriageModelPayload.setInputFilePath(entity.getInputFilePath());
        urgencyTriageModelPayload.setActionId(action.getActionId());
        urgencyTriageModelPayload.setOutputDir(outputDir);
        urgencyTriageModelPayload.setTenantId(entity.getTenantId());
        urgencyTriageModelPayload.setProcessId(entity.getProcessId());
        urgencyTriageModelPayload.setGroupId(entity.getGroupId());
        urgencyTriageModelPayload.setPaperNo(entity.getPaperNo());
        urgencyTriageModelPayload.setOriginId(entity.getOriginId());
        urgencyTriageModelPayload.setBatchId(entity.getBatchId());


        String jsonInputRequest = objectMapper.writeValueAsString(urgencyTriageModelPayload);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName(UT_START);
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype(TritonDataTypes.BYTES.name());
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);


        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);


        if (Objects.equals(TRITON_ACTIVATOR_FALSE, tritonRequestActivator)) {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonInputRequest, mediaTypeJSON)).build();
            coproRequestBuider(entity, request, objectMapper, parentObj);
        } else {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonRequest, mediaTypeJSON)).build();
            tritonRequestBuilder(entity, request, objectMapper, parentObj);
        }

        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n coproUrl  {} ,inputFilePath : {} ,outputDir {} ", endpoint, inputFilePath, outputDir);
        }

        return parentObj;
    }

    private void tritonRequestBuilder(UrgencyTriageInputTable entity, Request request, ObjectMapper objectMapper, List<UrgencyTriageOutputTable> parentObj) {
        String createdUserId = entity.getCreatedUserId();
        Long tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();
        Integer groupId = entity.getGroupId();
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();
        try (Response response = httpclient.newCall(request).execute()) {
            final String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                UrgencyTriageModelResponse modelResponse = objectMapper.readValue(responseBody, UrgencyTriageModelResponse.class);

                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(urgencyTriageModelDataItem -> {

                            extractedOutputRequest(entity, urgencyTriageModelDataItem, objectMapper, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion());

                            log.info(aMarker, "Execute for urgency triage {}", response.isSuccessful());
                        });
                    });
                }
            } else {
                parentObj.add(UrgencyTriageOutputTable.builder()
                        .createdUserId(createdUserId)
                        .lastUpdatedUserId(createdUserId)
                        .tenantId(tenantId)
                        .processId(processId)
                        .groupId(Long.valueOf(groupId))
                        .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .templateId(templateId)
                        .modelId(modelId)
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(URGENCY_TRIAGE_MODEL)
                        .message(response.message())
                        .rootPipelineId(entity.getRootPipelineId())
                        .batchId(entity.getBatchId())
                        .createdOn(entity.getCreatedOn())
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .build());
                log.error(aMarker, "The Exception occurred in urgency triage {}", response);
            }
        } catch (Exception e) {
            parentObj.add(UrgencyTriageOutputTable.builder()
                    .createdUserId(createdUserId)
                    .lastUpdatedUserId(createdUserId)
                    .tenantId(tenantId)
                    .groupId(Long.valueOf(groupId))
                    .processId(processId)
                    .originId(originId)
                    .paperNo(paperNo)
                    .templateId(templateId)
                    .modelId(modelId)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(URGENCY_TRIAGE_MODEL)
                    .message(ExceptionUtil.toString(e))
                    .rootPipelineId(entity.getRootPipelineId())
                    .batchId(entity.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .build());
            log.error(aMarker, "The Exception occurred in urgency triage", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Exception occurred in urgency triage model action for group id - " + groupId + " and originId - " + originId, handymanException, this.action);
        }
    }

    private static void extractedOutputRequest(UrgencyTriageInputTable entity, String urgencyTriageModelDataItem, ObjectMapper objectMapper, List<UrgencyTriageOutputTable> parentObj, String modelName, String modelVersion) {
        String createdUserId = entity.getCreatedUserId();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();
        try {
            UrgencyTriageModelDataItem urgencyTriageModelDataItem1 = objectMapper.readValue(urgencyTriageModelDataItem, UrgencyTriageModelDataItem.class);
            JsonNode qrBoundingBox = objectMapper.valueToTree(urgencyTriageModelDataItem1.getBboxes());
            Float confScore = urgencyTriageModelDataItem1.getConfidenceScore();
            String paperType = urgencyTriageModelDataItem1.getPaperType();
            parentObj.add(UrgencyTriageOutputTable.builder()
                    .createdUserId(createdUserId)
                    .lastUpdatedUserId(createdUserId)
                    .tenantId(urgencyTriageModelDataItem1.getTenantId())
                    .processId(urgencyTriageModelDataItem1.getProcessId())
                    .groupId(Long.valueOf(urgencyTriageModelDataItem1.getGroupId()))
                    .originId(urgencyTriageModelDataItem1.getOriginId())
                    .paperNo(urgencyTriageModelDataItem1.getPaperNo())
                    .templateId(templateId)
                    .modelId(modelId)
                    .utResult(paperType)
                    .confScore(confScore)
                    .bbox(qrBoundingBox.toString())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(URGENCY_TRIAGE_PROCESS_NAME)
                    .message("Urgency Triage Finished")
                    .rootPipelineId(entity.getRootPipelineId())
                    .modelName(modelName)
                    .modelVersion(modelVersion)
                    .batchId(urgencyTriageModelDataItem1.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private void coproRequestBuider(UrgencyTriageInputTable entity, Request request, ObjectMapper objectMapper, List<UrgencyTriageOutputTable> parentObj) {
        try (Response response = httpclient.newCall(request).execute()) {
            final String responseBody = Objects.requireNonNull(response.body()).string();
            String createdUserId = entity.getCreatedUserId();
            Long tenantId = entity.getTenantId();
            Long processId = entity.getProcessId();
            Integer groupId = entity.getGroupId();
            String originId = entity.getOriginId();
            Integer paperNo = entity.getPaperNo();
            String templateId = entity.getTemplateId();
            Long modelId = entity.getModelId();
            if (response.isSuccessful()) {
                extractedCoproOutputResponse(entity, responseBody, objectMapper, parentObj, "", "");

            } else {
                parentObj.add(UrgencyTriageOutputTable.builder()
                        .createdUserId(createdUserId)
                        .lastUpdatedUserId(createdUserId)
                        .tenantId(tenantId)
                        .processId(processId)
                        .groupId(Long.valueOf(groupId))
                        .originId(originId)
                        .paperNo(paperNo)
                        .templateId(templateId)
                        .modelId(modelId)
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(URGENCY_TRIAGE_PROCESS_NAME)
                        .message(response.message())
                        .rootPipelineId(entity.getRootPipelineId())
                        .batchId(entity.getBatchId())
                        .createdOn(entity.getCreatedOn())
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .build());
                log.error(aMarker, "The Exception occurred in urgency triage {}", response);
            }
        } catch (Exception e) {
            String createdUserId = entity.getCreatedUserId();

            Long tenantId = entity.getTenantId();
            Long processId = entity.getProcessId();
            Integer groupId = entity.getGroupId();
            String originId = entity.getOriginId();
            Integer paperNo = entity.getPaperNo();
            String templateId = entity.getTemplateId();
            Long modelId = entity.getModelId();

            parentObj.add(UrgencyTriageOutputTable.builder()

                    .createdUserId(createdUserId)
                    .lastUpdatedUserId(createdUserId)
                    .tenantId(tenantId)
                    .groupId(Long.valueOf(groupId))
                    .processId(processId)
                    .originId(originId)
                    .paperNo(paperNo)
                    .templateId(templateId)
                    .modelId(modelId)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(URGENCY_TRIAGE_PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .rootPipelineId(entity.getRootPipelineId())
                    .batchId(entity.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .build());
            log.error(aMarker, "The Exception occurred in urgency triage", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Exception occurred in urgency triage model action for group id - " + groupId + " and originId - " + originId, handymanException, this.action);
        }
    }

    private static void extractedCoproOutputResponse(UrgencyTriageInputTable entity, String urgencyTriageModelDataItem, ObjectMapper objectMapper, List<UrgencyTriageOutputTable> parentObj, String modelName, String modelVersion) {
        String createdUserId = entity.getCreatedUserId();
        String lastUpdatedUserId = entity.getLastUpdatedUserId();
        Long tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();
        Integer groupId = entity.getGroupId();
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();
        try {
            UrgencyTriageModelDataItemCopro urgencyTriageModelDataItem1 = objectMapper.readValue(urgencyTriageModelDataItem, UrgencyTriageModelDataItemCopro.class);
            JsonNode qrBoundingBox = objectMapper.valueToTree(urgencyTriageModelDataItem1.getBboxes());
            Float confScore = urgencyTriageModelDataItem1.getConfidenceScore();
            String paperType = urgencyTriageModelDataItem1.getPaperType();
            parentObj.add(UrgencyTriageOutputTable.builder()
                    .createdUserId(createdUserId)
                    .lastUpdatedUserId(createdUserId)
                    .tenantId(tenantId)
                    .processId(processId)
                    .groupId(Long.valueOf(groupId))
                    .originId(originId)
                    .paperNo(paperNo)
                    .templateId(templateId)
                    .modelId(modelId)
                    .utResult(paperType)
                    .confScore(confScore)
                    .bbox(qrBoundingBox.toString())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(URGENCY_TRIAGE_PROCESS_NAME)
                    .message("Urgency Triage Finished")
                    .rootPipelineId(entity.getRootPipelineId())
                    .modelName(modelName)
                    .modelVersion(modelVersion)
                    .batchId(entity.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}



