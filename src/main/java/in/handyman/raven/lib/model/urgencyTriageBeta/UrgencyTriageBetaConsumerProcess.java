package in.handyman.raven.lib.model.urgencyTriageBeta;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.lib.model.utmodel.*;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.lib.UrgencyTriageBetaAction.urgencyTriageBeta;


public class UrgencyTriageBetaConsumerProcess implements CoproProcessor.ConsumerProcess<UrgencyTriageInputTable, UrgencyTriageBetaOutputTable> {

    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    public static final String URGENCY_TRIAGE_PROCESS_NAME = PipelineName.URGENCY_TRIAGE.getProcessName();
    private final Logger log;
    private final Marker aMarker;
    private final MediaType mediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public UrgencyTriageBetaConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }

    @Override
    public List<UrgencyTriageBetaOutputTable> process(URL endpoint, UrgencyTriageInputTable entity) throws Exception {
        final List<UrgencyTriageBetaOutputTable> parentObj = new ArrayList<>();

        final String inputFilePath = entity.getInputFilePath();
        final String outputDir = urgencyTriageBeta.getOutputDir();

        final ObjectMapper objectMapper = new ObjectMapper();
        final UrgencyTriageModelPayload urgencyTriageModelPayload = getUrgencyTriageModelPayload(entity, outputDir);
        final String jsonInputRequest = objectMapper.writeValueAsString(urgencyTriageModelPayload);

        final TritonRequest requestBody = new TritonRequest();
        requestBody.setName("UT START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        final TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        final String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);
        final String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);

        if (Objects.equals("false", tritonRequestActivator)) {
            log.info(aMarker, "Request for copro API");
        } else {
            final Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonRequest, mediaTypeJSON)).build();
            tritonRequestBuilder(entity, request, objectMapper, parentObj, jsonRequest, endpoint);
        }
        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n copro Url  {} ,inputFilePath : {} ,outputDir {} ", endpoint, inputFilePath, outputDir);
        }
        return parentObj;
    }

    private @NotNull UrgencyTriageModelPayload getUrgencyTriageModelPayload(UrgencyTriageInputTable entity, String outputDir) {
        final UrgencyTriageModelPayload urgencyTriageModelPayload = new UrgencyTriageModelPayload();
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
        return urgencyTriageModelPayload;
    }

    private void tritonRequestBuilder(UrgencyTriageInputTable entity, Request request, ObjectMapper objectMapper, List<UrgencyTriageBetaOutputTable> parentObj, String jsonRequest, URL endpoint) {
        final String createdUserId = entity.getCreatedUserId();
        final Long tenantId = entity.getTenantId();
        final Long processId = entity.getProcessId();
        final Integer groupId = entity.getGroupId();
        final String originId = entity.getOriginId();
        final Integer paperNo = entity.getPaperNo();
        final String templateId = entity.getTemplateId();
        final Long modelId = entity.getModelId();

        try (Response response = httpclient.newCall(request).execute()) {
            final String responseBody = Objects.requireNonNull(response.body()).string();
            log.info("Raw response: {}", responseBody);
            if (response.isSuccessful()) {
                if (isValidJSON(responseBody)) {
                    UrgencyTriageModelResponse modelResponse = objectMapper.readValue(responseBody, UrgencyTriageModelResponse.class);

                    if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                        modelResponse.getOutputs().forEach(o -> o.getData().forEach(urgencyTriageModelDataItem -> {
                            extractedTritonOutputRequest(entity, urgencyTriageModelDataItem, objectMapper, parentObj, modelResponse.getModelVersion(), modelResponse.getModelName(), jsonRequest, responseBody, endpoint.toString());
                            log.info(aMarker, "Execute for urgency triage {}", response.isSuccessful());
                        }));
                    }
                } else {
                    log.error("Invalid JSON response: {}", responseBody);
                    handleErrorResponse(entity, parentObj, responseBody, createdUserId, tenantId, processId, groupId, originId, paperNo, templateId, modelId, jsonRequest, response.message(), endpoint.toString());
                }
            } else {
                handleErrorResponse(entity, parentObj, responseBody, createdUserId, tenantId, processId, groupId, originId, paperNo, templateId, modelId, jsonRequest, response.message(), endpoint.toString());
                log.error("The Exception occurred in urgency triage {}", response);
            }
        } catch (Exception e) {
            handleErrorResponse(entity, parentObj, ExceptionUtil.toString(e), createdUserId, tenantId, processId, groupId, originId, paperNo, templateId, modelId, jsonRequest, "null", endpoint.toString());
            log.error("The Exception occurred in urgency triage", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Exception occurred in urgency triage model action for group id - " + groupId + " and originId - " + originId, handymanException, this.action);
        }
    }

    private boolean isValidJSON(String responseBody) {
        try {
            new ObjectMapper().readTree(responseBody);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void handleErrorResponse(UrgencyTriageInputTable entity, List<UrgencyTriageBetaOutputTable> parentObj, String message, String createdUserId, Long tenantId, Long processId, Integer groupId, String originId, Integer paperNo, String templateId, Long modelId, String request, String response, String endpoint) {
        parentObj.add(UrgencyTriageBetaOutputTable.builder()
                .createdUserId(createdUserId)
                .lastUpdatedUserId(createdUserId)
                .tenantId(tenantId)
                .processId(processId)
                .groupId(groupId)
                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                .paperNo(paperNo)
                .templateId(templateId)
                .modelId(modelId)
                .status("FAILED")
                .stage("URGENCY_TRIAGE_MODEL")
                .message(message)
                .rootPipelineId(entity.getRootPipelineId())
                .batchId(entity.getBatchId())
                        .request(request)
                        .response(response)
                        .endpoint(endpoint)
                .build());
    }


    private static void extractedTritonOutputRequest(UrgencyTriageInputTable entity, String urgencyTriageModelDataItem, ObjectMapper objectMapper, List<UrgencyTriageBetaOutputTable> parentObj, String modelVersion, String modelName, String request, String response,String endpoint) {
        final String createdUserId = entity.getCreatedUserId();
        final String templateId = entity.getTemplateId();
        final Long modelId = entity.getModelId();
        final Long tenantId = entity.getTenantId();
        final Long processId = entity.getProcessId();
        final Integer groupId = entity.getGroupId();
        final String originId = entity.getOriginId();
        final Integer paperNo = entity.getPaperNo();

        try {
            final BetaUrgencyModelOuterItem modelOuterItem = objectMapper.readValue(urgencyTriageModelDataItem, BetaUrgencyModelOuterItem.class);

            final BetaUrgencyModelItems urgencyCheckboxModel = modelOuterItem.getUrgencyCheckboxModel();
            final BetaUrgencyModelItems urgencyHandwrittenModel = modelOuterItem.getUrgencyHandwrittenModel();
            final BetaUrgencyModelItems urgencyBinaryModel = modelOuterItem.getUrgencyBinaryModel();

            final String checkboxPaperType = urgencyCheckboxModel.getPaperType();
            final String handwrittenPaperType = urgencyHandwrittenModel.getPaperType();
            final String binaryPaperType = urgencyBinaryModel.getPaperType();

            final Float checkboxScore = urgencyCheckboxModel.getConfidenceScore();
            final Float handwrittenScore = urgencyHandwrittenModel.getConfidenceScore();
            final Float binaryScore = urgencyBinaryModel.getConfidenceScore();

            // Assuming getBboxes returns the bounding box as a JSON node, or you can adapt it as per your bounding box structure
            final JsonNode checkboxBBox = objectMapper.valueToTree(urgencyCheckboxModel.getBoundingBox());
            final JsonNode handwrittenBBox = objectMapper.valueToTree(urgencyHandwrittenModel.getBoundingBox());
            final JsonNode binaryBBox = objectMapper.valueToTree(urgencyBinaryModel.getBoundingBox());

            parentObj.add(UrgencyTriageBetaOutputTable.builder()
                    .createdUserId(createdUserId)
                    .lastUpdatedUserId(createdUserId)
                    .tenantId(tenantId)
                    .processId(processId)
                    .groupId(groupId)
                    .originId(originId)
                    .paperNo(paperNo)
                    .templateId(templateId)
                    .modelId(modelId)
                    .checkboxPaperType(checkboxPaperType)
                    .handwrittenPaperType(handwrittenPaperType)
                    .binaryPaperType(binaryPaperType)
                    .checkboxBBox(String.valueOf(checkboxBBox))
                    .handwrittenBBox(String.valueOf(handwrittenBBox))
                    .binaryBBox(String.valueOf(binaryBBox))
                    .checkboxScore(checkboxScore)
                    .handwrittenScore(handwrittenScore)
                    .binaryScore(binaryScore)
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(URGENCY_TRIAGE_PROCESS_NAME)
                    .message("Urgency Triage Finished")
                    .rootPipelineId(entity.getRootPipelineId())
                    .modelVersion(modelVersion)
                    .batchId(entity.getBatchId())
                    .modelName(modelName)
                    .request(request)
                    .response(response)
                    .endpoint(endpoint)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}