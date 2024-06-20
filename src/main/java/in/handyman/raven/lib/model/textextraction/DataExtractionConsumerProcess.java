package in.handyman.raven.lib.model.textextraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DataExtractionConsumerProcess implements CoproProcessor.ConsumerProcess<DataExtractionInputTable, DataExtractionOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private static final String PROCESS_NAME = "DATA_EXTRACTION";
    private static final String FAILED_STR = "FAILED";

    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

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
        Long actionId = action.getActionId();
        String process = "TEXT_EXTRACTOR";
        String filePath = String.valueOf(entity.getFilePath());
        ObjectMapper objectMapper = new ObjectMapper();


        //payload
        DataExtractionData dataExtractionData = new DataExtractionData();
        dataExtractionData.setRootPipelineId(rootpipelineId);
        dataExtractionData.setActionId(actionId);
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

        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();

        String tritonRequestActivator = action.getContext().get("triton.request.activator");

        if (Objects.equals("true", tritonRequestActivator)) {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonInputRequest, mediaType)).build();
            coproRequestBuilder(entity, request, parentObj, originId, groupId);
        } else {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonRequest, mediaType)).build();
            tritonRequestBuilder(entity, request, parentObj, originId, groupId);
        }


        return parentObj;
    }

    private void coproRequestBuilder(DataExtractionInputTable entity, Request request, List<DataExtractionOutputTable> parentObj, String originId, Integer groupId) {
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            ObjectMapper mapper=new ObjectMapper();
            DataExtractionDataItem dataExtractionDataItem=mapper.readValue(responseBody,DataExtractionDataItem.class);
            if (response.isSuccessful()) {
                extractedOutputDataRequest(entity, dataExtractionDataItem.getPageContent(), parentObj, originId, groupId, "", "");

            } else {
                parentObj.add(DataExtractionOutputTable.builder()
                        .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                        .groupId(groupId)
                        .paperNo(entity.getPaperNo())
                        .status(FAILED_STR)
                        .stage(PROCESS_NAME)
                        .tenantId(entity.getTenantId())
                        .templateId(entity.getTemplateId())
                        .processId(entity.getProcessId())
                        .message(response.message())
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .rootPipelineId(entity.getRootPipelineId())
                        .templateName(entity.getTemplateName())
                        .build());
                log.error(aMarker, "The Exception occurred in response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(DataExtractionOutputTable.builder()
                    .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .paperNo(entity.getPaperNo())
                    .status(FAILED_STR)
                    .stage(PROCESS_NAME)
                    .tenantId(entity.getTenantId())
                    .templateId(entity.getTemplateId())
                    .processId(entity.getProcessId())
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .message(ExceptionUtil.toString(e))
                    .rootPipelineId(entity.getRootPipelineId())
                    .templateName(entity.getTemplateName())
                    .build());

            log.error(aMarker, "The Exception occurred {} ", e.toString());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("test extraction consumer failed for batch/group " + groupId, handymanException, this.action);

        }
    }

    private void tritonRequestBuilder(DataExtractionInputTable entity, Request request, List<DataExtractionOutputTable> parentObj, String originId, Integer groupId) {
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            if (response.isSuccessful()) {
                ObjectMapper objectMappers = new ObjectMapper();
                DataExtractionResponse modelResponse = objectMappers.readValue(responseBody, DataExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> {
                        JSONArray jsonArrayObj = new JSONArray(o.getData());
                        extractedOutputDataRequest(entity, jsonArrayObj.toString(), parentObj, originId, groupId, modelResponse.getModelName(), modelResponse.getModelVersion());

                    });
                }
            } else {
                parentObj.add(DataExtractionOutputTable.builder()
                        .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                        .groupId(groupId)
                        .paperNo(entity.getPaperNo())
                        .status(FAILED_STR)
                        .stage(PROCESS_NAME)
                        .tenantId(entity.getTenantId())
                        .templateId(entity.getTemplateId())
                        .processId(entity.getProcessId())
                        .message(response.message())
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .rootPipelineId(entity.getRootPipelineId())
                        .templateName(entity.getTemplateName())
                        .build());
                log.error(aMarker, "The Exception occurred ");
            }
        } catch (Exception e) {
            parentObj.add(DataExtractionOutputTable.builder()
                    .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .paperNo(entity.getPaperNo())
                    .status(FAILED_STR)
                    .stage(PROCESS_NAME)
                    .tenantId(entity.getTenantId())
                    .templateId(entity.getTemplateId())
                    .processId(entity.getProcessId())
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .message(ExceptionUtil.toString(e))
                    .rootPipelineId(entity.getRootPipelineId())
                    .templateName(entity.getTemplateName())
                    .build());

            log.error(aMarker, "The Exception occurred ", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("test extraction consumer failed for batch/group " + groupId, handymanException, this.action);

        }
    }

    private static void extractedOutputDataRequest(DataExtractionInputTable entity, String stringDataItem, List<DataExtractionOutputTable> parentObj, String originId, Integer groupId, String modelName, String modelVersion) {



        final String flag = (!Objects.isNull(stringDataItem) && stringDataItem.length() > 5) ? "no" : "yes";
        parentObj.add(DataExtractionOutputTable.builder()
                .filePath(new File(entity.getFilePath()).getAbsolutePath())
                .extractedText(stringDataItem)
                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                .groupId(groupId)
                .paperNo(entity.getPaperNo())
                .status("COMPLETED")
                .stage(PROCESS_NAME)
                .message("Data extraction macro completed")
                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                .isBlankPage(flag)
                .tenantId(entity.getTenantId())
                .templateId(entity.getTemplateId())
                .processId(entity.getProcessId())
                .templateName(entity.getTemplateName())
                .rootPipelineId(entity.getRootPipelineId())
                .modelName(modelName)
                .modelVersion(modelVersion)
                .build());
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

