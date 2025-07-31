package in.handyman.raven.lib.model.deep.sift;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.lib.replicate.ReplicateRequest;
import in.handyman.raven.lib.replicate.ReplicateResponse;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.core.utils.ProcessFileFormatE;
import in.handyman.raven.util.ExceptionUtil;
import in.handyman.raven.lib.TestDataExtractorAction;
import in.handyman.raven.lib.model.TestDataExtractor;
import org.slf4j.Logger;
import org.slf4j.Marker;
import okhttp3.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;
import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_TEXT_EXTRACTION_OUTPUT;

public class DeepSiftConsumerProcess implements CoproProcessor.ConsumerProcess<DeepSiftInputTable, DeepSiftOutputTable> {
    public static final String DEEP_SIFT_START = "DEEP SIFT START";
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    public static final String KRYPTON_START = "KRYPTON START";
    public static final String REQUEST_ACTIVATOR_HANDLER_NAME = "copro.request.deep.sift.handler.name";
    public static final String REPLICATE_API_TOKEN_CONTEXT = "replicate.request.api.token";
    public static final String REPLICATE_DEEP_SIFT_VERSION = "replicate.deep.sift.version";
    public static final String DEEP_SIFT_MODEL_NAME = "preprocess.deep.sift.model.name";

    public static final String PAGE_CONTENT_NO = "no";
    public static final String PAGE_CONTENT_YES = "yes";
    private final int pageContentMinLength;
    private final Logger log;
    private final Marker aMarker;
    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private static final String PROCESS_NAME = "DEEP_SIFT";
    private final OkHttpClient httpclient;

    private static final String COPRO_SOCKET_TIMEOUT = "copro.client.socket.timeout";
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String processBase64;
    private final FileProcessingUtils fileProcessingUtils;

    public DeepSiftConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, Integer pageContentMinLength, FileProcessingUtils fileProcessingUtils, String processBase64) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        int timeOut = Integer.parseInt(this.action.getContext().getOrDefault(COPRO_SOCKET_TIMEOUT, "100"));
        this.pageContentMinLength = pageContentMinLength;
        this.processBase64 = processBase64;
        this.fileProcessingUtils = fileProcessingUtils;
        this.httpclient = new OkHttpClient.Builder().connectTimeout(timeOut, TimeUnit.MINUTES).writeTimeout(timeOut, TimeUnit.MINUTES).readTimeout(timeOut, TimeUnit.MINUTES).build();
    }

    @Override
    public List<DeepSiftOutputTable> process(URL endpoint, DeepSiftInputTable entity) throws IOException {
        List<DeepSiftOutputTable> parentObj = new ArrayList<>();

        String coproHandlerName = action.getContext().get(REQUEST_ACTIVATOR_HANDLER_NAME);
        String replicateApiToken = action.getContext().get(REPLICATE_API_TOKEN_CONTEXT);
        String DeepSiftModelName = action.getContext().get(DEEP_SIFT_MODEL_NAME);

        String inputFilePath = entity.getFilePath();
        Long rootPipelineId = entity.getRootPipelineId();
        String filePath = String.valueOf(entity.getFilePath());
        ObjectMapper objectMapper = new ObjectMapper();
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        Integer paperNumber = entity.getPaperNo();
        String processId = String.valueOf(entity.getProcessId());
        Long tenantId = entity.getTenantId();
        Long actionId = action.getActionId();
        String batchId = entity.getBatchId();

        // Payload
        DeepSiftData DeepSiftData = new DeepSiftData();
        DeepSiftData.setOriginId(originId);
        DeepSiftData.setGroupId(groupId);
        DeepSiftData.setProcessId(Long.valueOf(processId));
        DeepSiftData.setTenantId(tenantId);
        DeepSiftData.setRootPipelineId(rootPipelineId);
        DeepSiftData.setActionId(actionId);
        DeepSiftData.setPaperNumber(paperNumber);
        DeepSiftData.setTemplateName(entity.getTemplateName());
        DeepSiftData.setProcess(PROCESS_NAME);
        DeepSiftData.setInputFilePath(filePath);
        DeepSiftData.setBatchId(batchId);
        DeepSiftData.setBase64Img(entity.getBase64Img());

        String jsonInputRequest = objectMapper.writeValueAsString(DeepSiftData);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName(DEEP_SIFT_START);
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype(TritonDataTypes.BYTES.name());
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been built with the parameters \n URI : {}, with inputFilePath {} ", endpoint, inputFilePath);
        }

        if (Objects.equals("COPRO", coproHandlerName)) {
            if (log.isInfoEnabled()) {
                log.info(aMarker, "Executing COPRO handler for endpoint: {}", endpoint);
            }
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, mediaType)).build();
            coproRequestBuilder(entity, request, parentObj, originId, groupId, jsonInputRequest, endpoint);
        } else if (Objects.equals("REPLICATE", coproHandlerName)) {
            if (DeepSiftModelName.equals(ModelRegistry.ARGON.name())) {
                if (log.isInfoEnabled()) {
                    log.info(aMarker, "Executing REPLICATE handler for endpoint: {} and model: {}", endpoint, DeepSiftModelName);
                }
                String base64ForPath = getBase64ForPath(DeepSiftData.getInputFilePath());
                DeepSiftData.setBase64Img(base64ForPath);

                ReplicateRequest replicateRequest = new ReplicateRequest();
                replicateRequest.setInput(DeepSiftData);

                String replicateJsonRequest = objectMapper.writeValueAsString(replicateRequest);
                Request request = new Request.Builder().url(endpoint).post(RequestBody.create(replicateJsonRequest, mediaType)).addHeader("Authorization", "Bearer " + replicateApiToken).addHeader("Content-Type", "application/json").addHeader("Prefer", "wait").build();
                replicateResponseBuilder(endpoint, request, parentObj, entity, replicateJsonRequest);
            } else if (DeepSiftModelName.equals(ModelRegistry.KRYPTON.name())) {
                if (log.isInfoEnabled()) {
                    log.info(aMarker, "Executing REPLICATE handler for endpoint: {} and model: {}", endpoint, DeepSiftModelName);
                }
                String base64ForPath = getBase64ForPath(DeepSiftData.getInputFilePath());

                DeepSiftRequest kryptonRequestPayloadFromQuery = getKryptonRequestPayloadFromQuery(entity);
                kryptonRequestPayloadFromQuery.setBase64Img(base64ForPath);

                String DeepSiftPayloadString = mapper.writeValueAsString(kryptonRequestPayloadFromQuery);

                ReplicateRequest replicateRequest = new ReplicateRequest();
                replicateRequest.setInput(DeepSiftPayloadString);

                String replicateJsonRequest = objectMapper.writeValueAsString(replicateRequest);

                Request request = new Request.Builder().url(endpoint).post(RequestBody.create(replicateJsonRequest, mediaType)).addHeader("Authorization", "Bearer " + replicateApiToken).addHeader("Content-Type", "application/json").addHeader("Prefer", "wait").build();
                tritonRequestKryptonExecutor(entity, request, parentObj, DeepSiftPayloadString, endpoint);
            }
        } else if (Objects.equals("TRITON", coproHandlerName)) {
            log.info(aMarker, "Executing TRITON handler for endpoint: {} and model: {}", endpoint, DeepSiftModelName);
            if (DeepSiftModelName.equals(ModelRegistry.ARGON.name())) {
                DeepSiftData DeepSiftPayload = getArgonRequestPayloadFromEntity(entity);
                if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
                    DeepSiftPayload.setBase64Img(fileProcessingUtils.convertFileToBase64(filePath));
                } else {
                    DeepSiftPayload.setBase64Img("");
                }
                String DeepSiftPayloadString = mapper.writeValueAsString(DeepSiftPayload);

                String jsonRequestTritonArgon = getTritonRequestPayload(DeepSiftPayloadString, DEEP_SIFT_START, mapper);

                Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequestTritonArgon, mediaType)).build();
                tritonRequestArgonExecutor(entity, request, parentObj, jsonRequestTritonArgon, endpoint);
            } else if (DeepSiftModelName.equals(ModelRegistry.KRYPTON.name())) {
                DeepSiftRequest kryptonRequestPayloadFromQuery = getKryptonRequestPayloadFromQuery(entity);
                if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
                    kryptonRequestPayloadFromQuery.setBase64Img(fileProcessingUtils.convertFileToBase64(filePath));
                } else {
                    kryptonRequestPayloadFromQuery.setBase64Img("");
                }
                String DeepSiftPayloadString = mapper.writeValueAsString(kryptonRequestPayloadFromQuery);

                String jsonRequestTritonKrypton = getTritonRequestPayload(DeepSiftPayloadString, KRYPTON_START, mapper);

                Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequestTritonKrypton, mediaType)).build();
                tritonRequestKryptonExecutor(entity, request, parentObj, jsonRequestTritonKrypton, endpoint);
            }
        } else if (Objects.equals("TEST4J", coproHandlerName)) {
            log.info(aMarker, "Executing TEST4J handler for endpoint: {} and model: {}", endpoint, DeepSiftModelName);

            // Build DeepSiftRequest from entity
            DeepSiftRequest test4jRequest = getKryptonRequestPayloadFromQuery(entity);

            String deepSiftPayloadString;
            try {
                deepSiftPayloadString = mapper.writeValueAsString(test4jRequest);
            } catch (JsonProcessingException e) {
                log.error(aMarker, "Failed to serialize DeepSiftRequest for TEST4J", e);
                throw new HandymanException("Error serializing request for TEST4J", e, action);
            }

            try {
                // Prepare file for TestDataExtractor
                File inputFile = new File(filePath);
                if (!inputFile.exists()) {
                    throw new HandymanException("Input file does not exist: " + filePath);
                }
                byte[] fileContent = Files.readAllBytes(inputFile.toPath());
                MultipartFile multipartFile = new MockMultipartFile(
                        inputFile.getName(),
                        inputFile.getName(),
                        "application/pdf",
                        fileContent
                );

                // Create TestDataExtractor instance
                TestDataExtractor testDataExtractor = TestDataExtractor.builder()
                        .name("DeepSiftTest4J")
                        .mode("keywords")
                        .files(Collections.singletonList(multipartFile))
                        .outputPath(System.getProperty("java.io.tmpdir"))
                        .condition(true)
                        .build();

                // Execute TestDataExtractorAction
                TestDataExtractorAction extractorAction = new TestDataExtractorAction(action, log, testDataExtractor);
                extractorAction.execute();

                // Read the extracted text from the output file
                String outputFilePath = testDataExtractor.getOutputPath() + File.separator + inputFile.getName() + ".txt";
                File outputFile = new File(outputFilePath);
                if (!outputFile.exists()) {
                    throw new HandymanException("Output file not generated: " + outputFilePath);
                }
                String extractedText = Files.readString(outputFile.toPath());

                // Process the extracted text as a single page (assuming single-page output for simplicity)
                int pageNumber = paperNumber != null ? paperNumber : 1;
                String flag = (extractedText.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;
                String encryptSotPageContent = action.getContext().get(ENCRYPT_TEXT_EXTRACTION_OUTPUT);
                String extractedContentEnc = extractedText;
                InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);

                if (Objects.equals(encryptSotPageContent, "true")) {
                    extractedContentEnc = encryption.encrypt(extractedText, "AES256", "TEXT_DATA");
                }

                // Build JSON response to mimic original API response structure
                String jsonResponse = String.format(
                        "{\"payload\":{\"documents\":[{\"fileName\":\"%s\",\"textByPage\":{\"page%d\":\"%s\"}}]}}",
                        inputFile.getName(),
                        pageNumber,
                        extractedText.replace("\"", "\\\"")
                );

                parentObj.add(DeepSiftOutputTable.builder()
                        .filePath(entity.getFilePath())
                        .extractedText(extractedContentEnc)
                        .originId(entity.getOriginId())
                        .groupId(entity.getGroupId())
                        .paperNo(pageNumber)
                        .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .message("TEST4J processing completed for page " + pageNumber)
                        .createdOn(entity.getCreatedOn())
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .isBlankPage(flag)
                        .tenantId(entity.getTenantId())
                        .templateId(entity.getTemplateId())
                        .processId(entity.getProcessId())
                        .templateName(entity.getTemplateName())
                        .rootPipelineId(entity.getRootPipelineId())
                        .modelName(DeepSiftModelName)
                        .modelVersion("")
                        .batchId(entity.getBatchId())
                        .request(encryptRequestResponse(deepSiftPayloadString))
                        .response(encryptRequestResponse(jsonResponse))
                        .endpoint(endpoint.toString())
                        .build());

                // Clean up temporary output file
                outputFile.delete();

            } catch (Exception e) {
                parentObj.add(DeepSiftOutputTable.builder()
                        .batchId(entity.getBatchId())
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .groupId(entity.getGroupId())
                        .paperNo(entity.getPaperNo())
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .tenantId(entity.getTenantId())
                        .templateId(entity.getTemplateId())
                        .processId(entity.getProcessId())
                        .message(ExceptionUtil.toString(e))
                        .createdOn(entity.getCreatedOn())
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .rootPipelineId(entity.getRootPipelineId())
                        .templateName(entity.getTemplateName())
                        .request(encryptRequestResponse(deepSiftPayloadString))
                        .response("Error in TEST4J processing")
                        .endpoint(endpoint.toString())
                        .build());
                log.error(aMarker, "Exception in TEST4J handler", e);
                HandymanException handymanException = new HandymanException("TEST4J processing failed", e, action);
                HandymanException.insertException("Deep sift TEST4J failed for origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, action);
                throw handymanException;
            }
        }
        return parentObj;
    }

    private DeepSiftRequest getKryptonRequestPayloadFromQuery(DeepSiftInputTable entity) {
        DeepSiftRequest DeepSiftRequest = new DeepSiftRequest();
        DeepSiftRequest.setOriginId(entity.getOriginId());
        DeepSiftRequest.setProcessId(entity.getProcessId());
        DeepSiftRequest.setTenantId(entity.getTenantId());
        DeepSiftRequest.setRootPipelineId(entity.getRootPipelineId());
        DeepSiftRequest.setActionId(action.getActionId());
        DeepSiftRequest.setProcess(PROCESS_NAME);
        DeepSiftRequest.setInputFilePath(entity.getFilePath());
        DeepSiftRequest.setBatchId(entity.getBatchId());
        DeepSiftRequest.setUserPrompt(entity.getUserPrompt());
        DeepSiftRequest.setSystemPrompt(entity.getSystemPrompt());
        DeepSiftRequest.setPaperNo(entity.getPaperNo());
        DeepSiftRequest.setGroupId(Long.valueOf(entity.getGroupId()));

        return DeepSiftRequest;
    }

    private void tritonRequestKryptonExecutor(DeepSiftInputTable entity, Request request, List<DeepSiftOutputTable> parentObj, String jsonRequest, URL endpoint) {
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        Long rootPipelineId = entity.getRootPipelineId();
        String templateName = entity.getTemplateName();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            if (response.isSuccessful()) {
                DeepSiftResponse modelResponse = mapper.readValue(responseBody, DeepSiftResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(s -> {
                            try {
                                extractedKryptonOutputDataRequest(entity, s, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion(), jsonRequest, responseBody, endpoint.toString());
                            } catch (JsonProcessingException e) {
                                HandymanException handymanException = new HandymanException("Unsuccessful response code: " + response.code() + " message: " + response.message());
                                HandymanException.insertException("Deep sift consumer failed for origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
                                throw new HandymanException("Exception in extracted output Data request {}", e);
                            }
                        });
                    });
                }
            } else {
                parentObj.add(DeepSiftOutputTable.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonRequest)).response(encryptRequestResponse(responseBody)).endpoint(String.valueOf(endpoint)).build());
                HandymanException handymanException = new HandymanException("Unsuccessful response code: " + response.code() + " message: " + response.message());
                HandymanException.insertException("Deep sift consumer failed for origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
                log.error(aMarker, "The Exception occurred in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(DeepSiftOutputTable.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).message(ExceptionUtil.toString(e)).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonRequest)).response("Error In Response").endpoint(String.valueOf(endpoint)).build());
            log.error(aMarker, "The Exception occurred ", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Deep sift consumer failed for origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
        }
    }

    private void extractedKryptonOutputDataRequest(DeepSiftInputTable entity, String stringDataItem, List<DeepSiftOutputTable> parentObj, String modelName, String modelVersion, String request, String response, String endpoint) throws JsonProcessingException {
        DeepSiftLineItem DeepSiftDataItem = mapper.readValue(stringDataItem, DeepSiftLineItem.class);
        String extractedContent;
        final String flag = (DeepSiftDataItem.getInferResponse().length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;
        String encryptSotPageContent = action.getContext().get(ENCRYPT_TEXT_EXTRACTION_OUTPUT);
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);

        if (Objects.equals(encryptSotPageContent, "true")) {
            extractedContent = encryption.encrypt(DeepSiftDataItem.getInferResponse(), "AES256", "TEXT_DATA");
        } else {
            extractedContent = DeepSiftDataItem.getInferResponse();
        }

        String templateId = entity.getTemplateId();
        parentObj.add(DeepSiftOutputTable.builder().filePath(entity.getFilePath()).extractedText(extractedContent).originId(DeepSiftDataItem.getOriginId()).groupId(Math.toIntExact(DeepSiftDataItem.getGroupId())).paperNo(DeepSiftDataItem.getPaperNo()).status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).stage(PROCESS_NAME).message("Deep sift macro completed with krypton triton api call").createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).isBlankPage(flag).tenantId(DeepSiftDataItem.getTenantId()).templateId(templateId).processId(DeepSiftDataItem.getProcessId()).templateName(entity.getTemplateName()).rootPipelineId(DeepSiftDataItem.getRootPipelineId()).modelName(modelName).modelVersion(modelVersion).batchId(entity.getBatchId()).request(encryptRequestResponse(request)).response(encryptRequestResponse(response)).endpoint(String.valueOf(endpoint)).build());
    }

    private void extractedArgonOutputDataRequest(DeepSiftInputTable entity, String stringDataItem, List<DeepSiftOutputTable> parentObj, String modelName, String modelVersion, String request, String response, String endpoint) throws JsonProcessingException {
        DeepSiftDataItem DeepSiftDataItem = mapper.readValue(stringDataItem, DeepSiftDataItem.class);
        final String contentString = Optional.of(DeepSiftDataItem.getPageContent()).map(String::valueOf).orElse(null);
        final String flag = (contentString.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;
        String extractedContent;
        String encryptSotPageContent = action.getContext().get(ENCRYPT_TEXT_EXTRACTION_OUTPUT);
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);

        if (Objects.equals(encryptSotPageContent, "true")) {
            extractedContent = encryption.encrypt(contentString, "AES256", "TEXT_DATA");
        } else {
            extractedContent = contentString;
        }

        String templateId = entity.getTemplateId();
        parentObj.add(DeepSiftOutputTable.builder().batchId(entity.getBatchId()).filePath(DeepSiftDataItem.getInputFilePath()).extractedText(extractedContent).originId(DeepSiftDataItem.getOriginId()).groupId(DeepSiftDataItem.getGroupId()).paperNo(DeepSiftDataItem.getPaperNumber()).status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).stage(PROCESS_NAME).message("Deep sift macro completed").createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).isBlankPage(flag).tenantId(DeepSiftDataItem.getTenantId()).templateId(templateId).processId(DeepSiftDataItem.getProcessId()).templateName(DeepSiftDataItem.getTemplateName()).rootPipelineId(DeepSiftDataItem.getRootPipelineId()).modelName(modelName).modelVersion(modelVersion).batchId(DeepSiftDataItem.getBatchId()).request(encryptRequestResponse(request)).response(encryptRequestResponse(response)).endpoint(String.valueOf(endpoint)).build());
    }

    private void tritonRequestArgonExecutor(DeepSiftInputTable entity, Request request, List<DeepSiftOutputTable> parentObj, String jsonRequest, URL endpoint) {
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        Long rootPipelineId = entity.getRootPipelineId();
        String templateName = entity.getTemplateName();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            if (response.isSuccessful()) {
                ObjectMapper objectMappers = new ObjectMapper();
                DeepSiftResponse modelResponse = objectMappers.readValue(responseBody, DeepSiftResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(s -> {
                            try {
                                extractedArgonOutputDataRequest(entity, s, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion(), jsonRequest, responseBody, endpoint.toString());
                            } catch (JsonProcessingException e) {
                                throw new HandymanException("Exception in extracted output Data request {}", e);
                            }
                        });
                    });
                }
            } else {
                parentObj.add(DeepSiftOutputTable.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonRequest)).response(encryptRequestResponse(responseBody)).endpoint(String.valueOf(endpoint)).build());
                HandymanException handymanException = new HandymanException("Unsuccessful response code: " + response.code() + " message: " + response.message());
                HandymanException.insertException("Deep sift consumer failed for origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
                log.error(aMarker, "The Exception occurred ");
            }
        } catch (Exception e) {
            parentObj.add(DeepSiftOutputTable.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).message(ExceptionUtil.toString(e)).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonRequest)).response("Error In getting Response").endpoint(String.valueOf(endpoint)).build());
            log.error(aMarker, "The Exception occurred ", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Deep sift consumer failed for origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
        }
    }

    private static String getTritonRequestPayload(String DeepSiftPayloadString, String name, ObjectMapper objectMapper) throws JsonProcessingException {
        TritonRequest tritonRequestPayload = new TritonRequest();
        tritonRequestPayload.setName(name);
        tritonRequestPayload.setShape(List.of(1, 1));
        tritonRequestPayload.setDatatype(TritonDataTypes.BYTES.name());
        tritonRequestPayload.setData(Collections.singletonList(DeepSiftPayloadString));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(tritonRequestPayload));

        return objectMapper.writeValueAsString(tritonInputRequest);
    }

    private DeepSiftData getArgonRequestPayloadFromEntity(DeepSiftInputTable entity) {
        DeepSiftData DeepSiftData = new DeepSiftData();
        DeepSiftData.setOriginId(entity.getOriginId());
        DeepSiftData.setGroupId(entity.getGroupId());
        DeepSiftData.setProcessId(entity.getProcessId());
        DeepSiftData.setTenantId(entity.getTenantId());
        DeepSiftData.setRootPipelineId(entity.getRootPipelineId());
        DeepSiftData.setActionId(action.getActionId());
        DeepSiftData.setPaperNumber(entity.getPaperNo());
        DeepSiftData.setTemplateName(entity.getTemplateName());
        DeepSiftData.setProcess(PROCESS_NAME);
        DeepSiftData.setInputFilePath(entity.getFilePath());
        DeepSiftData.setBatchId(entity.getBatchId());

        return DeepSiftData;
    }

    private void replicateResponseBuilder(URL endpoint, Request request, List<DeepSiftOutputTable> parentObj, DeepSiftInputTable entity, String replicateJsonRequest) {
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            if (response.isSuccessful()) {
                ReplicateResponse replicateResponse = mapper.readValue(responseBody, ReplicateResponse.class);
                if (!replicateResponse.getOutput().isEmpty() && !replicateResponse.getOutput().isNull()) {
                    extractedReplicateOutputResponse(endpoint, entity, replicateResponse, parentObj, replicateJsonRequest, responseBody);
                } else {
                    parentObj.add(DeepSiftOutputTable.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(entity.getTenantId()).templateId(entity.getTemplateId()).processId(entity.getProcessId()).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(entity.getRootPipelineId()).templateName(entity.getTemplateName()).request(encryptRequestResponse(replicateJsonRequest)).response(encryptRequestResponse(responseBody)).endpoint(String.valueOf(endpoint)).build());
                    HandymanException handymanException = new HandymanException("Unsuccessful response code: " + response.code() + " message: " + response.message());
                    HandymanException.insertException("Deep sift replicate consumer failed for origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
                    log.error(aMarker, "The replicate response has empty output {}", replicateResponse.getOutput());
                }
            } else {
                parentObj.add(DeepSiftOutputTable.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(entity.getTenantId()).templateId(entity.getTemplateId()).processId(entity.getProcessId()).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(entity.getRootPipelineId()).templateName(entity.getTemplateName()).request(encryptRequestResponse(replicateJsonRequest)).response(encryptRequestResponse(responseBody)).endpoint(String.valueOf(endpoint)).build());
                HandymanException handymanException = new HandymanException("Unsuccessful response code: " + response.code() + " message: " + response.message());
                HandymanException.insertException("Deep sift consumer failed for origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
                log.error(aMarker, "The replicate response status {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(DeepSiftOutputTable.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(entity.getTenantId()).templateId(entity.getTemplateId()).processId(entity.getProcessId()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).message(ExceptionUtil.toString(e)).rootPipelineId(entity.getRootPipelineId()).templateName(entity.getTemplateName()).request(encryptRequestResponse(request.toString())).response("Error In getting replicate Response").endpoint(String.valueOf(endpoint)).build());
            log.error(aMarker, "The Exception occurred in replicate {} ", e.toString());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Deep sift consumer failed for replicate origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
        }
    }

    public String getBase64ForPath(String imagePath) throws IOException {
        String base64Image = new String();
        try {
            byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
            base64Image = Base64.getEncoder().encodeToString(imageBytes);
            log.info(aMarker, "Base64 created for this file {}", imagePath);
        } catch (Exception e) {
            log.error(aMarker, "Error occurred in creating base64 {}", ExceptionUtil.toString(e));
            throw new HandymanException("Error occurred in creating base64 {}", e, action);
        }
        return base64Image;
    }

    private void extractedReplicateOutputResponse(URL endpoint, DeepSiftInputTable entity, ReplicateResponse replicateResponse, List<DeepSiftOutputTable> parentObj, String replicateJsonRequest, String replicateJsonResponse) throws JsonProcessingException {
        DeepSiftDataItem DeepSiftDataItem = mapper.treeToValue(replicateResponse.getOutput(), DeepSiftDataItem.class);
        final String contentString = Optional.of(DeepSiftDataItem.getPageContent()).map(String::valueOf).orElse(null);
        final String flag = (!Objects.isNull(contentString) && contentString.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;
        Integer paperNo = entity.getPaperNo();
        String filePath = entity.getFilePath();
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        String templateName = entity.getTemplateName();
        Long rootPipelineId = entity.getRootPipelineId();
        String batchId = entity.getBatchId();
        String encryptSotPageContent = action.getContext().get(ENCRYPT_TEXT_EXTRACTION_OUTPUT);
        String extractedContentEnc;
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);

        if (Objects.equals(encryptSotPageContent, "true")) {
            extractedContentEnc = encryption.encrypt(contentString, "AES256", "TEXT_DATA");
        } else {
            extractedContentEnc = contentString;
        }

        parentObj.add(DeepSiftOutputTable.builder().filePath(new File(filePath).getAbsolutePath()).extractedText(extractedContentEnc).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(paperNo).status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).stage(PROCESS_NAME).message("Deep sift action api call completed").createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).isBlankPage(flag).tenantId(tenantId).templateId(templateId).processId(processId).templateName(templateName).rootPipelineId(rootPipelineId).modelName(replicateResponse.getModel()).modelVersion(replicateResponse.getVersion()).batchId(batchId).request(encryptRequestResponse(replicateJsonRequest)).response(encryptRequestResponse(replicateJsonResponse)).endpoint(endpoint.toString()).build());
    }

    private void coproRequestBuilder(DeepSiftInputTable entity, Request request, List<DeepSiftOutputTable> parentObj, String originId, Integer groupId, String jsonInputRequest, URL endpoint) {
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        Long rootPipelineId = entity.getRootPipelineId();
        String templateName = entity.getTemplateName();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            if (response.isSuccessful()) {
                extractedCoproOutputResponse(entity, responseBody, parentObj, originId, groupId, "", "", jsonInputRequest, responseBody, endpoint.toString());
            } else {
                parentObj.add(DeepSiftOutputTable.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonInputRequest)).response(encryptRequestResponse(responseBody)).endpoint(String.valueOf(endpoint)).build());
                HandymanException handymanException = new HandymanException("Unsuccessful response code: " + response.code() + " message: " + response.message());
                HandymanException.insertException("Deep sift consumer failed for origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
                log.error(aMarker, "The Exception occurred in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(DeepSiftOutputTable.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).message(ExceptionUtil.toString(e)).rootPipelineId(rootPipelineId).templateName(templateName).response("Error In Response").endpoint(String.valueOf(endpoint)).build());
            log.error(aMarker, "The Exception occurred in Copro {} ", e.toString());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Deep sift consumer failed for origin Id " + originId + " paper no " + entity.getPaperNo(), handymanException, this.action);
        }
    }

    private void extractedCoproOutputResponse(DeepSiftInputTable entity, String stringDataItem, List<DeepSiftOutputTable> parentObj, String originId, Integer groupId, String modelName, String modelVersion, String request, String response, String endpoint) {
        String parentResponseObject = extractPageContent(stringDataItem);
        final String contentString = Optional.of(parentResponseObject).map(String::valueOf).orElse(null);
        final String flag = (!Objects.isNull(contentString) && contentString.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;
        Integer paperNo = entity.getPaperNo();
        String filePath = entity.getFilePath();
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        String templateName = entity.getTemplateName();
        Long rootPipelineId = entity.getRootPipelineId();
        String batchId = entity.getBatchId();
        String encryptSotPageContent = action.getContext().get(ENCRYPT_TEXT_EXTRACTION_OUTPUT);
        String extractedContentEnc;
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);

        if (Objects.equals(encryptSotPageContent, "true")) {
            extractedContentEnc = encryption.encrypt(contentString, "AES256", "TEXT_DATA");
        } else {
            extractedContentEnc = contentString;
        }

        parentObj.add(DeepSiftOutputTable.builder().batchId(entity.getBatchId()).filePath(new File(filePath).getAbsolutePath()).extractedText(extractedContentEnc).originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).paperNo(paperNo).status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).stage(PROCESS_NAME).message("Deep sift macro completed").createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).isBlankPage(flag).tenantId(tenantId).templateId(templateId).processId(processId).templateName(templateName).rootPipelineId(rootPipelineId).modelName(modelName).modelVersion(modelVersion).batchId(batchId).request(encryptRequestResponse(request)).response(encryptRequestResponse(response)).endpoint(endpoint).build());
    }

    public JsonNode convertFormattedJsonStringToJsonNode(String jsonResponse, ObjectMapper objectMapper) {
        try {
            if (jsonResponse.contains("```json")) {
                log.info("Input contains the required ```json``` markers. So processing it based on the ```json``` markers.");
                Pattern pattern = Pattern.compile("(?s)```json\\s*(.*?)\\s*```");
                Matcher matcher = pattern.matcher(jsonResponse);

                if (matcher.find()) {
                    String jsonString = matcher.group(1);
                    jsonString = jsonString.replace("\n", "");
                    if (!jsonString.isEmpty()) {
                        return objectMapper.readTree(jsonString);
                    } else {
                        return null;
                    }
                } else {
                    return objectMapper.readTree(jsonResponse);
                }
            } else if (jsonResponse.contains("{")) {
                log.info("Input does not contain the required ```json``` markers. So processing it based on the indication of object literals.");
                return objectMapper.readTree(jsonResponse);
            } else {
                log.info("Input does not contain the required ```json``` markers or any indication of object literals. So returning null.");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String extractPageContent(String jsonString) {
        int startIndex = jsonString.indexOf("\"pageContent\":") + "\"pageContent\":".length();
        int endIndex = jsonString.lastIndexOf("}");

        if (startIndex != -1 && endIndex != -1) {
            String pageContent = jsonString.substring(startIndex, endIndex).trim();
            if (pageContent.startsWith("\"")) {
                pageContent = pageContent.substring(1);
            }
            if (pageContent.endsWith("\"")) {
                pageContent = pageContent.substring(0, pageContent.length() - 1);
            }
            return pageContent;
        } else {
            return "";
        }
    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        String requestStr;
        if ("true".equals(encryptReqRes)) {
            String encryptedRequest = SecurityEngine.getInticsIntegrityMethod(action, log).encrypt(request, "AES256", "COPRO_REQUEST");
            requestStr = encryptedRequest;
        } else {
            requestStr = request;
        }
        return requestStr;
    }
}