package in.handyman.raven.lib.model.textextraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
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

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class DataExtractionConsumerProcess implements CoproProcessor.ConsumerProcess<DataExtractionInputTable, DataExtractionOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private static final String PROCESS_NAME = "DATA_EXTRACTION";

    final OkHttpClient httpclient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.MINUTES).writeTimeout(10, TimeUnit.MINUTES).readTimeout(10, TimeUnit.MINUTES).build();

    private static final ObjectMapper mapper = new ObjectMapper();

    public DataExtractionConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }


    @Override
    public List<DataExtractionOutputTable> process(URL endpoint, DataExtractionInputTable entity) throws JsonProcessingException {
        List<DataExtractionOutputTable> parentObj = new ArrayList<>();

        String inputFilePath = entity.getFilePath();
        Long rootpipelineId = entity.getRootPipelineId();
        // Long actionId = action.getActionId();
        String process = "TEXT_EXTRACTOR";
        String filePath = String.valueOf(entity.getFilePath());
        ObjectMapper objectMapper = new ObjectMapper();
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        Integer paperNumber = entity.getPaperNo();
        String processId = String.valueOf(entity.getProcessId());
        Long tenantId = entity.getTenantId();
        Long actionId = action.getActionId();

        //payload
        DataExtractionData dataExtractionData = new DataExtractionData();
        dataExtractionData.setOriginId(originId);
        dataExtractionData.setGroupId(groupId);
        dataExtractionData.setProcessId(Long.valueOf(processId));
        dataExtractionData.setTenantId(tenantId);
        dataExtractionData.setRootPipelineId(rootpipelineId);
        dataExtractionData.setActionId(actionId);
        dataExtractionData.setPaperNumber(paperNumber);
        dataExtractionData.setTemplateName(entity.getTemplateName());
        dataExtractionData.setProcess(process);
        dataExtractionData.setInputFilePath(filePath);
        String jsonInputRequest = objectMapper.writeValueAsString(dataExtractionData);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("TEXT EXTRACTOR START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} ", endpoint, inputFilePath);
        }

        String tritonRequestActivator = action.getContext().get("triton.request.activator");

        if (Objects.equals("false", tritonRequestActivator)) {
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, mediaType)).build();
            coproRequestBuilder(entity, request, parentObj, originId, groupId);
        } else {
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, mediaType)).build();
            tritonRequestBuilder(entity, request, parentObj, originId, groupId);
        }


        return parentObj;
    }

    private void coproRequestBuilder(DataExtractionInputTable entity, Request request, List<DataExtractionOutputTable> parentObj, String originId, Integer groupId) {
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        Long rootPipelineId = entity.getRootPipelineId();
        String templateName = entity.getTemplateName();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            if (response.isSuccessful()) {
                extractedCoproOutputResponse(entity, responseBody, parentObj, originId, groupId, "", "");

            } else {
                parentObj.add(DataExtractionOutputTable.builder().originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).message(response.message()).createdOn(Timestamp.valueOf(LocalDateTime.now())).rootPipelineId(rootPipelineId).templateName(templateName).build());
                log.error(aMarker, "The Exception occurred in response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(DataExtractionOutputTable.builder().originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).createdOn(Timestamp.valueOf(LocalDateTime.now())).message(ExceptionUtil.toString(e)).rootPipelineId(rootPipelineId).templateName(templateName).build());

            log.error(aMarker, "The Exception occurred {} ", e.toString());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("test extraction consumer failed for batch/group " + groupId, handymanException, this.action);

        }
    }

    private void tritonRequestBuilder(DataExtractionInputTable entity, Request request, List<DataExtractionOutputTable> parentObj, String originId, Integer groupId) {
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        Long rootPipelineId = entity.getRootPipelineId();
        String templateName = entity.getTemplateName();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            if (response.isSuccessful()) {
                ObjectMapper objectMappers = new ObjectMapper();
                DataExtractionResponse modelResponse = objectMappers.readValue(responseBody, DataExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(s -> {
                            try {
                                extractedOutputDataRequest(entity, s, parentObj, originId, groupId, modelResponse.getModelName(), modelResponse.getModelVersion());
                            } catch (JsonProcessingException e) {
                                throw new HandymanException("Exception in extracted output Data request {}", e);
                            }
                        });

                    });
                }
            } else {
                parentObj.add(DataExtractionOutputTable.builder().originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).message(response.message()).createdOn(Timestamp.valueOf(LocalDateTime.now())).rootPipelineId(rootPipelineId).templateName(templateName).build());
                log.error(aMarker, "The Exception occurred ");
            }
        } catch (Exception e) {
            parentObj.add(DataExtractionOutputTable.builder().originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).createdOn(Timestamp.valueOf(LocalDateTime.now())).message(ExceptionUtil.toString(e)).rootPipelineId(rootPipelineId).templateName(templateName).build());

            log.error(aMarker, "The Exception occurred ", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("test extraction consumer failed for batch/group " + groupId, handymanException, this.action);

        }
    }

    private static void extractedOutputDataRequest(DataExtractionInputTable entity, String stringDataItem, List<DataExtractionOutputTable> parentObj, String originId, Integer groupId, String modelName, String modelVersion) throws JsonProcessingException {
        JsonNode jsonNode = mapper.readTree(stringDataItem);
        String pageContent = jsonNode.get("pageContent").asText();
        final String contentString = Optional.of(pageContent).map(String::valueOf).orElse(null);
        final String flag = (contentString.length() > 5) ? "no" : "yes";
        DataExtractionDataItem dataExtractionDataItem = mapper.readValue(stringDataItem, DataExtractionDataItem.class);
        String templateId = entity.getTemplateId();
        parentObj.add(DataExtractionOutputTable.builder().filePath(dataExtractionDataItem.getInputFilePath()).extractedText(dataExtractionDataItem.getPageContent()).originId(dataExtractionDataItem.getOriginId()).groupId(dataExtractionDataItem.getGroupId()).paperNo(dataExtractionDataItem.getPaperNumber()).status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).stage(PROCESS_NAME).message("Data extraction macro completed").createdOn(Timestamp.valueOf(LocalDateTime.now())).isBlankPage(flag).tenantId(dataExtractionDataItem.getTenantId()).templateId(templateId).processId(dataExtractionDataItem.getProcessId()).templateName(dataExtractionDataItem.getTemplateName()).rootPipelineId(dataExtractionDataItem.getRootPipelineId()).modelName(modelName).modelVersion(modelVersion).build());
    }

    private static void extractedCoproOutputResponse(DataExtractionInputTable entity, String stringDataItem, List<DataExtractionOutputTable> parentObj, String originId, Integer groupId, String modelName, String modelVersion) throws JsonProcessingException {

        String parentResponseObject = extractPageContent(stringDataItem);
        final String contentString = Optional.of(parentResponseObject).map(String::valueOf).orElse(null);
        final String flag = (!Objects.isNull(contentString) && contentString.length() > 5) ? "no" : "yes";
        Integer paperNo = entity.getPaperNo();
        String filePath = entity.getFilePath();
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        String templateName = entity.getTemplateName();
        Long rootPipelineId = entity.getRootPipelineId();
        parentObj.add(DataExtractionOutputTable.builder().filePath(new File(filePath).getAbsolutePath()).extractedText(contentString).originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).paperNo(paperNo).status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).stage(PROCESS_NAME).message("Data extraction macro completed").createdOn(Timestamp.valueOf(LocalDateTime.now())).isBlankPage(flag).tenantId(tenantId).templateId(templateId).processId(processId).templateName(templateName).rootPipelineId(rootPipelineId).modelName(modelName).modelVersion(modelVersion).build());


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

}

