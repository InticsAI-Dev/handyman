package in.handyman.raven.lib.model.deep.sift;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.lib.replicate.ReplicateResponse;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.core.utils.ProcessFileFormatE;
import in.handyman.raven.util.ExceptionUtil;
import in.handyman.raven.lib.TestDataExtractorAction;
import in.handyman.raven.lib.model.testDataExtractor.TestDataExtractorInput;
import org.slf4j.Logger;
import org.slf4j.Marker;
import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.nio.file.FileAlreadyExistsException;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;
import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_TEXT_EXTRACTION_OUTPUT;

public class DeepSiftConsumerProcess implements CoproProcessor.ConsumerProcess<DeepSiftInputTable, DeepSiftOutputTable> {
    public static final String OPTIMUS_START = "OPTIMUS START";
    public static final String KRYPTON_START = "KRYPTON START";
    public static final String DEEP_SIFT_MODEL_NAME = "preprocess.deep.sift.model.name";

    public static final String PAGE_CONTENT_NO = "no";
    public static final String PAGE_CONTENT_YES = "yes";
    private final int pageContentMinLength;
    private final Logger log;
    private final Marker aMarker;
    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private static final String PROCESS_NAME = "DATA_EXTRACTION";
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
        this.httpclient = new OkHttpClient.Builder()
                .connectTimeout(timeOut, TimeUnit.MINUTES)
                .writeTimeout(timeOut, TimeUnit.MINUTES)
                .readTimeout(timeOut, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public List<DeepSiftOutputTable> process(URL endpoint, DeepSiftInputTable entity) throws IOException {
        List<DeepSiftOutputTable> parentObj = new ArrayList<>();

        String deepSiftModelName = action.getContext().get(DEEP_SIFT_MODEL_NAME);

        String inputFilePath = entity.getInputFilePath();
        if (inputFilePath == null || inputFilePath.trim().isEmpty()) {
            log.error(aMarker, "Input file path is null or empty for originId: {}", entity.getOriginId());
            parentObj.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                    "Input file path is null or empty", "", "", endpoint.toString()));
            return parentObj;
        }

        File inputFile = new File(inputFilePath);
        if (!inputFile.exists() || !inputFile.canRead()) {
            log.error(aMarker, "Input file does not exist or is not readable: {}", inputFilePath);
            parentObj.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                    "Input file does not exist or is not readable: " + inputFilePath, "", "", endpoint.toString()));
            return parentObj;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        Integer paperNumber = entity.getPaperNo();
        Long tenantId = entity.getTenantId();
        Long actionId = action.getActionId();
        String batchId = entity.getBatchId();
        String sourceDocumentType = entity.getSourceDocumentType();
        String sorItemName = entity.getSorItemName();
        String sorContainerName = entity.getSorContainerName();
        Integer modelId = entity.getModelId();
        String modelName = entity.getModelName();
        String searchName = entity.getSearchName();

        DeepSiftData deepSiftData = new DeepSiftData();
        deepSiftData.setOriginId(originId);
        deepSiftData.setGroupId(groupId);
        deepSiftData.setTenantId(tenantId);
        deepSiftData.setRootPipelineId(entity.getRootPipelineId());
        deepSiftData.setActionId(actionId);
        deepSiftData.setPaperNumber(paperNumber);
        deepSiftData.setProcess(PROCESS_NAME);
        deepSiftData.setInputFilePath(inputFilePath);
        deepSiftData.setBatchId(batchId);

        String jsonInputRequest = objectMapper.writeValueAsString(deepSiftData);

        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request built for URL: {}, inputFilePath: {}", endpoint, inputFilePath);
        }

        if ("OPTIMUS".equals(modelName)) {
            log.info(aMarker, "Executing OPTIMUS handler for endpoint: {}", endpoint);
            DeepSiftData deepSiftPayload = getOptimusRequestPayloadFromEntity(entity);
            String base64ForPath;
            try {
                base64ForPath = getBase64ForPath(inputFilePath);
                if (base64ForPath == null || base64ForPath.isEmpty()) {
                    log.error(aMarker, "Base64 conversion returned null or empty for file: {}", inputFilePath);
                }
                deepSiftPayload.setBase64Img(base64ForPath);
                log.info(aMarker, "Base64 image generated for file: {}", inputFilePath);
            } catch (IOException e) {
                log.error(aMarker, "Failed to convert file to Base64: {}", inputFilePath, e);
                throw new HandymanException("Error converting file to Base64 for OPTIMUS", e, action);
            }
            String deepSiftPayloadString;
            try {
                deepSiftPayloadString = mapper.writeValueAsString(deepSiftPayload);
            } catch (JsonProcessingException e) {
                log.error(aMarker, "Failed to serialize OPTIMUS payload", e);
                throw new HandymanException("Error serializing OPTIMUS payload", e, action);
            }

            deepSiftPayloadString = mapper.writeValueAsString(deepSiftPayload);

            String jsonRequestOptimus = getTritonRequestPayload(deepSiftPayloadString, OPTIMUS_START, mapper);

            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(jsonRequestOptimus, mediaType))
                    .build();
            tritonRequestOptimusExecutor(entity, request, parentObj, jsonRequestOptimus, endpoint);
        } else if ("KRYPTON".equals(modelName)) {
            log.info(aMarker, "Executing KRYPTON handler for endpoint: {}", endpoint);
            DeepSiftRequest kryptonRequestPayload = getKryptonRequestPayloadFromQuery(entity);
            String base64ForPath;
            try {
                base64ForPath = getBase64ForPath(inputFilePath);
                if (base64ForPath == null || base64ForPath.isEmpty()) {
                    log.error(aMarker, "Base64 conversion returned null or empty for file: {}", inputFilePath);
                }
                kryptonRequestPayload.setBase64Img(base64ForPath);
                log.info(aMarker, "Base64 image generated for file: {}", inputFilePath);
            } catch (IOException e) {
                log.error(aMarker, "Failed to convert file to Base64: {}", inputFilePath, e);
                throw new HandymanException("Error converting file to Base64 for KRYPTON", e, action);
            }
            String deepSiftPayloadString;
            try {
                deepSiftPayloadString = mapper.writeValueAsString(kryptonRequestPayload);
            } catch (JsonProcessingException e) {
                log.error(aMarker, "Failed to serialize Krypton payload", e);
                throw new HandymanException("Error serializing Krypton payload", e, action);
            }

            String jsonRequestKrypton = getTritonRequestPayload(deepSiftPayloadString, KRYPTON_START, mapper);

            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(jsonRequestKrypton, mediaType))
                    .build();
            tritonRequestKryptonExecutor(entity, request, parentObj, jsonRequestKrypton, endpoint);
        } else if ("ARGON".equals(modelName)) {
            log.info(aMarker, "Executing ARGON handler for endpoint: {}", endpoint);

            try {
                log.info(aMarker, "Input file: {}, exists: {}", inputFilePath, inputFile.exists());

                byte[] fileContent = Files.readAllBytes(inputFile.toPath());
                String contentType = inputFile.getName().endsWith(".jpg") || inputFile.getName().endsWith(".jpeg") ? "image/jpeg" : null;
                if (contentType == null) {
                    throw new HandymanException("Input file is not a JPEG: " + inputFilePath);
                }

                String outputDir = System.getProperty("java.io.tmpdir") + File.separator + "ARGON_output";
                try {
                    Path outputPath = Paths.get(outputDir);
                    if (Files.exists(outputPath) && !Files.isDirectory(outputPath)) {
                        throw new HandymanException("Path exists but is not a directory: " + outputDir);
                    }
                    Files.createDirectories(outputPath);
                    log.info(aMarker, "Output directory created or already exists: {}", outputDir);
                } catch (FileAlreadyExistsException e) {
                    log.warn(aMarker, "Directory already exists, continuing: {}", outputDir, e);
                } catch (IOException e) {
                    log.error(aMarker, "Failed to create output directory: {}", outputDir, e);
                    throw new HandymanException("Cannot create output directory: " + outputDir, e);
                }

                if (!Files.isWritable(Paths.get(outputDir))) {
                    throw new HandymanException("Output directory is not writable: " + outputDir);
                }

                String baseFileName = inputFile.getName().replaceAll("(?i)\\.jpg|\\.jpeg$", "");
                String outputFilePath = outputDir + File.separator + baseFileName + ".txt";
                log.info(aMarker, "Output file path: {}", outputFilePath);

                TestDataExtractorInput.TestDataExtractorInputBuilder builder = TestDataExtractorInput.builder()
                        .name("DeepSiftArgon")
                        .mode("text")
                        .inputFilePaths(Collections.singletonList(inputFilePath))
                        .outputPath(outputFilePath)
                        .condition(true);

                builder.endPoint(endpoint.toString());
                builder.resourceConn(action.getContext().getOrDefault("resource.connection", ""));
                builder.resultTable(action.getContext().getOrDefault("result.table", ""));
                builder.querySet(action.getContext().getOrDefault("query.set", ""));

                TestDataExtractorInput testDataExtractorInput = builder.build();

                TestDataExtractorAction extractorAction = new TestDataExtractorAction(action, log, testDataExtractorInput);
                log.info(aMarker, "Starting extraction for file: {}", inputFile.getName());
                extractorAction.execute();

                File outputFile = new File(outputFilePath);
                log.info(aMarker, "Checking output file: {}, exists: {}", outputFilePath, outputFile.exists());
                if (!outputFile.exists()) {
                    throw new HandymanException("Output file not generated: " + outputFilePath);
                }
                String extractedText = Files.readString(outputFile.toPath());

                String cleanedText = extractedText.replaceAll(
                        "TesseractBatchResponseDto\\(documents=\\[TesseractResponseDto\\(textByPage=\\{Data=([\\s\\S]*?)\\}\\)\\]\\)",
                        "$1"
                );

                int pageNumber = entity.getPaperNo() != null ? entity.getPaperNo() : 1; // Use entity.getPaperNo()
                String flag = (cleanedText.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;
                String encryptSotPageContent = action.getContext().get(ENCRYPT_TEXT_EXTRACTION_OUTPUT);
                String extractedContentEnc = cleanedText;
                InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);

                if ("true".equals(encryptSotPageContent)) {
                    extractedContentEnc = encryption.encrypt(cleanedText, "AES256", "TEXT_DATA");
                }

                parentObj.add(DeepSiftOutputTable.builder().inputFilePath(inputFilePath).extractedText(extractedContentEnc).originId(entity.getOriginId()).groupId(entity.getGroupId()).paperNo(pageNumber).createdOn(entity.getCreatedOn()).rootPipelineId(entity.getRootPipelineId()).tenantId(entity.getTenantId()).batchId(entity.getBatchId()).sourceDocumentType(entity.getSourceDocumentType()).sorItemId(entity.getSorItemId()).sorItemName(entity.getSorItemName()).sorContainerId(entity.getSorContainerId()).sorContainerName(entity.getSorContainerName()).modelId(entity.getModelId()).modelName(modelName).searchName(entity.getSearchName()).build());

                outputFile.delete();
                log.info(aMarker, "Output file deleted: {}", outputFilePath);

            } catch (Exception e) {
                log.error(aMarker, "Exception in ARGON handler for file: {}", inputFilePath, e);
                parentObj.add(DeepSiftOutputTable.builder().inputFilePath(inputFilePath).originId(entity.getOriginId()).groupId(entity.getGroupId()).paperNo(entity.getPaperNo() != null ? entity.getPaperNo() : 1).createdOn(entity.getCreatedOn()).rootPipelineId(entity.getRootPipelineId()).tenantId(entity.getTenantId()).batchId(entity.getBatchId()).sourceDocumentType(entity.getSourceDocumentType()).sorItemId(entity.getSorItemId()).sorItemName(entity.getSorItemName()).sorContainerId(entity.getSorContainerId()).sorContainerName(entity.getSorContainerName()).modelId(entity.getModelId()).modelName(modelName).searchName(entity.getSearchName()).build());
                HandymanException handymanException = new HandymanException("ARGON processing failed", e);
                HandymanException.insertException("Deep sift ARGON failed for origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, action);
                throw handymanException;
            }
        }
        return parentObj;
    }

    private DeepSiftRequest getKryptonRequestPayloadFromQuery(DeepSiftInputTable entity) {
        DeepSiftRequest deepSiftRequest = new DeepSiftRequest();
        deepSiftRequest.setOriginId(entity.getOriginId());
        deepSiftRequest.setTenantId(entity.getTenantId());
        deepSiftRequest.setRootPipelineId(entity.getRootPipelineId());
        deepSiftRequest.setActionId(action.getActionId());
        deepSiftRequest.setProcess(PROCESS_NAME);
        deepSiftRequest.setInputFilePath(entity.getInputFilePath());
        deepSiftRequest.setBatchId(entity.getBatchId());
        deepSiftRequest.setSystemPrompt(entity.getSystemPrompt());
        deepSiftRequest.setPaperNo(entity.getPaperNo());
        deepSiftRequest.setGroupId(Long.valueOf(entity.getGroupId()));
        deepSiftRequest.setUserPrompt(entity.getBasePrompt());
        deepSiftRequest.setSystemPrompt(entity.getSystemPrompt());
        deepSiftRequest.setModelName(entity.getModelName());
        return deepSiftRequest;
    }

    private void tritonRequestKryptonExecutor(DeepSiftInputTable entity, Request request, List<DeepSiftOutputTable> parentObj,
                                              String jsonRequest, URL endpoint) {
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            log.info(aMarker, "KRYPTON response: code={}, message={}", response.code(), response.message());

            if (response.isSuccessful()) {
                DeepSiftResponse modelResponse = mapper.readValue(responseBody, DeepSiftResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(s -> {
                        try {
                            extractedKryptonOutputDataRequest(entity, s, parentObj, modelResponse.getModelName(),
                                    modelResponse.getModelVersion(), jsonRequest, responseBody,
                                    endpoint.toString());
                        } catch (JsonProcessingException e) {
                            throw new HandymanException("Exception in extracted output Data request", e);
                        }
                    }));
                }
            } else {
                parentObj.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                        response.message(), jsonRequest, responseBody, endpoint.toString()));
                HandymanException handymanException = new HandymanException("Unsuccessful response code: " + response.code() +
                        " message: " + response.message());
                HandymanException.insertException("Deep sift consumer failed for origin Id " + entity.getOriginId() +
                        " paper no " + entity.getPaperNo(), handymanException, action);
                log.error(aMarker, "Error in response: {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                    ExceptionUtil.toString(e), jsonRequest, "Error In Response", endpoint.toString()));
            log.error(aMarker, "Exception occurred", e);
            HandymanException handymanException = new HandymanException("Deep sift consumer failed", e);
            HandymanException.insertException("Deep sift consumer failed for origin Id " + entity.getOriginId() +
                    " paper no " + entity.getPaperNo(), handymanException, action);
        }
    }

    private void extractedKryptonOutputDataRequest(DeepSiftInputTable entity, String stringDataItem, List<DeepSiftOutputTable> parentObj,
                                                   String modelName, String modelVersion, String request, String response, String endpoint)
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dataNodeJson = mapper.readTree(stringDataItem);

        String innerModel = dataNodeJson.has("model") ? dataNodeJson.get("model").asText() : modelName;

        DeepSiftLineItem deepSiftDataItem = mapper.readValue(stringDataItem, DeepSiftLineItem.class);
        deepSiftDataItem.setOriginId(Optional.ofNullable(deepSiftDataItem.getOriginId()).orElse(entity.getOriginId()));
        deepSiftDataItem.setInputFilePath(Optional.ofNullable(deepSiftDataItem.getInputFilePath()).orElse(entity.getInputFilePath()));
        deepSiftDataItem.setProcessId(Optional.ofNullable(deepSiftDataItem.getProcessId()).orElse(entity.getProcessId()));
        deepSiftDataItem.setGroupId(Optional.ofNullable(deepSiftDataItem.getGroupId()).orElse(entity.getGroupId()));
        deepSiftDataItem.setTenantId(Optional.ofNullable(deepSiftDataItem.getTenantId()).orElse(entity.getTenantId()));
        deepSiftDataItem.setRootPipelineId(Optional.ofNullable(deepSiftDataItem.getRootPipelineId()).orElse(entity.getRootPipelineId()));
        deepSiftDataItem.setBatchId(Optional.ofNullable(deepSiftDataItem.getBatchId()).orElse(entity.getBatchId()));

        String contentString = Optional.ofNullable(deepSiftDataItem.getInferResponse()).map(String::valueOf).orElse("");
        String flag = (contentString.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;
        String extractedContent = contentString;
        String encryptSotPageContent = action.getContext().get(ENCRYPT_TEXT_EXTRACTION_OUTPUT);
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);

        if ("true".equals(encryptSotPageContent)) {
            extractedContent = encryption.encrypt(contentString, "AES256", "TEXT_DATA");
        }

        parentObj.add(DeepSiftOutputTable.builder().inputFilePath(deepSiftDataItem.getInputFilePath()).extractedText(extractedContent).originId(deepSiftDataItem.getOriginId()).groupId(deepSiftDataItem.getGroupId()).paperNo(entity.getPaperNo()).createdOn(entity.getCreatedOn()).rootPipelineId(deepSiftDataItem.getRootPipelineId()).tenantId(deepSiftDataItem.getTenantId()).batchId(deepSiftDataItem.getBatchId()).sourceDocumentType(entity.getSourceDocumentType()).sorItemId(entity.getSorItemId()).sorItemName(entity.getSorItemName()).sorContainerId(entity.getSorContainerId()).sorContainerName(entity.getSorContainerName()).modelId(entity.getModelId()).modelName(innerModel).searchName(entity.getSearchName()).build());
    }

    private void extractedOptimusOutputDataRequest(DeepSiftInputTable entity, String stringDataItem, List<DeepSiftOutputTable> parentObj,
                                                 String modelName, String modelVersion, String request, String response, String endpoint)
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dataNodeJson = mapper.readTree(stringDataItem);
        String innerModel = dataNodeJson.has("model") ? dataNodeJson.get("model").asText() : modelName;

        DeepSiftDataItem deepSiftDataItem = mapper.readValue(stringDataItem, DeepSiftDataItem.class);
        deepSiftDataItem.setOriginId(Optional.ofNullable(deepSiftDataItem.getOriginId()).orElse(entity.getOriginId()));
        deepSiftDataItem.setInputFilePath(Optional.ofNullable(deepSiftDataItem.getInputFilePath()).orElse(entity.getInputFilePath()));
        deepSiftDataItem.setProcessId(Optional.ofNullable(deepSiftDataItem.getProcessId()).orElse(entity.getProcessId()));
        deepSiftDataItem.setGroupId(Optional.ofNullable(deepSiftDataItem.getGroupId()).orElse(entity.getGroupId()));
        deepSiftDataItem.setTenantId(Optional.ofNullable(deepSiftDataItem.getTenantId()).orElse(entity.getTenantId()));
        deepSiftDataItem.setRootPipelineId(Optional.ofNullable(deepSiftDataItem.getRootPipelineId()).orElse(entity.getRootPipelineId()));
        deepSiftDataItem.setBatchId(Optional.ofNullable(deepSiftDataItem.getBatchId()).orElse(entity.getBatchId()));

        String contentString = Optional.ofNullable(deepSiftDataItem.getInferResponse()).map(String::valueOf).orElse("");
        String flag = (contentString.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;
        String extractedContent = contentString;
        String encryptSotPageContent = action.getContext().get(ENCRYPT_TEXT_EXTRACTION_OUTPUT);
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);

        if ("true".equals(encryptSotPageContent)) {
            extractedContent = encryption.encrypt(contentString, "AES256", "TEXT_DATA");
        }

        parentObj.add(DeepSiftOutputTable.builder().inputFilePath(deepSiftDataItem.getInputFilePath()).extractedText(extractedContent).originId(deepSiftDataItem.getOriginId()).groupId(deepSiftDataItem.getGroupId()).paperNo(entity.getPaperNo()).createdOn(entity.getCreatedOn()).rootPipelineId(deepSiftDataItem.getRootPipelineId()).tenantId(deepSiftDataItem.getTenantId()).batchId(deepSiftDataItem.getBatchId()).sourceDocumentType(entity.getSourceDocumentType()).sorItemId(entity.getSorItemId()).sorItemName(entity.getSorItemName()).sorContainerId(entity.getSorContainerId()).sorContainerName(entity.getSorContainerName()).modelId(entity.getModelId()).modelName(innerModel).searchName(entity.getSearchName()).build());
    }

    private void tritonRequestOptimusExecutor(DeepSiftInputTable entity, Request request, List<DeepSiftOutputTable> parentObj, String jsonRequest, URL endpoint) {
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            log.info(aMarker, "Optimus response: code={}, message={}", response.code(), response.message());

            if (response.isSuccessful()) {
                DeepSiftResponse modelResponse = mapper.readValue(responseBody, DeepSiftResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(s -> {
                        try {
                            extractedOptimusOutputDataRequest(entity, s, parentObj, modelResponse.getModelName(),
                                    modelResponse.getModelVersion(), jsonRequest, responseBody,
                                    endpoint.toString());
                        } catch (JsonProcessingException e) {
                            throw new HandymanException("Exception in extracted output Data request", e);
                        }
                    }));
                }
            } else {
                parentObj.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                        response.message(), jsonRequest, responseBody, endpoint.toString()));
                HandymanException handymanException = new HandymanException("Unsuccessful response code: " + response.code() +
                        " message: " + response.message());
                HandymanException.insertException("Deep sift consumer failed for origin Id " + entity.getOriginId() +
                        " paper no " + entity.getPaperNo(), handymanException, action);
                log.error(aMarker, "Error in response");
            }
        } catch (Exception e) {
            parentObj.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                    ExceptionUtil.toString(e), jsonRequest, "Error In getting Response", endpoint.toString()));
            log.error(aMarker, "Exception occurred", e);
            HandymanException handymanException = new HandymanException("Deep sift consumer failed", e);
            HandymanException.insertException("Deep sift consumer failed for origin Id " + entity.getOriginId() +
                    " paper no " + entity.getPaperNo(), handymanException, action);
        }
    }

    private static String getTritonRequestPayload(String deepSiftPayloadString, String name, ObjectMapper objectMapper)
            throws JsonProcessingException {
        TritonRequest tritonRequestPayload = new TritonRequest();
        tritonRequestPayload.setName(name);
        tritonRequestPayload.setShape(List.of(1, 1));
        tritonRequestPayload.setDatatype(TritonDataTypes.BYTES.name());
        tritonRequestPayload.setData(Collections.singletonList(deepSiftPayloadString));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(tritonRequestPayload));

        return objectMapper.writeValueAsString(tritonInputRequest);
    }

    private DeepSiftData getOptimusRequestPayloadFromEntity(DeepSiftInputTable entity) {
        DeepSiftData deepSiftData = new DeepSiftData();
        deepSiftData.setOriginId(entity.getOriginId());
        deepSiftData.setGroupId(entity.getGroupId());
        deepSiftData.setProcessId(entity.getProcessId());
        deepSiftData.setTenantId(entity.getTenantId());
        deepSiftData.setRootPipelineId(entity.getRootPipelineId());
        deepSiftData.setActionId(action.getActionId());
        deepSiftData.setPaperNumber(entity.getPaperNo());
        deepSiftData.setProcess(PROCESS_NAME);
        deepSiftData.setInputFilePath(entity.getInputFilePath());
        deepSiftData.setBatchId(entity.getBatchId());
        deepSiftData.setUserPrompt(entity.getBasePrompt());
        deepSiftData.setSystemPrompt(entity.getSystemPrompt());
        deepSiftData.setModelName(entity.getModelName());
        return deepSiftData;
    }

    public String getBase64ForPath(String imagePath) throws IOException {
        try {
            byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            log.info(aMarker, "Base64 created for file: {}", imagePath);
            return base64Image;
        } catch (Exception e) {
            log.error(aMarker, "Error creating base64: {}", ExceptionUtil.toString(e));
            throw new HandymanException("Error creating base64", e);
        }
    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        if ("true".equals(encryptReqRes)) {
            return SecurityEngine.getInticsIntegrityMethod(action, log).encrypt(request, "AES256", "COPRO_REQUEST");
        }
        return request;
    }

    private DeepSiftOutputTable buildOutputTable(DeepSiftInputTable entity, String status, String message,
                                                 String request, String response, String endpoint) {
        return DeepSiftOutputTable.builder().inputFilePath(entity.getInputFilePath()).originId(entity.getOriginId()).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).createdOn(entity.getCreatedOn()).rootPipelineId(entity.getRootPipelineId()).tenantId(entity.getTenantId()).batchId(entity.getBatchId()).sourceDocumentType(entity.getSourceDocumentType()).sorItemId(entity.getSorItemId()).sorItemName(entity.getSorItemName()).sorContainerId(entity.getSorContainerId()).sorContainerName(entity.getSorContainerName()).modelId(entity.getModelId()).modelName(entity.getModelName()).searchName(entity.getSearchName()).build();
    }
}