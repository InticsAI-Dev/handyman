package in.handyman.raven.lib.model.agentic.paper.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.core.utils.ProcessFileFormatE;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.AgenticPaperFilterAction;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpExtractionRequest;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpExtractionResponse;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpLineItem;
import in.handyman.raven.lib.model.retry.CoproRetryErrorAuditTable;
import in.handyman.raven.lib.model.retry.CoproRetryService;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.TritonDataTypes;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;
import static in.handyman.raven.exception.HandymanException.handymanRepo;

public class AgenticPaperFilterConsumerProcess implements CoproProcessor.ConsumerProcess<AgenticPaperFilterInput, AgenticPaperFilterOutput> {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final Marker aMarker;

    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    public static final String KRYPTON_START = "KRYPTON START";
    public static final String AGENTIC_PAPER_FILTER_MODEL_NAME = "preprocess.agentic.paper.filter.model.name";
    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
    private static final String MODEL_TYPE = "OPTIMUS";
    private static final String PROCESS_NAME = "AGENTIC PAPER FILTER";
    public static final String PAGE_CONTENT_NO = "no";
    public static final String PAGE_CONTENT_YES = "yes";
    private final int pageContentMinLength;
    final OkHttpClient httpclient;
    final String jdbiResourceName;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String MODEL = "model";
    private static final String ENCRYPTION_ALGO = "AES256";
    private static final String INFER_OUTPUT_KEY = "AGENTIC_INFER_OUTPUT";
    private static final String TRUE = "true";

    private final String processBase64;
    private final FileProcessingUtils fileProcessingUtils;
    private final CoproRetryService coproRetryService;
    private final InticsIntegrity encryption;

    public AgenticPaperFilterConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, AgenticPaperFilterAction aAction, Integer pageContentMinLength, FileProcessingUtils fileProcessingUtils, String processBase64, String jdbiResourceName) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.processBase64 = processBase64;
        this.fileProcessingUtils = fileProcessingUtils;
        this.pageContentMinLength = pageContentMinLength;
        this.encryption = SecurityEngine.getInticsIntegrityMethod(action, log);
        int timeOut = aAction.getTimeOut();

        String httpClientType = aAction.getHttpClientType();
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(timeOut, TimeUnit.MINUTES)
                .writeTimeout(timeOut, TimeUnit.MINUTES)
                .readTimeout(timeOut, TimeUnit.MINUTES);

        if ("HTTP/1.1".equalsIgnoreCase(httpClientType)) {
            log.info(aMarker, "HTTP client protocol explicitly set to HTTP/1.1");
            builder.protocols(List.of(Protocol.HTTP_1_1));
        }

        this.httpclient = builder.build();

        this.jdbiResourceName = jdbiResourceName;
        coproRetryService = new CoproRetryService(handymanRepo, httpclient, log);
    }

    @Override
    public List<AgenticPaperFilterOutput> process(URL endpoint, AgenticPaperFilterInput entity) throws IOException {
        List<AgenticPaperFilterOutput> parentObj = new ArrayList<>();

        try {
            String textExtractionModelName = action.getContext().get(AGENTIC_PAPER_FILTER_MODEL_NAME);
            String inputFilePath = entity.getFilePath();
            String filePath = String.valueOf(entity.getFilePath());

            if (log.isInfoEnabled()) {
                log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} ", endpoint, inputFilePath);
            }
            getCoproHandlerMethod(endpoint, entity, parentObj, textExtractionModelName, filePath);
        } catch (Exception e) {
            String errorMessage = "Error in process method for batch/group" + entity.getGroupId() +
                    " originId " + entity.getOriginId() + " paperNo " + entity.getPaperNo() + "\n message: " + e.getMessage();
            log.error(aMarker, errorMessage, e);
            HandymanException.insertException(errorMessage, new HandymanException(e), this.action);
        }
        return parentObj;
    }

    private void getCoproHandlerMethod(URL endpoint, AgenticPaperFilterInput
            entity, List<AgenticPaperFilterOutput> parentObj, String textExtractionModelName, String filePath) {
        log.debug(aMarker, "Executing TRITON handler for endpoint: {} and model: {}", endpoint, textExtractionModelName);
        getTritonHandlerMethod(endpoint, entity, parentObj, filePath);
    }

    private void getTritonHandlerMethod(URL endpoint, AgenticPaperFilterInput
            entity, List<AgenticPaperFilterOutput> parentObj, String filePath) {
        RadonKvpExtractionRequest kryptonRequestPayloadFromQuery = getKryptonRequestPayloadFromQuery(entity);
        getTritonKryptonHandlerName(endpoint, entity, parentObj, filePath, kryptonRequestPayloadFromQuery);
    }

    private void getTritonKryptonHandlerName(URL endpoint, AgenticPaperFilterInput
            entity, List<AgenticPaperFilterOutput> parentObj, String filePath, RadonKvpExtractionRequest
                                                     kryptonRequestPayloadFromQuery) {
        try {
            String base64Img = processBase64.equals(ProcessFileFormatE.BASE64.name())
                    ? fileProcessingUtils.convertFileToBase64(filePath)
                    : "";
            kryptonRequestPayloadFromQuery.setBase64Img(base64Img);
            String agenticFilterPayloadString = mapper.writeValueAsString(kryptonRequestPayloadFromQuery);
            kryptonRequestPayloadFromQuery.setBase64Img("");
            String requestForInsert = mapper.writeValueAsString(kryptonRequestPayloadFromQuery);
            String jsonRequestTritonKrypton = getTritonRequestPayload(agenticFilterPayloadString);

            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequestTritonKrypton, mediaType)).build();
            tritonRequestKryptonExecutor(entity, request, parentObj, requestForInsert, endpoint);
        } catch (Exception e) {
            String errorMessage = "Error in preparing or sending TRITON request for paper " + entity.getPaperNo() + ": " + e.getMessage();
            log.error(aMarker, errorMessage, e);
            HandymanException.insertException(errorMessage, new HandymanException(e), this.action);
        }
    }

    private RadonKvpExtractionRequest getKryptonRequestPayloadFromQuery(AgenticPaperFilterInput entity) {
        RadonKvpExtractionRequest radonKvpExtractionRequest = new RadonKvpExtractionRequest();
        radonKvpExtractionRequest.setOriginId(entity.getOriginId());
        radonKvpExtractionRequest.setProcessId(entity.getProcessId());
        radonKvpExtractionRequest.setTenantId(entity.getTenantId());
        radonKvpExtractionRequest.setRootPipelineId(entity.getRootPipelineId());
        radonKvpExtractionRequest.setActionId(action.getActionId());
        radonKvpExtractionRequest.setProcess(PROCESS_NAME);
        radonKvpExtractionRequest.setInputFilePath(entity.getFilePath());
        radonKvpExtractionRequest.setBatchId(entity.getBatchId());
        radonKvpExtractionRequest.setUserPrompt(entity.getUserPrompt());
        radonKvpExtractionRequest.setSystemPrompt(entity.getSystemPrompt());
        radonKvpExtractionRequest.setPaperNo(entity.getPaperNo());
        radonKvpExtractionRequest.setGroupId(Long.valueOf(entity.getGroupId()));
        radonKvpExtractionRequest.setModelName(action.getContext().get("agentic.paper.filter.activator").equalsIgnoreCase("true") ? "KRYPTON" : entity.getModelName());
        return radonKvpExtractionRequest;
    }

    private void tritonRequestKryptonExecutor(AgenticPaperFilterInput entity, Request
            request, List<AgenticPaperFilterOutput> parentObj, String requestForInsert, URL endpoint) {
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        Long rootPipelineId = entity.getRootPipelineId();
        String templateName = entity.getTemplateName();
        Timestamp createdOn = CreateTimeStamp.currentTimestamp();
        entity.setCreatedOn(createdOn);

        CoproRetryErrorAuditTable auditInput = setErrorAuditInputDetails(entity, endpoint);
        Response response;
        try {
            response = Boolean.parseBoolean(action.getContext().getOrDefault("copro.isretry.enabled", "false"))
                    ? coproRetryService.callCoproApiWithRetry(request, requestForInsert, auditInput, this.action)
                    : httpclient.newCall(request).execute();

            if (response == null) {
                String errorMessage = "No response received from API";
                parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).message(errorMessage).rootPipelineId(rootPipelineId).templateName(templateName).endpoint(String.valueOf(endpoint)).build());
                log.error(aMarker, errorMessage);
                HandymanException handymanException = new HandymanException(errorMessage);
                HandymanException.insertException(errorMessage, handymanException, this.action);
                throw new IOException(errorMessage);
            }
            try (Response safeResponse = response) {
                Protocol protocol = response.protocol();
                log.info(aMarker, " Protocol in use : {} ", protocol);
                String responseBody = Objects.requireNonNull(safeResponse.body()).string();
                
                if (safeResponse.isSuccessful()) {
                    RadonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, RadonKvpExtractionResponse.class);
                    if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                        modelResponse.getOutputs().forEach(o -> o.getData().forEach(s -> {
                            try {
                                extractedKryptonOutputDataRequest(entity, s, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion(), requestForInsert, endpoint, safeResponse);
                            } catch (JsonProcessingException e) {
                                String errorMessage = "Error in parsing response in consumer failed for batch/group " + entity.getGroupId() + " originId " + entity.getOriginId() + " paperNo " + entity.getPaperNo() + " code " + safeResponse.code() + " message : " + safeResponse.message();
                                handleKryptonErrorResponse(entity, parentObj, requestForInsert, endpoint, tenantId, templateId, processId, safeResponse, rootPipelineId, templateName);
                                HandymanException handymanException = new HandymanException(errorMessage);
                                HandymanException.insertException(errorMessage, handymanException, this.action);
                                log.error(aMarker, errorMessage);
                            }
                        }));
                    } else {
                        String errorMessage = "Successful response in consumer but output node not found for batch/group " + entity.getGroupId() + " originId: " + entity.getOriginId() + " paperNo: " + entity.getPaperNo() + " code: " + safeResponse.code() + "\n message: " + safeResponse.message();
                        handleKryptonErrorResponse(entity, parentObj, requestForInsert, endpoint, tenantId, templateId, processId, safeResponse, rootPipelineId, templateName);
                        HandymanException handymanException = new HandymanException(errorMessage);
                        HandymanException.insertException(errorMessage, handymanException, this.action);
                        log.error(aMarker, errorMessage);
                    }
                } else {
                    String errorMessage = "Unsuccessful response in consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo() + " code : " + response.code() + "\n message : " + responseBody;
                    handleKryptonErrorResponse(entity, parentObj, requestForInsert, endpoint, tenantId, templateId, processId, safeResponse, rootPipelineId, templateName);
                    HandymanException handymanException = new HandymanException(errorMessage);
                    HandymanException.insertException(errorMessage, handymanException, this.action);
                    log.error(aMarker, errorMessage);
                }
            }
        } catch (Exception e) {
            String errorMessage = "Error in api call consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo() + "\n message : " + e.getMessage();
            parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).message(errorMessage).rootPipelineId(rootPipelineId).templateName(templateName).endpoint(String.valueOf(endpoint)).build());
            log.error(aMarker, errorMessage);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(errorMessage, handymanException, this.action);
        }
    }

    private CoproRetryErrorAuditTable setErrorAuditInputDetails(AgenticPaperFilterInput entity, URL endPoint) {
        return CoproRetryErrorAuditTable.builder()
                .originId(entity.getOriginId())
                .paperNo(entity.getPaperNo())
                .groupId(entity.getGroupId() != null ? Math.toIntExact(entity.getGroupId()) : null)
                .tenantId(entity.getTenantId())
                .processId(entity.getProcessId())
                .filePath(entity.getFilePath())
                .paperNo(entity.getPaperNo())
                .createdOn(entity.getCreatedOn())
                .rootPipelineId(entity.getRootPipelineId())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(PROCESS_NAME)
                .batchId(entity.getBatchId())
                .endpoint(String.valueOf(endPoint))
                .templateId(entity.getTemplateId())
                .build();
    }

    private void handleKryptonErrorResponse(AgenticPaperFilterInput
                                                    entity, List<AgenticPaperFilterOutput> parentObj, String jsonRequest, URL endpoint, Long tenantId, String
                                                    templateId, Long processId, Response response, Long rootPipelineId, String templateName) {
        parentObj.add(AgenticPaperFilterOutput.builder()
                .batchId(entity.getBatchId())
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .groupId(entity.getGroupId())
                .paperNo(entity.getPaperNo())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(PROCESS_NAME)
                .tenantId(tenantId)
                .templateId(templateId)
                .processId(processId)
                .message(response.message())
                .createdOn(entity.getCreatedOn())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .rootPipelineId(rootPipelineId)
                .templateName(templateName)
                .endpoint(String.valueOf(endpoint)).build());
    }


    private void extractedKryptonOutputDataRequest(AgenticPaperFilterInput entity, String stringDataItem,
                                                   List<AgenticPaperFilterOutput> parentObj, String modelName,
                                                   String modelVersion, String request,
                                                   URL endpoint,Response safeResponse) throws JsonProcessingException {

        String cleanedJson = stringDataItem.replace("```json", "").replace("```", "").trim();

        JSONObject json = new JSONObject(cleanedJson);
        JsonNode inferResponseNode = null;
        String formattedInferResponse;
        RadonKvpLineItem dataExtractionDataItem = mapper.readValue(cleanedJson, RadonKvpLineItem.class);
        String inferResponseJson = dataExtractionDataItem.getInferResponse();

        if(inferResponseJson == null){
            handleKryptonErrorResponse(entity, parentObj, request, endpoint, entity.getTenantId(), entity.getTemplateId(), entity.getProcessId(), safeResponse, entity.getRootPipelineId(), entity.getTemplateName());
        }else {

            if (json.has(MODEL)) {
                String modelValue = json.getString(MODEL);
                if (!MODEL_TYPE.equalsIgnoreCase(modelValue)) {
                    //KRYPTON
                    inferResponseNode = mapper.readTree(inferResponseJson);
                } else {
                    inferResponseNode = TextNode.valueOf(inferResponseJson.trim());
                }
            }
            String flag = (inferResponseJson.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;
            String templateId = entity.getTemplateId();
            Iterator<Map.Entry<String, JsonNode>> fields = Objects.requireNonNull(inferResponseNode).fields();
            if (MODEL_TYPE.equalsIgnoreCase(json.getString(MODEL))) {
                doOptimusParentObjectBuild(entity, parentObj, modelName, modelVersion, request, endpoint.toString(), dataExtractionDataItem, flag, templateId, inferResponseNode);

            } else {
                doKryptonParentObjBuild(entity, parentObj, modelName, modelVersion, request, endpoint.toString(), fields, dataExtractionDataItem, flag, templateId);
            }
        }


    }

    private void doKryptonParentObjBuild(AgenticPaperFilterInput entity, List<AgenticPaperFilterOutput> parentObj, String modelName, String modelVersion, String request, String endpoint, Iterator<Map.Entry<String, JsonNode>> fields, RadonKvpLineItem dataExtractionDataItem, String flag, String templateId) {
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String containerName = entry.getKey();
            String containerValue = entry.getValue().asText();

            parentObj.add(AgenticPaperFilterOutput.builder()
                    .filePath(entity.getFilePath())
                    .originId(dataExtractionDataItem.getOriginId())
                    .groupId(Math.toIntExact(dataExtractionDataItem.getGroupId()))
                    .paperNo(dataExtractionDataItem.getPaperNo())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message("Agentic Paper Filter macro completed with krypton triton api call " + entity.getModelName())
                    .createdOn(entity.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .isBlankPage(flag)
                    .tenantId(dataExtractionDataItem.getTenantId())
                    .templateId(templateId)
                    .processId(dataExtractionDataItem.getProcessId())
                    .templateName(entity.getTemplateName())
                    .rootPipelineId(dataExtractionDataItem.getRootPipelineId())
                    .modelName(entity.getModelName() != null ? entity.getModelName() : modelName)
                    .modelVersion(modelVersion)
                    .batchId(entity.getBatchId())
                    .endpoint(String.valueOf(endpoint))
                    .containerName(containerName)
                    .containerValue(containerValue)
                    .build());
        }
    }

    private void doOptimusParentObjectBuild(AgenticPaperFilterInput entity, List<AgenticPaperFilterOutput> parentObj, String modelName, String modelVersion, String request, String endpoint, RadonKvpLineItem dataExtractionDataItem, String flag, String templateId, JsonNode inferResponseNode) {
        Long groupId = dataExtractionDataItem.getGroupId();
        Integer paperNo = dataExtractionDataItem.getPaperNo();
        String statusDescription = ConsumerProcessApiStatus.COMPLETED.getStatusDescription();
        String originId = dataExtractionDataItem.getOriginId();
        Timestamp createdOn = entity.getCreatedOn();
        String batchId = entity.getBatchId();
        String promptType = entity.getPromptType();
        parentObj.add(AgenticPaperFilterOutput.builder()
                .filePath(entity.getFilePath())
                .originId(originId)
                .groupId(groupId != null ? Math.toIntExact(groupId) : 0)
                .paperNo(paperNo)
                .status(statusDescription)
                .stage(PROCESS_NAME)
                .message("Agentic Paper Filter macro completed with optimus triton api call " + entity.getModelName())
                .createdOn(createdOn)
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .isBlankPage(flag)
                .tenantId(dataExtractionDataItem.getTenantId())
                .templateId(templateId)
                .processId(dataExtractionDataItem.getProcessId())
                .templateName(entity.getTemplateName())
                .rootPipelineId(dataExtractionDataItem.getRootPipelineId())
                .modelName(entity.getModelName() != null ? entity.getModelName() : modelName)
                .modelVersion(modelVersion)
                .batchId(batchId)
                .endpoint(String.valueOf(endpoint))
                .containerValue("yes".equalsIgnoreCase(inferResponseNode.asText()) ? "true" : "false")
                .containerName(entity.getUniqueName())
                .containerId(entity.getUniqueId())
                .promptType(promptType)
                .build());
    }

    private static String getTritonRequestPayload(String dataExtractionPayloadString) throws JsonProcessingException {
        TritonRequest tritonRequestPayload = new TritonRequest();
        tritonRequestPayload.setName(AgenticPaperFilterConsumerProcess.KRYPTON_START);
        tritonRequestPayload.setShape(List.of(1, 1));
        tritonRequestPayload.setDatatype(TritonDataTypes.BYTES.name());
        tritonRequestPayload.setData(Collections.singletonList(dataExtractionPayloadString));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(tritonRequestPayload));

        return AgenticPaperFilterConsumerProcess.mapper.writeValueAsString(tritonInputRequest);
    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        String requestStr;
        if ("true".equals(encryptReqRes)) {
            String encryptedRequest = encryption.encrypt(request, ENCRYPTION_ALGO, "AP_COPRO_REQUEST");
            log.debug(aMarker, "Request has been encrypted");
            requestStr = encryptedRequest;
        } else {
            requestStr = request;
        }
        return requestStr;
    }
}