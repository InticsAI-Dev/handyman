package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.RadonKvpAction;
import in.handyman.raven.lib.encryption.SecurityEngine;
import in.handyman.raven.lib.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.lib.utils.FileProcessingUtils;
import in.handyman.raven.lib.utils.ProcessFileFormatE;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    public RadonKvpConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, RadonKvpAction aAction, final String processBase64, final FileProcessingUtils fileProcessingUtils) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        int timeOut = aAction.getTimeOut();
        this.processBase64 = processBase64;
        this.fileProcessingUtils = fileProcessingUtils;
        this.httpclient = new OkHttpClient.Builder().connectTimeout(timeOut, TimeUnit.MINUTES).writeTimeout(timeOut, TimeUnit.MINUTES).readTimeout(timeOut, TimeUnit.MINUTES).build();
    }

    @Override
    public List<RadonQueryOutputTable> process(URL endpoint, RadonQueryInputTable entity) throws Exception {
        List<RadonQueryOutputTable> parentObj = new ArrayList<>();
        String rootPipelineId = String.valueOf(entity.getRootPipelineId());
        String filePath = String.valueOf(entity.getInputFilePath());
        Long actionId = action.getActionId();
        Long groupId = entity.getGroupId();
        String userPrompt = entity.getUserPrompt();
        String systemPrompt = entity.getSystemPrompt();
        String kryptonInferenceMode = entity.getKryptonInferenceMode();
        Integer paperNo = entity.getPaperNo();
        String originId = entity.getOriginId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();

        // Get the transformation prompts as strings
        String transformationUserPromptsJson = entity.getTransformationUserPrompts();
        String transformationSystemPromptsJson = entity.getTransformationSystemPrompts();

        // Convert JSON strings to List<Map<String, Object>>
        List<Map<String, Object>> transformationUserPromptsList = parseJsonToList(transformationUserPromptsJson, "transformationUserPrompts");
        List<Map<String, Object>> transformationSystemPromptsList = parseJsonToList(transformationSystemPromptsJson, "transformationSystemPrompts");

        // Process all prompts if BBox activator is enabled
        boolean isBBoxActivated = Objects.equals(action.getContext().get("bbox.radon_bbox_activator"), "true")
                && Objects.equals(entity.getProcess(), "RADON_KVP_ACTION");

        String inputResponseJson;
        if (isBBoxActivated) {
            log.info("RADON_KVP_ACTION process started. BBox activator is enabled.");

            // Get and potentially decrypt the input response JSON
            String inputResponseJsonStr = entity.getInputResponseJson();
            String encryptOutputJsonContent = action.getContext().get("pipeline.end.to.end.encryption");
            log.info("Checking if end-to-end encryption is enabled: {}", encryptOutputJsonContent);

            InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action);

            if (Objects.equals(encryptOutputJsonContent, "true")) {
                log.info("Encrypting input response JSON...");
                inputResponseJson = encryption.decrypt(inputResponseJsonStr, "AES256", "RADON_KVP_JSON");
                log.info("Encryption completed successfully.");
            } else {
                log.info("Encryption is disabled. Using raw input response JSON.");
                inputResponseJson = inputResponseJsonStr;
            }

            // Process the user prompt
            String base64Activator = action.getContext().get("sor.transaction.prompt.base64.activator");
            String placeholderName = action.getContext().get("prompt.bbox.json.placeholder.name");

            log.info("Checking if Base64 activator is enabled: {}", base64Activator);

            // Process the main user prompt
            userPrompt = processPrompt(entity.getUserPrompt(), inputResponseJson, base64Activator, placeholderName, "Main user prompt");

            // Process transformation user prompts
            if (!transformationUserPromptsList.isEmpty()) {
                for (Map<String, Object> promptMap : transformationUserPromptsList) {
                    if (promptMap.containsKey("transformationUserPrompt")) {
                        String prompt = String.valueOf(promptMap.get("transformationUserPrompt"));
                        String processedPrompt = processPrompt(prompt, inputResponseJson, base64Activator, placeholderName,
                                "User prompt for ID: " + promptMap.getOrDefault("userUniqueId", "unknown"));
                        promptMap.put("transformationUserPrompt", processedPrompt);
                    }
                }
            }

            // Process transformation system prompts
            if (!transformationSystemPromptsList.isEmpty()) {
                for (Map<String, Object> promptMap : transformationSystemPromptsList) {
                    if (promptMap.containsKey("transformationSystemPrompt")) {
                        String prompt = String.valueOf(promptMap.get("transformationSystemPrompt"));
                        String processedPrompt = processPrompt(prompt, inputResponseJson, base64Activator, placeholderName,
                                "System prompt for ID: " + promptMap.getOrDefault("systemUniqueId", "unknown"));
                        promptMap.put("transformationSystemPrompt", processedPrompt);
                    }
                }
            }
        } else {
            log.info("BBox activator is disabled or process is not RADON_KVP_ACTION. Using original prompts.");
        }

        // Check if KRYPTON_DOUBLE_PASS_MODE is enabled
        if ("KRYPTON_DOUBLE_PASS_MODE".equals(kryptonInferenceMode) && !transformationUserPromptsList.isEmpty()) {
            log.info("KRYPTON_DOUBLE_PASS_MODE detected. Processing in batches.");

            // Get batch size from context or use default
            int batchSize = Integer.parseInt(action.getContext().get("kvp.double.pass.batch.size"));

            // Validate lists have the same size
            if (transformationUserPromptsList.size() != transformationSystemPromptsList.size()) {
                log.warn("User prompts list size ({}) doesn't match system prompts list size ({}). Using minimum size.",
                        transformationUserPromptsList.size(), transformationSystemPromptsList.size());
            }

            int minSize = Math.min(transformationUserPromptsList.size(), transformationSystemPromptsList.size());

            // Split both lists into batches
            List<List<Map<String, Object>>> userPromptBatches = new ArrayList<>();
            List<List<Map<String, Object>>> systemPromptBatches = new ArrayList<>();

            for (int i = 0; i < minSize; i += batchSize) {
                int endIndex = Math.min(i + batchSize, minSize);

                userPromptBatches.add(new ArrayList<>(transformationUserPromptsList.subList(i, endIndex)));
                systemPromptBatches.add(new ArrayList<>(transformationSystemPromptsList.subList(i, endIndex)));
            }

            log.info("Split prompts into {} batches", userPromptBatches.size());

            // Process each batch separately
            for (int batchIndex = 0; batchIndex < userPromptBatches.size(); batchIndex++) {
                List<Map<String, Object>> batchUserPrompts = userPromptBatches.get(batchIndex);
                List<Map<String, Object>> batchSystemPrompts = systemPromptBatches.get(batchIndex);

                log.info("Processing batch {} of {} with {} user prompts and {} system prompts",
                        batchIndex + 1, userPromptBatches.size(),
                        batchUserPrompts.size(), batchSystemPrompts.size());

                // Create a copy of the request for this batch
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
                radonKvpExtractionRequest.setKryptonInferenceMode(kryptonInferenceMode);

                // Set the batch-specific transformation prompts
                radonKvpExtractionRequest.setTransformationUserPrompts(batchUserPrompts);
                radonKvpExtractionRequest.setTransformationSystemPrompts(batchSystemPrompts);

                // Set base64 image if needed
                if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
                    radonKvpExtractionRequest.setBase64Img(fileProcessingUtils.convertFileToBase64(filePath));
                } else {
                    radonKvpExtractionRequest.setBase64Img("");
                }

                // Process the batch request
                sendRequest(endpoint, entity, parentObj, radonKvpExtractionRequest);
            }

            return parentObj;
        } else {
            // Original non-batch processing approach
            log.info("Standard processing mode (non-batched)");

            // Prepare the request payload
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

            // Set the transformed lists into the request object
            radonKvpExtractionRequest.setKryptonInferenceMode(kryptonInferenceMode);
            radonKvpExtractionRequest.setTransformationUserPrompts(transformationUserPromptsList);
            radonKvpExtractionRequest.setTransformationSystemPrompts(transformationSystemPromptsList);

            // Set base64 image if needed
            if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
                radonKvpExtractionRequest.setBase64Img(fileProcessingUtils.convertFileToBase64(filePath));
            } else {
                radonKvpExtractionRequest.setBase64Img("");
            }

            // Process the single request
            sendRequest(endpoint, entity, parentObj, radonKvpExtractionRequest);

            return parentObj;
        }
    }

    /**
     * Send request to endpoint and process response
     *
     * @param endpoint                  The API endpoint
     * @param entity                    The input entity
     * @param parentObj                 The output table list
     * @param radonKvpExtractionRequest The request payload
     * @throws Exception If any error occurs
     */
    private void sendRequest(URL endpoint, RadonQueryInputTable entity, List<RadonQueryOutputTable> parentObj,
                             RadonKvpExtractionRequest radonKvpExtractionRequest) throws Exception {
        // Prepare the request
        String jsonInputRequest = mapper.writeValueAsString(radonKvpExtractionRequest);

        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);
        if (Objects.equals("false", tritonRequestActivator)) {
            log.info("Triton request activator variable: {} value: {}, Copro API running in legacy mode ",
                    TRITON_REQUEST_ACTIVATOR, tritonRequestActivator);
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON))
                    .build();
            coproResponseBuilder(entity, request, parentObj, jsonInputRequest, endpoint);
        } else {
            log.info("Triton request activator variable: {} value: {}, Copro API running in Triton mode ",
                    TRITON_REQUEST_ACTIVATOR, tritonRequestActivator);

            TritonRequest requestBody = new TritonRequest();
            requestBody.setName(entity.getApiName());
            requestBody.setShape(List.of(1, 1));
            requestBody.setDatatype(TritonDataTypes.BYTES.name());
            requestBody.setData(Collections.singletonList(jsonInputRequest));

            TritonInputRequest tritonInputRequest = new TritonInputRequest();
            tritonInputRequest.setInputs(Collections.singletonList(requestBody));
            String jsonRequest = mapper.writeValueAsString(tritonInputRequest);
            log.info(aMarker, " Input variables id : {}", action.getActionId());

            if (log.isInfoEnabled()) {
                log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {}",
                        endpoint, radonKvpExtractionRequest.getInputFilePath());
            }

            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON))
                    .build();
            tritonRequestBuilder(entity, request, parentObj, jsonRequest, endpoint);
        }
    }

    /**
     * Parse JSON string to List<Map<String, Object>>
     */
    private List<Map<String, Object>> parseJsonToList(String jsonString, String logIdentifier) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (jsonString == null || jsonString.trim().isEmpty()) {
            log.warn("{} JSON string is null or empty", logIdentifier);
            return resultList;
        }

        // Try multiple parsing strategies
        Exception lastException = new Exception();
        try {
            // The JSON string might already have escaped quotes (\"foo\":\"bar\")
            // Try uncapping first, then parsing
            String unescaped = jsonString
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");

            resultList = mapper.readValue(unescaped, new TypeReference<>() {
            });
            log.info("Successfully parsed {} JSON after unescaping: {} entries", logIdentifier, resultList.size());
            return resultList;
        } catch (Exception e) {
            log.debug("Unescaped parsing failed: {}", e.getMessage());
        }

        // If we get here, all strategies failed
        log.error("All parsing strategies failed for {} JSON. Last error: {}", logIdentifier, lastException.getMessage());

        // For debugging, log a small portion of the problematic JSON
        if (jsonString.length() > 150) {
            log.debug("First 150 chars of problematic JSON: {}", jsonString.substring(0, 150));
        } else {
            log.debug("Problematic JSON: {}", jsonString);
        }
        return resultList;
    }


    /**
     * Process a prompt with placeholder replacement and base64 encoding if needed
     */
    private String processPrompt(String originalPrompt, String inputResponseJson, String base64Activator,
                                 String placeholderName, String logIdentifier) {
        if (originalPrompt == null || inputResponseJson == null) {
            return originalPrompt;
        }

        String processedPrompt;

        if (Objects.equals(base64Activator, "true")) {
            log.info("Base64 activator is turned ON for {}", logIdentifier);
            try {
                Base64toActualVaue base64Caller = new Base64toActualVaue();
                String base64Value = base64Caller.base64toActual(originalPrompt);

                byte[] decodedBytes = Base64.getDecoder().decode(base64Value);
                String decodedPrompt = new String(decodedBytes);

                log.info("Decoded {} before replacing placeholder", logIdentifier);

                String updatedPrompt = decodedPrompt.replace(placeholderName, inputResponseJson);
                processedPrompt = Base64.getEncoder().encodeToString(updatedPrompt.getBytes());

                log.info("{} processed with base64 encoding", logIdentifier);
            } catch (Exception e) {
                log.error("Error processing base64 for {}: {}", logIdentifier, e.getMessage(), e);
                processedPrompt = originalPrompt;
            }
        } else {
            log.info("Base64 activator is OFF for {}. Using plain text.", logIdentifier);
            processedPrompt = originalPrompt.replace(placeholderName, inputResponseJson);
            log.info("{} processed in plain text", logIdentifier);
        }

        return processedPrompt;
    }

    private void tritonRequestBuilder(RadonQueryInputTable entity, Request request, List<RadonQueryOutputTable> parentObj, String jsonRequest, URL endpoint) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();


        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                String responseBody = response.body().string();
                RadonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, RadonKvpExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(radonDataItem -> {
                        try {
                            extractTritonOutputDataResponse(entity, radonDataItem, parentObj, jsonRequest, responseBody, endpoint.toString());
                        } catch (IOException e) {
                            HandymanException handymanException = new HandymanException(e);
                            HandymanException.insertException("radon kvp consumer failed for batch/group " + groupId, handymanException, this.action);
                            log.error(aMarker, "The Exception occurred in converting the response from triton server output {}", ExceptionUtil.toString(e));
                        }
                    }));

                }
            } else {
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
                        .message(response.message())
                        .batchId(entity.getBatchId())
                        .createdOn(entity.getCreatedOn())
                        .createdUserId(tenantId)
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .lastUpdatedUserId(tenantId)
                        .category(entity.getCategory())
                        .request(jsonRequest)
                        .response(response.message())
                        .endpoint(String.valueOf(endpoint))
                        .build());
                log.info(aMarker, "Error in getting response from triton response {}", response.message());
            }
        } catch (IOException e) {
            parentObj.add(RadonQueryOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .inputFilePath(entity.getInputFilePath())
                    .tenantId(tenantId)
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .actionId(action.getActionId())
                    .process(entity.getProcess())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(entity.getApiName())
                    .message(ExceptionUtil.toString(e))
                    .batchId(entity.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .createdUserId(tenantId)
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .lastUpdatedUserId(tenantId)
                    .category(entity.getCategory())
                    .build());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("radon kvp consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response  from triton server {}", ExceptionUtil.toString(e));
        }
    }

    private void extractTritonOutputDataResponse(RadonQueryInputTable entity, String radonDataItem, List<RadonQueryOutputTable> parentObj, String request, String response, String endpoint) throws IOException {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();

        // Parse JSON input
        RadonKvpLineItem modelResponse = mapper.readValue(radonDataItem, RadonKvpLineItem.class);

        // Encryption setting
        String encryptOutputJsonContent = action.getContext().get("pipeline.end.to.end.encryption");
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action);

        JsonNode extractedContent = modelResponse.getInferResponse();
        String finalResponseJson = "{}";

        ObjectMapper objectMapper = new ObjectMapper();

        if (extractedContent == null || extractedContent.isEmpty()) {
            throw new IOException("InferResponse is null or empty");
        }

        try {
            // Convert JsonNode to String before passing to JSONObject
            String jsonString = objectMapper.writeValueAsString(extractedContent);

            // Now create JSONObject from a valid JSON string
            JSONObject inferResponseObj = new JSONObject(jsonString);

            // Extract `finalResponse`
            if (inferResponseObj.has("finalResponse")) {
                finalResponseJson = inferResponseObj.getJSONArray("finalResponse").toString();
            }

        } catch (Exception e) {
            throw new IOException("Invalid JSON format in inferResponse", e);
        }

        // Encrypt only finalResponse if encryption is enabled
        if (Objects.equals(encryptOutputJsonContent, "true")) {
            finalResponseJson = encryption.encrypt(finalResponseJson, "AES256", "RADON_KVP_JSON");
        }

        // Add processed data to parentObj
        parentObj.add(RadonQueryOutputTable.builder()
                .createdOn(CreateTimeStamp.currentTimestamp())
                .createdUserId(tenantId)
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(tenantId)
                .originId(originId)
                .paperNo(paperNo)
                .totalResponseJson(finalResponseJson) // Stores only "finalResponse"
                .groupId(groupId)
                .inputFilePath(processedFilePaths)
                .actionId(action.getActionId())
                .tenantId(tenantId)
                .processId(processId)
                .rootPipelineId(rootPipelineId)
                .process(entity.getProcess())
                .batchId(modelResponse.getBatchId()) // Ensure correct batch ID
                .modelRegistry(entity.getModelRegistry())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(entity.getApiName())
                .category(entity.getCategory())
                .message("Radon kvp action macro completed")
                .request(request)
                .response(response) // Stores complete response including vision responses
                .sorContainerId(entity.getSorContainerId())
                .endpoint(endpoint)
                .build());
    }


    private void coproResponseBuilder(RadonQueryInputTable entity, Request request, List<RadonQueryOutputTable> parentObj, String jsonInputRequest, URL endpoint) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                String responseBody = response.body().string();
                RadonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, RadonKvpExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(radonDataItem -> {
                        try {
                            extractedCoproOutputResponse(entity, radonDataItem, parentObj, jsonInputRequest, responseBody, endpoint.toString());
                        } catch (JsonProcessingException e) {
                            HandymanException handymanException = new HandymanException(e);
                            HandymanException.insertException("radon kvp consumer failed for batch/group " + groupId, handymanException, this.action);
                            log.error(aMarker, "The Exception occurred in converting response from triton server output {}", ExceptionUtil.toString(e));
                        }
                    }));

                }
            } else {
                parentObj.add(RadonQueryOutputTable.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .inputFilePath(entity.getInputFilePath())
                        .actionId(action.getActionId())
                        .tenantId(tenantId)
                        .processId(processId)
                        .rootPipelineId(rootPipelineId)
                        .process(entity.getProcess())
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(entity.getApiName())
                        .message(response.message())
                        .batchId(entity.getBatchId())
                        .category(entity.getCategory())
                        .request(jsonInputRequest)
                        .response(response.message())
                        .endpoint(String.valueOf(endpoint))
                        .sorContainerId(entity.getSorContainerId())
                        .build());
                log.info(aMarker, "Error in converting response from copro server {}", response.message());
            }
        } catch (IOException e) {
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
                    .message(ExceptionUtil.toString(e))
                    .batchId(entity.getBatchId())
                    .category(entity.getCategory())
                    .sorContainerId(entity.getSorContainerId())
                    .build());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Radon kvp action consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response from copro server {}", ExceptionUtil.toString(e));
        }
    }

    private void extractedCoproOutputResponse(RadonQueryInputTable entity, String radonDataItem, List<RadonQueryOutputTable> parentObj, String request, String response, String endpoint) throws JsonProcessingException {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();

        RadonKvpLineItem modelResponse = mapper.readValue(radonDataItem, RadonKvpLineItem.class);

        parentObj.add(RadonQueryOutputTable.builder()
                .createdOn(entity.getCreatedOn())
                .createdUserId(tenantId)
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(tenantId)
                .originId(originId)
                .paperNo(paperNo)
                .totalResponseJson(String.valueOf(modelResponse.getInferResponse()))
                .groupId(groupId)
                .inputFilePath(processedFilePaths)
                .actionId(action.getActionId())
                .tenantId(tenantId)
                .processId(processId)
                .rootPipelineId(rootPipelineId)
                .modelRegistry(entity.getModelRegistry())
                .process(entity.getProcess())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(entity.getApiName())
                .batchId(entity.getBatchId())
                .message("Radon kvp action macro completed")
                .category(entity.getCategory())
                .request(request)
                .response(response)
                .endpoint(String.valueOf(endpoint))
                .sorContainerId(entity.getSorContainerId())
                .build()
        );
    }

}
