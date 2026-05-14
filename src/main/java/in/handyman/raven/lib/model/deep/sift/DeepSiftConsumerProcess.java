package in.handyman.raven.lib.model.deep.sift;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.utils.ProcessFileFormatE;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.retry.CoproRetryErrorAuditTable;
import in.handyman.raven.lib.model.retry.CoproRetryService;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.utils.FileProcessingUtils;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_DEEP_SIFT_OUTPUT;
import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;
import static in.handyman.raven.core.enums.NetworkHandlerConstants.*;
import static in.handyman.raven.exception.HandymanException.handymanRepo;
import static in.handyman.raven.lib.DeepSiftAction.*;

import in.handyman.raven.lib.adapters.scalar.WordCountAdapter;

public class DeepSiftConsumerProcess
        implements CoproProcessor.ConsumerProcess<DeepSiftInputTable, DeepSiftOutputTable> {

    private static final String PROCESS_NAME = "DATA_EXTRACTION";
    private static final String ENCRYPTION_ALGORITHM = "AES256";
    private static final String TEXT_DATA_TYPE = "TEXT_DATA";
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final Set<String> VALID_MODELS = Set.of("XENON", "ARGON", "KRYPTON", "OPTIMUS");

    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;
    private final OkHttpClient httpClient;
    private final FileProcessingUtils fileProcessingUtils;
    private final ObjectMapper objectMapper;
    private final CoproRetryService coproRetryService;
    private final String processBase64;
    private final WordCountAdapter wordCountAdapter;

    public DeepSiftConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action,
                                   FileProcessingUtils fileProcessingUtils, String processBase64) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.fileProcessingUtils = fileProcessingUtils;
        this.objectMapper = new ObjectMapper();
        this.processBase64 = processBase64;
        this.wordCountAdapter = new WordCountAdapter();
        int connectTimeout = Integer
                .parseInt(this.action.getContext().getOrDefault(COPRO_CLIENT_DEEP_SIFT_CONNECT_TIMEOUT, "100"));
        int writeTimeout = Integer
                .parseInt(this.action.getContext().getOrDefault(COPRO_CLIENT_DEEP_SIFT_WRITE_TIMEOUT, "100"));
        int readTimeout = Integer
                .parseInt(this.action.getContext().getOrDefault(COPRO_CLIENT_DEEP_SIFT_READ_TIMEOUT, "100"));
        int callTimeout = Integer
                .parseInt(this.action.getContext().getOrDefault(COPRO_CLIENT_DEEP_SIFT_CALL_TIMEOUT, "100"));

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.MINUTES)
                .writeTimeout(writeTimeout, TimeUnit.MINUTES)
                .readTimeout(readTimeout, TimeUnit.MINUTES)
                .callTimeout(callTimeout, TimeUnit.MINUTES)
                .build();
        coproRetryService = new CoproRetryService(handymanRepo, httpClient, log);
    }

    @Override
    public List<DeepSiftOutputTable> process(URL endpoint, DeepSiftInputTable entity) throws IOException {

        final boolean deepSiftBboxActivator = Boolean.parseBoolean(action.getContext().getOrDefault(DEEP_SIFT_BBOX_EXTRACTION_ACTIVATOR, "false"));
        final boolean useTess4j = Boolean
                .parseBoolean(action.getContext().getOrDefault(DEEP_SIFT_ROUTE_TESS4J, "false"));

        List<DeepSiftOutputTable> parentObj = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        if (!VALID_MODELS.contains(entity.getModelName())) {
            String errorMessage = "Invalid model name " + entity.getModelName()
                    + " | originId=" + entity.getOriginId()
                    + " paperNo=" + entity.getPaperNo()
                    + " rootPipelineId=" + entity.getRootPipelineId();
            log.error(aMarker, errorMessage);
            HandymanException handymanException = new HandymanException(errorMessage);
            HandymanException.insertException(errorMessage, handymanException, action);
        }

        String inputFilePath = entity.getInputFilePath();
        if (inputFilePath == null || inputFilePath.trim().isEmpty()) {
            String errorMessage = "Input file path is null or empty"
                    + " | originId=" + entity.getOriginId()
                    + " paperNo=" + entity.getPaperNo()
                    + " rootPipelineId=" + entity.getRootPipelineId();
            log.error(aMarker, errorMessage);
            HandymanException handymanException = new HandymanException(errorMessage);
            HandymanException.insertException(errorMessage, handymanException, action);
        }

        assert inputFilePath != null;
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists() || !inputFile.canRead()) {
            String errorMessage = "Input file does not exist or is not readable"
                    + " | originId=" + entity.getOriginId()
                    + " paperNo=" + entity.getPaperNo()
                    + " rootPipelineId=" + entity.getRootPipelineId();
            log.error(aMarker, errorMessage);
            HandymanException handymanException = new HandymanException(errorMessage);
            HandymanException.insertException(errorMessage, handymanException, action);
        }

        log.info(aMarker, "Executing {} handler | originId={} paperNo={} rootPipelineId={} endpoint={}",
                entity.getModelName(), entity.getOriginId(), entity.getPaperNo(),
                entity.getRootPipelineId(), endpoint);

        DeepSiftRequest requestPayload = getRequestPayloadFromQuery(entity, deepSiftBboxActivator);

        if (useTess4j) {
            processWithTess4j(entity, parentObj, inputFile, endpoint, startTime, deepSiftBboxActivator);
        } else {
            final UUID requestId = UUID.randomUUID();
            final Boolean coproMetricsCalculator = Boolean
                    .valueOf(action.getContext().getOrDefault("copro.metrics.activator", "false"));
            entity.setRequestId(requestId);
            entity.setCoproMetricsActivator(coproMetricsCalculator);
            String base64Content = processBase64.equals(ProcessFileFormatE.BASE64.name())
                    ? fileProcessingUtils.convertFileToBase64(inputFilePath)
                    : "";
            requestPayload.setBase64Img(base64Content);

            String jsonRequest = getXenonRequest(requestPayload);
            requestPayload.setBase64Img("");
            String dbJsonRequest = sanitizeRequestForDb(requestPayload);

            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(jsonRequest, MEDIA_TYPE))
                    .build();
            requestExecutor(entity, request, parentObj, dbJsonRequest, endpoint, startTime, deepSiftBboxActivator);
        }

        return parentObj;
    }

    private DeepSiftRequest getRequestPayloadFromQuery(DeepSiftInputTable entity, Boolean deepSiftBboxActivator) {
        DeepSiftRequest deepSiftRequest = new DeepSiftRequest();
        deepSiftRequest.setOriginId(entity.getOriginId());
        deepSiftRequest.setTenantId(entity.getTenantId());
        deepSiftRequest.setRootPipelineId(entity.getRootPipelineId());
        deepSiftRequest.setActionId(action.getActionId());
        deepSiftRequest.setProcess(PROCESS_NAME);
        deepSiftRequest.setInputFilePath(entity.getInputFilePath());
        deepSiftRequest.setBatchId(entity.getBatchId());
        deepSiftRequest.setProcessId(entity.getRootPipelineId());
        deepSiftRequest.setGroupId(Long.valueOf(entity.getGroupId()));
        deepSiftRequest.setModelName(entity.getModelName());
        deepSiftRequest.setPaperNo(entity.getPaperNo());
        deepSiftRequest.setRequestId(entity.getRequestId());
        deepSiftRequest.setCoproMetricsActivator(entity.getCoproMetricsActivator());
        deepSiftRequest.setCoproBboxActivator(deepSiftBboxActivator);
        return deepSiftRequest;
    }

    private String getXenonRequest(DeepSiftRequest deepSiftRequest) throws JsonProcessingException {
        XenonRequest customRequest = XenonRequest.builder()
                .originId(deepSiftRequest.getOriginId())
                .batchId(deepSiftRequest.getBatchId())
                .paperNo(deepSiftRequest.getPaperNo())
                .processId(deepSiftRequest.getProcessId())
                .groupId(deepSiftRequest.getGroupId())
                .tenantId(deepSiftRequest.getTenantId())
                .rootPipelineId(deepSiftRequest.getRootPipelineId())
                .process(deepSiftRequest.getProcess())
                .modelName(deepSiftRequest.getModelName())
                .actionId(deepSiftRequest.getActionId())
                .inputFilePath(deepSiftRequest.getInputFilePath())
                .base64Img(deepSiftRequest.getBase64Img())
                .requestId(deepSiftRequest.getRequestId())
                .coproMetricsActivator(deepSiftRequest.getCoproMetricsActivator())
                .returnBbox(deepSiftRequest.getCoproBboxActivator())
                .build();
        return objectMapper.writeValueAsString(customRequest);
    }

    private String sanitizeRequestForDb(DeepSiftRequest deepSiftRequest) {
        try {
            XenonRequest sanitizedRequest = XenonRequest.builder()
                    .originId(deepSiftRequest.getOriginId())
                    .batchId(deepSiftRequest.getBatchId())
                    .paperNo(deepSiftRequest.getPaperNo())
                    .processId(deepSiftRequest.getProcessId())
                    .groupId(deepSiftRequest.getGroupId())
                    .tenantId(deepSiftRequest.getTenantId())
                    .rootPipelineId(deepSiftRequest.getRootPipelineId())
                    .process(deepSiftRequest.getProcess())
                    .modelName(deepSiftRequest.getModelName())
                    .actionId(deepSiftRequest.getActionId())
                    .inputFilePath(deepSiftRequest.getInputFilePath())
                    .requestId(deepSiftRequest.getRequestId())
                    .coproMetricsActivator(deepSiftRequest.getCoproMetricsActivator())
                    .returnBbox(deepSiftRequest.getCoproBboxActivator())
                    .build();
            return objectMapper.writeValueAsString(sanitizedRequest);
        } catch (JsonProcessingException e) {
            String errorMessage = "Failed to sanitize DeepSiftRequest for DB"
                    + " | originId=" + deepSiftRequest.getOriginId()
                    + " paperNo=" + deepSiftRequest.getPaperNo()
                    + " rootPipelineId=" + deepSiftRequest.getRootPipelineId();
            HandymanException handymanException = new HandymanException(errorMessage, e);
            HandymanException.insertException(errorMessage, handymanException, action);
            throw handymanException;
        }
    }

    private void processWithTess4j(DeepSiftInputTable entity, List<DeepSiftOutputTable> parentObj, File inputFile,
                                   URL endpoint, long startTime, boolean deepSiftBboxActivator) {
        try {
            log.info(aMarker, "Executing Tess4J extraction | originId={} paperNo={} rootPipelineId={}",
                    entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId());

            String tess4jModelPath = action.getContext()
                    .getOrDefault(DEEP_SIFT_TESS4J_MODEL_PATH, "/usr/share/tesseract-ocr/5/tessdata");

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(tess4jModelPath);
            tesseract.setLanguage("eng");

            String extractedContent;
            String bboxListJson = null;
            int bboxCount = 0;

            if (deepSiftBboxActivator) {
                List<Map<String, Object>> tess4jBbox = new ArrayList<>();
                extractedContent = extractTess4jTextAndBbox(tesseract, inputFile, entity, tess4jBbox);
                bboxCount = tess4jBbox.size();
                bboxListJson = serializeBboxList(tess4jBbox, entity);
            } else {
                extractedContent = tesseract.doOCR(inputFile);
            }

            int wordCount = 0;
            try {
                wordCount = wordCountAdapter.getThresholdScore(extractedContent);
            } catch (Exception e) {
                log.error(aMarker, "Error computing word count | originId={} paperNo={} rootPipelineId={}",
                        entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId(), e);
            }

            int blankPageThreshold = Integer.parseInt(
                    action.getContext().getOrDefault(PAGE_CONTENT_MIN_LENGTH, "10"));
            boolean isBlankPage = wordCount < blankPageThreshold;

            log.info(aMarker, "Tess4J completed | originId={} paperNo={} rootPipelineId={} "
                            + "wordCount={} threshold={} isBlank={} bboxCount={}",
                    entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId(),
                    wordCount, blankPageThreshold, isBlankPage, bboxCount);

            String encryptSotPageContent = action.getContext().get(ENCRYPT_DEEP_SIFT_OUTPUT);
            String finalExtractedContent = extractedContent;
            String finalBboxListJson = bboxListJson;
            if ("true".equals(encryptSotPageContent)) {
                InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);
                finalExtractedContent = encryption.encrypt(extractedContent, ENCRYPTION_ALGORITHM, TEXT_DATA_TYPE);
                if (bboxListJson != null && !bboxListJson.isEmpty()) {
                    finalBboxListJson = encryption.encrypt(bboxListJson, ENCRYPTION_ALGORITHM, TEXT_DATA_TYPE);
                }
            }

            long elapsedTimeMs = System.currentTimeMillis() - startTime;

            parentObj.add(DeepSiftOutputTable.builder()
                    .inputFilePath(entity.getInputFilePath())
                    .extractedText(finalExtractedContent)
                    .extractedTextWithBbox(finalBboxListJson)
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(entity.getGroupId() != null ? Math.toIntExact(entity.getGroupId()) : null)
                    .paperNo(entity.getPaperNo())
                    .createdOn(entity.getCreatedOn())
                    .createdBy(String.valueOf(entity.getTenantId()))
                    .rootPipelineId(entity.getRootPipelineId())
                    .tenantId(entity.getTenantId())
                    .batchId(entity.getBatchId())
                    .sourceDocumentType(entity.getSourceDocumentType())
                    .modelId(entity.getModelId())
                    .modelName(entity.getModelName())
                    .timeTakenMS(elapsedTimeMs)
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .request("Tess4J Internal Processing")
                    .response("Tess4J Internal Processing Result")
                    .endpoint(String.valueOf(endpoint))
                    .wordCount(wordCount)
                    .isBlankPage(isBlankPage)
                    .build());

        } catch (TesseractException e) {
            log.error(aMarker, "TesseractException | originId={} paperNo={} rootPipelineId={}",
                    entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId(), e);
            HandymanException handymanException = new HandymanException(
                    "Deep sift consumer failed for Tess4J model", e);
            HandymanException.insertException(
                    "Deep sift consumer failed | originId=" + entity.getOriginId()
                            + " paperNo=" + entity.getPaperNo()
                            + " rootPipelineId=" + entity.getRootPipelineId() + " model=Tess4J",
                    handymanException, action);

            String errorMessage = "Tess4J processing failed: " + e.getMessage();
            parentObj.add(DeepSiftOutputTable.builder()
                    .batchId(entity.getBatchId())
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(entity.getGroupId() != null ? Math.toIntExact(entity.getGroupId()) : null)
                    .paperNo(entity.getPaperNo())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .tenantId(entity.getTenantId())
                    .createdOn(entity.getCreatedOn())
                    .rootPipelineId(entity.getRootPipelineId())
                    .request("Tess4J Internal Processing")
                    .response(errorMessage)
                    .endpoint(String.valueOf(endpoint))
                    .build());
        } catch (Exception e) {
            log.error(aMarker, "Tess4J unknown error | originId={} paperNo={} rootPipelineId={}",
                    entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId(), e);
            HandymanException handymanException = new HandymanException(
                    "Deep sift consumer failed for Tess4J model", e);
            HandymanException.insertException(
                    "Deep sift consumer failed | originId=" + entity.getOriginId()
                            + " paperNo=" + entity.getPaperNo()
                            + " rootPipelineId=" + entity.getRootPipelineId() + " model=Tess4J",
                    handymanException, action);

            String errorMessage = "Tess4J unknown error: " + e.getMessage();
            parentObj.add(DeepSiftOutputTable.builder()
                    .batchId(entity.getBatchId())
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(entity.getGroupId() != null ? Math.toIntExact(entity.getGroupId()) : null)
                    .paperNo(entity.getPaperNo())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .tenantId(entity.getTenantId())
                    .createdOn(entity.getCreatedOn())
                    .rootPipelineId(entity.getRootPipelineId())
                    .request("Tess4J Internal Processing")
                    .response(errorMessage)
                    .endpoint(String.valueOf(endpoint))
                    .build());
        }
    }

    private String extractTess4jTextAndBbox(ITesseract tesseract, File inputFile,
                                            DeepSiftInputTable entity,
                                            List<Map<String, Object>> bboxList) {
        StringBuilder textBuilder = new StringBuilder();
        try {
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                log.warn(aMarker, "Tess4J extraction skipped, unreadable image | originId={} paperNo={} rootPipelineId={}",
                        entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId());
                return "";
            }

            List<Word> words = tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_WORD);
            if (words == null || words.isEmpty()) {
                return "";
            }

            Integer prevLineMidY = null;
            boolean firstWordOnLine = true;

            for (Word word : words) {
                if (word == null) {
                    continue;
                }
                String wordText = word.getText() == null ? "" : word.getText().trim();
                Rectangle rect = word.getBoundingBox();
                if (wordText.isEmpty() || rect == null) {
                    continue;
                }

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("text", wordText);
                item.put("bbox", Arrays.asList(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height));
                item.put("confidence", Math.round(word.getConfidence() * 100.0) / 100.0);
                bboxList.add(item);

                int wordMidY = rect.y + rect.height / 2;
                int lineThreshold = Math.max(rect.height / 2, 5);

                if (prevLineMidY != null && Math.abs(wordMidY - prevLineMidY) > lineThreshold) {
                    textBuilder.append("\n");
                    firstWordOnLine = true;
                }

                if (!firstWordOnLine) {
                    textBuilder.append(" ");
                }
                textBuilder.append(wordText);
                firstWordOnLine = false;
                prevLineMidY = wordMidY;
            }
        } catch (Exception e) {
            log.error(aMarker, "Tess4J extraction failed | originId={} paperNo={} rootPipelineId={}",
                    entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId(), e);
        }
        return textBuilder.toString().trim();
    }


    private String serializeBboxList(Object bboxList, DeepSiftInputTable entity) {
        if (bboxList == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(bboxList);
        } catch (JsonProcessingException e) {
            log.error(aMarker, "Bbox serialization failed | originId={} paperNo={} rootPipelineId={}",
                    entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId(), e);
            return null;
        }
    }

    private void requestExecutor(DeepSiftInputTable entity, Request request, List<DeepSiftOutputTable> parentObj,
                                 String dbJsonRequest, URL endpoint, long startTime,
                                 boolean deepSiftBboxActivator) {

        CoproRetryErrorAuditTable auditInput = setErrorAuditInputDetails(entity, endpoint);
        Response response;
        try {
            response = Boolean.parseBoolean(action.getContext().getOrDefault("copro.isretry.enabled", "false"))
                    ? coproRetryService.callCoproApiWithRetry(request, dbJsonRequest, auditInput, this.action,
                    entity.getRequestId())
                    : httpClient.newCall(request).execute();
            if (response == null) {
                String errorMessage = "No response received from API";
                parentObj.add(DeepSiftOutputTable.builder()
                        .batchId(entity.getBatchId())
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .groupId(entity.getGroupId())
                        .paperNo(entity.getPaperNo())
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .tenantId(entity.getTenantId())
                        .createdOn(entity.getCreatedOn())
                        .rootPipelineId(entity.getRootPipelineId())
                        .request(encryptRequestResponse(dbJsonRequest))
                        .response(errorMessage)
                        .endpoint(String.valueOf(endpoint))
                        .build());
                log.error(aMarker, "No response from API | originId={} paperNo={} rootPipelineId={}",
                        entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId());
                HandymanException handymanException = new HandymanException(errorMessage);
                HandymanException.insertException(errorMessage, handymanException, this.action);
                throw new IOException(errorMessage);
            }

            try (Response safeResponse = response) {
                long elapsedTimeMs = System.currentTimeMillis() - startTime;
                if (safeResponse.body() == null) {
                    log.error(aMarker, "Response body is null | originId={} paperNo={} rootPipelineId={} model={}",
                            entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId(),
                            entity.getModelName());
                    HandymanException handymanException = new HandymanException(
                            "Deep sift consumer failed for model " + entity.getModelName());
                    HandymanException.insertException(
                            "Response body is null | originId=" + entity.getOriginId()
                                    + " paperNo=" + entity.getPaperNo()
                                    + " rootPipelineId=" + entity.getRootPipelineId(),
                            handymanException, action);
                }

                if (safeResponse.code() != 200) {
                    String errorMessage = "Non-200 response | code=" + safeResponse.code()
                            + " originId=" + entity.getOriginId()
                            + " paperNo=" + entity.getPaperNo()
                            + " rootPipelineId=" + entity.getRootPipelineId()
                            + " model=" + entity.getModelName();
                    log.error(aMarker, errorMessage);
                    HandymanException handymanException = new HandymanException(errorMessage);
                    HandymanException.insertException(errorMessage, handymanException, action);
                }

                assert safeResponse.body() != null;
                String responseBody = safeResponse.body().string();

                log.info(aMarker, "{} API response | originId={} paperNo={} rootPipelineId={} code={} message={}",
                        entity.getModelName(), entity.getOriginId(), entity.getPaperNo(),
                        entity.getRootPipelineId(), safeResponse.code(), safeResponse.message());

                if (safeResponse.isSuccessful()) {
                    XenonResponse modelResponse = objectMapper.readValue(responseBody, XenonResponse.class);

                    if (modelResponse.isSuccess() && modelResponse.hasInferResponse()) {
                        String extractedContent = modelResponse.getInferResponseText();

                        String bboxListJson = null;
                        int bboxCount = 0;
                        if (deepSiftBboxActivator) {
                            List<XenonResponse.BboxItem> bboxItems = modelResponse.getBboxListSafe();
                            bboxCount = bboxItems.size();
                            bboxListJson = serializeBboxList(bboxItems, entity);
                        }

                        int wordCount;
                        try {
                            wordCount = wordCountAdapter.getThresholdScore(extractedContent);
                        } catch (Exception e) {
                            log.error(aMarker, "Word count error | originId={} paperNo={} rootPipelineId={}",
                                    entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId(), e);
                            wordCount = 0;
                        }

                        int blankPageThreshold = Integer.parseInt(
                                action.getContext().getOrDefault(PAGE_CONTENT_MIN_LENGTH, "10"));
                        boolean isBlankPage = wordCount < blankPageThreshold;

                        log.info(aMarker, "API completed | originId={} paperNo={} rootPipelineId={} "
                                        + "wordCount={} threshold={} isBlank={} bboxCount={}",
                                entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId(),
                                wordCount, blankPageThreshold, isBlankPage, bboxCount);

                        String encryptSotPageContent = action.getContext().get(ENCRYPT_DEEP_SIFT_OUTPUT);
                        String finalExtractedContent = extractedContent;
                        String finalBboxListJson = bboxListJson;
                        if ("true".equals(encryptSotPageContent)) {
                            InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);
                            finalExtractedContent = encryption.encrypt(extractedContent, ENCRYPTION_ALGORITHM,
                                    TEXT_DATA_TYPE);
                            if (bboxListJson != null && !bboxListJson.isEmpty()) {
                                finalBboxListJson = encryption.encrypt(bboxListJson, ENCRYPTION_ALGORITHM,
                                        TEXT_DATA_TYPE);
                            }
                        }

                        if (modelResponse.getOriginId() == null || modelResponse.getGroupId() == null
                                || modelResponse.getTenantId() == null || modelResponse.getRootPipelineId() == null) {
                            log.error(aMarker, "Invalid response, missing fields | originId={} paperNo={} "
                                            + "rootPipelineId={} model={}",
                                    entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId(),
                                    entity.getModelName());
                            return;
                        }

                        parentObj.add(DeepSiftOutputTable.builder()
                                .inputFilePath(entity.getInputFilePath())
                                .extractedText(finalExtractedContent)
                                .extractedTextWithBbox(finalBboxListJson)
                                .originId(modelResponse.getOriginId())
                                .groupId(modelResponse.getGroupId().intValue())
                                .paperNo(entity.getPaperNo())
                                .createdOn(entity.getCreatedOn())
                                .createdBy(entity.getTenantId().toString())
                                .rootPipelineId(modelResponse.getRootPipelineId())
                                .tenantId(modelResponse.getTenantId())
                                .batchId(modelResponse.getBatchId())
                                .sourceDocumentType(entity.getSourceDocumentType())
                                .modelId(entity.getModelId())
                                .modelName(modelResponse.getModelName())
                                .timeTakenMS(elapsedTimeMs)
                                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                                .request(encryptRequestResponse(dbJsonRequest))
                                .response(encryptRequestResponse(responseBody))
                                .endpoint(String.valueOf(endpoint))
                                .wordCount(wordCount)
                                .isBlankPage(isBlankPage)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error(aMarker, "Request processing exception | originId={} paperNo={} rootPipelineId={} model={}",
                    entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId(),
                    entity.getModelName(), e);
            HandymanException handymanException = new HandymanException(
                    "Deep sift consumer failed for model " + entity.getModelName(), e);
            HandymanException.insertException(
                    "Deep sift consumer failed | originId=" + entity.getOriginId()
                            + " paperNo=" + entity.getPaperNo()
                            + " rootPipelineId=" + entity.getRootPipelineId()
                            + " model=" + entity.getModelName(),
                    handymanException, action);
        }
    }

    private CoproRetryErrorAuditTable setErrorAuditInputDetails(DeepSiftInputTable entity, URL endPoint) {
        return CoproRetryErrorAuditTable.builder()
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .paperNo(entity.getPaperNo())
                .groupId(entity.getGroupId() != null ? Math.toIntExact(entity.getGroupId()) : null)
                .tenantId(entity.getTenantId())
                .processId(entity.getRootPipelineId())
                .filePath(entity.getInputFilePath())
                .createdOn(entity.getCreatedOn())
                .rootPipelineId(entity.getRootPipelineId())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(PROCESS_NAME)
                .batchId(entity.getBatchId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .endpoint(String.valueOf(endPoint))
                .requestId(entity.getRequestId().toString())
                .build();
    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        if ("true".equals(encryptReqRes)) {
            InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);
            return encryption.encrypt(request, ENCRYPTION_ALGORITHM, TEXT_DATA_TYPE);
        }
        return request;
    }
}