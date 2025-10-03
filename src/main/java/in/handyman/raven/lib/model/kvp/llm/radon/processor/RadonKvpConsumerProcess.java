package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import bsh.EvalError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.core.utils.ProcessFileFormatE;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.RadonKvpAction;
import in.handyman.raven.lib.custom.kvp.post.processing.processor.ProviderDataTransformer;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.retry.CoproRetryErrorAuditTable;
import in.handyman.raven.lib.model.retry.CoproRetryService;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.util.ExceptionUtil;
import in.handyman.raven.util.LoggingInitializer;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;
import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;
import static in.handyman.raven.core.utils.DatabaseUtility.fetchBshResultByClassName;
import static in.handyman.raven.exception.HandymanException.handymanRepo;

public class RadonKvpConsumerProcess implements CoproProcessor.ConsumerProcess<RadonQueryInputTable, RadonQueryOutputTable> {

    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.radon.kvp.activator";
    public static final String PROCESS_NAME = PipelineName.RADON_KVP_ACTION.getProcessName();
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final FileProcessingUtils fileProcessingUtils;
    private final String processBase64;

    private String jdbiResourceName;

    private final ProviderDataTransformer providerDataTransformer;

    private final CoproRetryService coproRetryService;


    public RadonKvpConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, RadonKvpAction aAction, final String processBase64, final FileProcessingUtils fileProcessingUtils, ProviderDataTransformer providerDataTransformer, String jdbiResourceName) {
        LoggingInitializer.initialize();
        
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.jdbiResourceName = jdbiResourceName;
        this.providerDataTransformer = providerDataTransformer;
        int timeOut = aAction.getTimeOut();
        this.processBase64 = processBase64;
        this.fileProcessingUtils = fileProcessingUtils;

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

        coproRetryService = new CoproRetryService(handymanRepo, httpclient);
    }


    @Override
    public List<RadonQueryOutputTable> process(URL endpoint, RadonQueryInputTable entity) throws Exception {
        List<RadonQueryOutputTable> parentObj = new ArrayList<>();
        String rootPipelineId = String.valueOf(entity.getRootPipelineId());
        String filePath = String.valueOf(entity.getInputFilePath());
        Long actionId = action.getActionId();
        Long groupId = entity.getGroupId();
        String userPrompt = "";
        String systemPrompt = entity.getSystemPrompt();
        Integer paperNo = entity.getPaperNo();
        String originId = entity.getOriginId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();

        if (Objects.equals(action.getContext().get("bbox.radon_bbox_activator"), "true")
                && Objects.equals(entity.getProcess(), "RADON_KVP_ACTION")) {


            String inputResponseJsonstr = entity.getInputResponseJson();
            String inputResponseJson;
            String encryptOutputJsonContent = action.getContext().get(ENCRYPT_ITEM_WISE_ENCRYPTION);


            InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);

            if (Objects.equals(encryptOutputJsonContent, "true")) {
                inputResponseJson = encryption.decrypt(inputResponseJsonstr, "AES256", "RADON_KVP_JSON");
            } else {
                log.info("Encryption is disabled. Using raw input response JSON.");
                inputResponseJson = inputResponseJsonstr;
            }

            String base64Activator = action.getContext().get("sor.transaction.prompt.base64.activator");

            if (Objects.equals(base64Activator, "true")) {

                Base64toActualVaue base64Caller = new Base64toActualVaue();
                String base64Value = base64Caller.base64toActual(entity.getUserPrompt());


                byte[] decodedBytes = Base64.getDecoder().decode(base64Value);
                String decodedPrompt = new String(decodedBytes);


                String updatedPrompt = decodedPrompt.replace(
                        action.getContext().get("prompt.bbox.json.placeholder.name"), inputResponseJson);

                userPrompt = Base64.getEncoder().encodeToString(updatedPrompt.getBytes());

            } else {
                log.info("Base64 activator is OFF. Using plain text prompt.");

                String actualUserPrompt = entity.getUserPrompt();

                userPrompt = actualUserPrompt.replace(
                        action.getContext().get("prompt.bbox.json.placeholder.name"), inputResponseJson);

            }
        } else {
            userPrompt = entity.getUserPrompt();
        }


        //payload
        RadonKvpExtractionRequest radonKvpExtractionRequest = new RadonKvpExtractionRequest();

        radonKvpExtractionRequest.setRootPipelineId(Long.valueOf(rootPipelineId));
        radonKvpExtractionRequest.setActionId(actionId);
        radonKvpExtractionRequest.setProcess(entity.getProcess());
        radonKvpExtractionRequest.setInputFilePath(filePath);
        radonKvpExtractionRequest.setGroupId(groupId);
        radonKvpExtractionRequest.setUserPrompt(userPrompt);
        radonKvpExtractionRequest.setSystemPrompt(systemPrompt);
        radonKvpExtractionRequest.setProcessId(processId);
        radonKvpExtractionRequest.setPaperNo(paperNo);
        radonKvpExtractionRequest.setTenantId(tenantId);
        radonKvpExtractionRequest.setOriginId(originId);
        radonKvpExtractionRequest.setBatchId(entity.getBatchId());
        radonKvpExtractionRequest.setSorContainerId(entity.getSorContainerId());
        radonKvpExtractionRequest.setModelName(entity.getModelName());

        String base64Content = processBase64.equals(ProcessFileFormatE.BASE64.name())
                ? fileProcessingUtils.convertFileToBase64(filePath)
                : "";
        radonKvpExtractionRequest.setBase64Img(base64Content);

        String jsonInputRequest = mapper.writeValueAsString(radonKvpExtractionRequest);
        
        radonKvpExtractionRequest.setBase64Img("");
        String jsonInsertRequest = mapper.writeValueAsString(radonKvpExtractionRequest);
        String jsonInsertRequestEncrypted = encryptRequestResponse(jsonInsertRequest);


        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} with container Id {}", endpoint, filePath, entity.getSorContainerId());
        }
        // creating request body
 
        kvpRequestBuilder(entity, parentObj, jsonInsertRequestEncrypted, jsonInputRequest, endpoint);

        log.info(aMarker, "Radon kvp consumer process output parent object entities size {}", parentObj.size());
        return parentObj;
    }

    @NotNull
    private Request getRequest(URL endpoint, String jsonInputRequest) {
        Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
        return request;
    }

    private void kvpRequestBuilder(RadonQueryInputTable entity, List<RadonQueryOutputTable> parentObj, String jsonInsertRequestEncrypted, String jsonInputRequest, URL endpoint) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();

        Timestamp createdOn = CreateTimeStamp.currentTimestamp();
        entity.setCreatedOn(createdOn);


        CoproRetryErrorAuditTable auditInput = setErrorAuditInputDetails(entity, endpoint);
        Response response;
        try {
            // request curl creation
            final Request request = getRequest(endpoint, jsonInputRequest);
            // retry mechanism
            response = Boolean.parseBoolean(action.getContext().getOrDefault("copro.isretry.enabled", "false"))
                    ? coproRetryService.callCoproApiWithRetry(request, jsonInsertRequestEncrypted, auditInput, this.action)
                    : httpclient.newCall(request).execute();
            if (response == null) {
                String errorMessage = "No response received from API";
                parentObj.add(RadonQueryOutputTable.builder().processId(entity.getProcessId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).processId(processId).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).message(errorMessage).rootPipelineId(rootPipelineId).request(encryptRequestResponse(jsonInsertRequestEncrypted)).response(errorMessage).endpoint(String.valueOf(endpoint)).build());
                log.error(aMarker, errorMessage);
                HandymanException handymanException = new HandymanException(errorMessage);
                HandymanException.insertException(errorMessage, handymanException, this.action);
                throw new IOException(errorMessage);
            }

            try (Response safeResponse = response) {
                Protocol protocol = response.protocol();
                log.info(aMarker, " Protocol in use : {} ", protocol);
                if (safeResponse.isSuccessful()) {
                    assert safeResponse.body() != null;
                    String responseBody = safeResponse.body().string();
                    RadonKvpDataOutput modelResponse = mapper.readValue(responseBody, RadonKvpDataOutput.class);
                    if (modelResponse != null) {
                        try {
                            extractKVPOutputDataResponse(entity, modelResponse, parentObj, jsonInsertRequestEncrypted, responseBody, endpoint.toString());
                        } catch (IOException | EvalError e) {
                            HandymanException handymanException = new HandymanException(e);
                            HandymanException.insertException("Radon kvp consumer failed for batch/group " + groupId + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
                            log.error(aMarker, "The Exception occurred in converting the response from triton server output {}", ExceptionUtil.toString(e));
                        }

                    }
                } else {
                    String errorBody = safeResponse.body() != null ? safeResponse.body().string() : "No response body or detail found for the request.";

                    parentObj.add(RadonQueryOutputTable.builder()
                            .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                            .paperNo(paperNo)
                            .groupId(groupId)
                            .inputFilePath(entity.getInputFilePath())
                            .tenantId(tenantId)
                            .actionId(action.getActionId())
                            .processId(processId)
                            .rootPipelineId(rootPipelineId)
                            .process(entity.getProcess())
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .stage(entity.getApiName())
                            .message(encryptRequestResponse(safeResponse.message()))
                            .batchId(entity.getBatchId())
                            .createdOn(entity.getCreatedOn())
                            .createdUserId(tenantId)
                            .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                            .lastUpdatedUserId(tenantId)
                            .category(entity.getCategory())
                            .request(jsonInsertRequestEncrypted)
                            .response(encryptRequestResponse(errorBody))
                            .endpoint(String.valueOf(endpoint))
                            .build());
                    HandymanException handymanException = new HandymanException("Unsuccessful response code : " + safeResponse.code() + " message : " + errorBody);
                    HandymanException.insertException("Radon kvp consumer failed for batch/group " + groupId + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);

                    log.error(aMarker, "Error in getting response from triton api {}", errorBody);
                }
            }
        } catch (IOException e) {
            handleErrorParentObject(entity, parentObj, e, jsonInsertRequestEncrypted);

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Radon kvp consumer failed for batch/group " + groupId + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response  from triton server {}", ExceptionUtil.toString(e));
        }
    }


    private CoproRetryErrorAuditTable setErrorAuditInputDetails(RadonQueryInputTable entity, URL endPoint) {
        return CoproRetryErrorAuditTable.builder()
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .paperNo(entity.getPaperNo())
                .message("Sor container Id : " + entity.getSorContainerId() + " for the process " + entity.getProcess())
                .groupId(entity.getGroupId() != null ? Math.toIntExact(entity.getGroupId()) : null)
                .tenantId(entity.getTenantId())
                .processId(entity.getProcessId())
                .filePath(entity.getInputFilePath())
                .createdOn(entity.getCreatedOn())
                .rootPipelineId(entity.getRootPipelineId())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(PROCESS_NAME)
                .batchId(entity.getBatchId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .endpoint(String.valueOf(endPoint))
                .build();

    }
    private void handleErrorParentObject(RadonQueryInputTable entity, List<RadonQueryOutputTable> parentObj, Exception e,String jsonInsertRequest) {
        parentObj.add(RadonQueryOutputTable.builder()
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .paperNo(entity.getPaperNo())
                .groupId(entity.getGroupId())
                .inputFilePath(entity.getInputFilePath())
                .tenantId(entity.getTenantId())
                .processId(entity.getProcessId())
                .rootPipelineId(entity.getRootPipelineId())
                .actionId(action.getActionId())
                .process(entity.getProcess())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(entity.getApiName())
                .request(jsonInsertRequest)
                .message(encryptRequestResponse(ExceptionUtil.toString(e)))
                .batchId(entity.getBatchId())
                .createdOn(entity.getCreatedOn())
                .createdUserId(entity.getTenantId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(entity.getTenantId())
                .category(entity.getCategory())
                .build());
    }


    private void extractKVPOutputDataResponse(RadonQueryInputTable entity, RadonKvpDataOutput modelResponse, List<RadonQueryOutputTable> parentObj, String jsonInsertRequestEncrypted, String response, String endpoint) throws IOException, EvalError {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();
        String extractedContent;
        final String metrics = mapper.writeValueAsString(modelResponse.getComputationDetails());

        String encryptOutputJsonContent = action.getContext().get(ENCRYPT_ITEM_WISE_ENCRYPTION);
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);
        if (Boolean.TRUE.equals(entity.getPostProcess())) {
            String providerClassName = action.getContext().get(entity.getPostProcessClassName());
            Optional<String> sourceCode = fetchBshResultByClassName(jdbiResourceName, providerClassName, tenantId);
            if (sourceCode.isPresent()) {
                List<RadonQueryOutputTable> providerParentObj = providerDataTransformer.processProviderData(sourceCode.get(), providerClassName, modelResponse.getInferResponse(), entity, jsonInsertRequestEncrypted, response, endpoint);
                parentObj.addAll(providerParentObj);
            }

        } else {
            if (Objects.equals(encryptOutputJsonContent, "true")) {
                extractedContent = encryption.encrypt(modelResponse.getInferResponse(), "AES256", "RADON_KVP_JSON");
            } else {
                extractedContent = modelResponse.getInferResponse();
            }

            parentObj.add(RadonQueryOutputTable.builder()
                    .createdOn(entity.getCreatedOn())
                    .createdUserId(tenantId)
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .lastUpdatedUserId(tenantId)
                    .originId(originId)
                    .paperNo(paperNo)
                    .totalResponseJson(extractedContent)
                    .groupId(groupId)
                    .inputFilePath(processedFilePaths)
                    .actionId(action.getActionId())
                    .tenantId(tenantId)
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .process(entity.getProcess())
                    .batchId(modelResponse.getBatchId())
                    .modelRegistry(entity.getModelRegistry())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(entity.getApiName())
                    .batchId(entity.getBatchId())
                    .category(entity.getCategory())
                    .message("Radon kvp action macro completed")
                    .request(jsonInsertRequestEncrypted)
                    .response(encryptRequestResponse(response))
                    .sorContainerId(entity.getSorContainerId())
                    .endpoint(String.valueOf(endpoint))
                    .statusCode(modelResponse.getStatusCode())
                    .computationDetails(metrics)
                    .log(modelResponse.getErrorMessage())
                    .detail(modelResponse.getDetail())
                    .build()
            );
        }
    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        String requestStr;
        if ("true".equals(encryptReqRes)) {
            String encryptedRequest = SecurityEngine.getInticsIntegrityMethod(action, log).encrypt(request, "AES256", "KVP_COPRO_REQUEST");
            requestStr = encryptedRequest;
        } else {
            requestStr = request;
        }
        return requestStr;
    }
}
