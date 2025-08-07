package in.handyman.raven.lib.model.agentic.paper.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.AgenticPaperFilterAction;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpExtractionRequest;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpExtractionResponse;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpLineItem;
import in.handyman.raven.lib.model.textextraction.DataExtractionData;
import in.handyman.raven.lib.model.textextraction.DataExtractionDataItem;
import in.handyman.raven.lib.model.textextraction.DataExtractionResponse;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.lib.replicate.ReplicateRequest;
import in.handyman.raven.lib.replicate.ReplicateResponse;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.core.utils.ProcessFileFormatE;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jdbi.v3.core.Jdbi;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_AGENTIC_FILTER_OUTPUT;
import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class AgenticPaperFilterConsumerProcess implements CoproProcessor.ConsumerProcess<AgenticPaperFilterInput, AgenticPaperFilterOutput> {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final Marker aMarker;

    public static final String AGENTIC_PAPER_FILTER_START = "AGENTIC PAPER FILTER START";
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    public static final String KRYPTON_START = "KRYPTON START";
    public static final String REQUEST_ACTIVATOR_HANDLER_NAME = "copro.request.agentic.paper.filter.extraction.handler.name";
    public static final String REPLICATE_API_TOKEN_CONTEXT = "replicate.request.api.token";
    public static final String AGENTIC_PAPER_FILTER_MODEL_NAME = "preprocess.agentic.paper.filter.model.name";
    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
    private static final String MODEL_TYPE = "OPTIMUS";
    private static final String PROCESS_NAME = "AGENTIC PAPER FILTER";
    public static final String PAGE_CONTENT_NO = "no";
    public static final String PAGE_CONTENT_YES = "yes";
    private final int pageContentMinLength;
    final OkHttpClient httpclient ;
    final String jdbiResourceName ;
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String processBase64;
    private final FileProcessingUtils fileProcessingUtils;


    public AgenticPaperFilterConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, AgenticPaperFilterAction aAction, Integer pageContentMinLength, FileProcessingUtils fileProcessingUtils, String processBase64, String jdbiResourceName) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.processBase64 = processBase64;
        this.fileProcessingUtils = fileProcessingUtils;
        this.pageContentMinLength = pageContentMinLength;
        int timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder().connectTimeout(timeOut, TimeUnit.MINUTES).writeTimeout(timeOut, TimeUnit.MINUTES).readTimeout(timeOut, TimeUnit.MINUTES).build();
        this.jdbiResourceName = jdbiResourceName;
    }

    @Override
    public List<AgenticPaperFilterOutput> process(URL endpoint, AgenticPaperFilterInput entity) throws IOException {
        List<AgenticPaperFilterOutput> parentObj = new ArrayList<>();

        String textExtractionModelName = action.getContext().get(AGENTIC_PAPER_FILTER_MODEL_NAME);

        String inputFilePath = entity.getFilePath();
        String filePath = String.valueOf(entity.getFilePath());


        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} ", endpoint, inputFilePath);
        }

        getCoproHandlerMethod(endpoint, entity, parentObj, textExtractionModelName, filePath);

        return parentObj;
    }

    private void getCoproHandlerMethod(URL endpoint, AgenticPaperFilterInput entity, List<AgenticPaperFilterOutput> parentObj, String textExtractionModelName, String filePath) throws IOException {
        log.info(aMarker, "Executing TRITON handler for endpoint: {} and model: {}", endpoint, textExtractionModelName);
        getTritonHandlerMethod(endpoint, entity, parentObj, filePath);
    }

    private void getTritonHandlerMethod(URL endpoint, AgenticPaperFilterInput entity, List<AgenticPaperFilterOutput> parentObj, String filePath) throws IOException {
        RadonKvpExtractionRequest kryptonRequestPayloadFromQuery = getKryptonRequestPayloadFromQuery(entity);
        getTritonKryptonHandlerName(endpoint, entity, parentObj, filePath, kryptonRequestPayloadFromQuery);
    }

    private void getTritonKryptonHandlerName(URL endpoint, AgenticPaperFilterInput entity, List<AgenticPaperFilterOutput> parentObj, String filePath, RadonKvpExtractionRequest kryptonRequestPayloadFromQuery) throws IOException {
        if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
            kryptonRequestPayloadFromQuery.setBase64Img(fileProcessingUtils.convertFileToBase64(filePath));
        } else {
            kryptonRequestPayloadFromQuery.setBase64Img("");
        }
        String textExtractionPayloadString = mapper.writeValueAsString(kryptonRequestPayloadFromQuery);
        kryptonRequestPayloadFromQuery.setBase64Img("");
        String textExtractionInsertPayloadString = mapper.writeValueAsString(kryptonRequestPayloadFromQuery);
        String jsonRequestTritonKrypton = getTritonRequestPayload(textExtractionPayloadString, KRYPTON_START, mapper);

        Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequestTritonKrypton, mediaType)).build();
        tritonRequestKryptonExecutor(entity, request, parentObj, textExtractionInsertPayloadString, endpoint);
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
        radonKvpExtractionRequest.setModelName(action.getContext().get("agentic.paper.filter.activator").equalsIgnoreCase("true")?"KRYPTON":entity.getModelName());
        return radonKvpExtractionRequest;
    }

    private void tritonRequestKryptonExecutor(AgenticPaperFilterInput entity, Request request, List<AgenticPaperFilterOutput> parentObj, String jsonRequest, URL endpoint) {
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        Long rootPipelineId = entity.getRootPipelineId();
        String templateName = entity.getTemplateName();
        CoproRetryErrorAuditTable auditInput = setErrorAudictInputDetails(entity, endpoint);

        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                RadonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, RadonKvpExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(s -> {
                            try {
                                extractedKryptonOutputDataRequest(entity, s, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion(), jsonRequest, responseBody, endpoint.toString());
                            } catch (JsonProcessingException e) {
                                String errorMessage = "Error in parsing response in consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo() + " code : " + response.code() + " message : " + response.message();
                                HandymanException handymanException = new HandymanException(errorMessage);
                                HandymanException.insertException(errorMessage, handymanException, this.action);
                                log.error(aMarker, errorMessage);
                            }
                        });
                    });
                } else {
                    String errorMessage = "Successful response in consumer but output node not found for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo() + " code : " + response.code() + "\n message : " + response.message();
                    handleKryptonErrorResponse(entity, parentObj, jsonRequest, endpoint, tenantId, templateId, processId, response, rootPipelineId, templateName, responseBody);
                    HandymanException handymanException = new HandymanException(errorMessage);
                    HandymanException.insertException(errorMessage, handymanException, this.action);
                    log.error(aMarker, errorMessage);
                }
            } else {
                String errorBody = responseBody;
                String errorMessage = "Unsuccessful response in consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo() + " code : " + response.code() + "\n message : " + errorBody;
                handleKryptonErrorResponse(entity, parentObj, jsonRequest, endpoint, tenantId, templateId, processId, response, rootPipelineId, templateName, responseBody);
                HandymanException handymanException = new HandymanException(errorMessage);
                HandymanException.insertException(errorMessage, handymanException, this.action);
                log.error(aMarker, errorMessage);
            }
        } catch (Exception e) {
            String errorMessage = "Error in api call consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo() + "\n message : " + e.getMessage();
            parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).message(errorMessage).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonRequest)).response("Error In Response").endpoint(String.valueOf(endpoint)).build());
            log.error(aMarker, errorMessage);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(errorMessage, handymanException, this.action);
        }
    }

    private CoproRetryErrorAuditTable setErrorAudictInputDetails(AgenticPaperFilterInput entity, URL endPoint) {
        CoproRetryErrorAuditTable retryAudict = CoproRetryErrorAuditTable.builder()
                .originId(entity.getOriginId())
                .groupId(Math.toIntExact(entity.getGroupId()))
                .tenantId(entity.getTenantId())
                .processId(entity.getProcessId())
                .filePath(entity.getFilePath())
                .paperNo(entity.getPaperNo())
                .createdOn(entity.getCreatedOn())
                .rootPipelineId(entity.getRootPipelineId())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage("RETRY API CALL TO COPRO")
                .batchId(entity.getBatchId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .endpoint(String.valueOf(endPoint))
                .build();
        return retryAudict;
    }

    private void handleKryptonErrorResponse(AgenticPaperFilterInput entity, List<AgenticPaperFilterOutput> parentObj, String jsonRequest, URL endpoint, Long tenantId, String templateId, Long processId, Response response, Long rootPipelineId, String templateName, String responseBody) {
        parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonRequest)).response(encryptRequestResponse(responseBody)).endpoint(String.valueOf(endpoint)).build());
    }


    private void extractedKryptonOutputDataRequest(AgenticPaperFilterInput entity, String stringDataItem,
                                                   List<AgenticPaperFilterOutput> parentObj, String modelName,
                                                   String modelVersion, String request, String response,
                                                   String endpoint) throws JsonProcessingException {

        String cleanedJson = stringDataItem.replaceAll("```json", "").replaceAll("```", "").trim();

        JSONObject json = new JSONObject(cleanedJson);
        JsonNode inferResponseNode = null;
        String formattedInferResponse = "";
        RadonKvpLineItem dataExtractionDataItem = mapper.readValue(cleanedJson, RadonKvpLineItem.class);
        String inferResponseJson = dataExtractionDataItem.getInferResponse();

        if (json.has("model")) {
            String modelValue = json.getString("model");
            if (!MODEL_TYPE.equalsIgnoreCase(modelValue)) {
                //KRYPTON
                inferResponseNode = mapper.readTree(inferResponseJson);
            }else{
                inferResponseNode = TextNode.valueOf(inferResponseJson.trim());
            }
        }
        formattedInferResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inferResponseNode);
        String flag = (inferResponseJson.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;

        String encryptSotPageContent = action.getContext().get(ENCRYPT_AGENTIC_FILTER_OUTPUT);
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action,log);

        String extractedContent = Objects.equals(encryptSotPageContent, "true")
                ? encryption.encrypt(formattedInferResponse, "AES256", "AGENTIC_INFER_OUTPUT")
                : formattedInferResponse;

        String templateId = entity.getTemplateId();

        assert inferResponseNode != null;
        Iterator<Map.Entry<String, JsonNode>> fields = inferResponseNode.fields();

        if(MODEL_TYPE.equalsIgnoreCase(json.getString("model"))){
            //single insert
            parentObj.add(AgenticPaperFilterOutput.builder()
                    .filePath(entity.getFilePath())
                    .extractedText(extractedContent)
                    .originId(dataExtractionDataItem.getOriginId())
                    .groupId(dataExtractionDataItem.getGroupId()!=null?Math.toIntExact(dataExtractionDataItem.getGroupId()):0)
                    .paperNo(dataExtractionDataItem.getPaperNo())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message("Agentic Paper Filter macro completed with krypton triton api call "+ entity.getModelName())
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
                    .request(encryptRequestResponse(request))
                    .response(encryptRequestResponse(response))
                    .endpoint(String.valueOf(endpoint))
                    .containerValue("yes".equalsIgnoreCase(inferResponseNode.asText())?"true":"false")
                    .containerName(entity.getUniqueName())
                    .containerId(entity.getUniqueId())
                    .promptType(entity.getPromptType())
                    .build());

        }else{
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String containerName = entry.getKey();
                String containerValue = entry.getValue().asText();

                parentObj.add(AgenticPaperFilterOutput.builder()
                        .filePath(entity.getFilePath())
                        .extractedText(extractedContent)
                        .originId(dataExtractionDataItem.getOriginId())
                        .groupId(Math.toIntExact(dataExtractionDataItem.getGroupId()))
                        .paperNo(dataExtractionDataItem.getPaperNo())
                        .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .message("Agentic Paper Filter macro completed with krypton triton api call "+ entity.getModelName())
                        .createdOn(entity.getCreatedOn())
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .isBlankPage(flag)
                        .tenantId(dataExtractionDataItem.getTenantId())
                        .templateId(templateId)
                        .processId(dataExtractionDataItem.getProcessId())
                        .templateName(entity.getTemplateName())
                        .rootPipelineId(dataExtractionDataItem.getRootPipelineId())
                        .modelName(entity.getModelName() != null ? entity.getModelName():modelName)
                        .modelVersion(modelVersion)
                        .batchId(entity.getBatchId())
                        .request(encryptRequestResponse(request))
                        .response(encryptRequestResponse(response))
                        .endpoint(String.valueOf(endpoint))
                        .containerName(containerName)
                        .containerValue(containerValue)
                        .build());
            }
        }

    }

    private static String getTritonRequestPayload(String dataExtractionPayloadString, String name, ObjectMapper objectMapper) throws JsonProcessingException {
        TritonRequest tritonRequestPayload = new TritonRequest();
        tritonRequestPayload.setName(name);
        tritonRequestPayload.setShape(List.of(1, 1));
        tritonRequestPayload.setDatatype(TritonDataTypes.BYTES.name());
        tritonRequestPayload.setData(Collections.singletonList(dataExtractionPayloadString));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(tritonRequestPayload));

        return objectMapper.writeValueAsString(tritonInputRequest);
    }

    public JsonNode convertFormattedJsonStringToJsonNode(String jsonResponse, ObjectMapper objectMapper) {
        try {
            if (jsonResponse.contains("```json")) {
                log.info("Input contains the required ```json``` markers. So processing it based on the ```json``` markers.");
                // Define the regex pattern to match content between ```json and ```
                Pattern pattern = Pattern.compile("(?s)```json\\s*(.*?)\\s*```");
                Matcher matcher = pattern.matcher(jsonResponse);

                if (matcher.find()) {
                    // Extract the JSON string from the matched group
                    String jsonString = matcher.group(1);
                    jsonString = jsonString.replace("\n", "");
                    // Convert the cleaned JSON string to a JsonNode

                    if (!jsonResponse.isEmpty()) {
                        return objectMapper.readTree(jsonResponse);
                    } else {
                        return null;
                    }
                } else {

                    return objectMapper.readTree(jsonResponse);
                }
            } else if (jsonResponse.contains("{")) {
                log.info("Input does not contain the required ```json``` markers. So processing it based on the indication of object literals.");

                return objectMapper.readTree(jsonResponse);
                //throw new IllegalArgumentException("Input does not contain the required ```json``` markers.");
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
            String encryptedRequest = SecurityEngine.getInticsIntegrityMethod(action,log).encrypt(request, "AES256", "AP_COPRO_REQUEST");
            requestStr = encryptedRequest;
        } else {
            requestStr = request;
        }
        return requestStr;
    }
}