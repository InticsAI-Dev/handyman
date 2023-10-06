package in.handyman.raven.lib.model.TextExtraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.lib.HwDetectionAction.httpClientTimeout;

public class DataExtractionConsumerProcess implements CoproProcessor.ConsumerProcess<DataExtractionInputTable, DataExtractionOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
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
        DataExtractionData DataExtractiondata = new DataExtractionData();
        DataExtractiondata.setRootPipelineId(Long.valueOf(rootpipelineId));
        DataExtractiondata.setActionId(actionId);
        DataExtractiondata.setProcess(process);
        DataExtractiondata.setInputFilePath(filePath);
        String jsonInputRequest = objectMapper.writeValueAsString(DataExtractiondata);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("TEXT EXTRACTOR START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();

        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} ", endpoint, inputFilePath);
        }

        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();

        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            if (response.isSuccessful()) {
                ObjectMapper objectMappers = new ObjectMapper();
                DataExtractionResponse modelResponse = objectMappers.readValue(responseBody, DataExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> {
                        JSONArray jsonArrayObj = new JSONArray(o.getData());
                        String pageContent = jsonArrayObj.getString(0);
                        String pageCont = extractPageContent(pageContent);
                        String dataExtractionDataItem1 = StringEscapeUtils.escapeJava(pageCont);

                        final String flag=(!Objects.isNull(dataExtractionDataItem1) && dataExtractionDataItem1.length() > 5 ) ? "no" : "yes";


                        parentObj.add(DataExtractionOutputTable.builder()
                                .filePath(new File(entity.getFilePath()).getAbsolutePath())
                                .extractedText(dataExtractionDataItem1)
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .groupId(groupId)
                                .paperNo(entity.getPaperNo())
                                .status("COMPLETED")
                                .stage("DATA_EXTRACTION")
                                .message("Data extraction macro completed")
                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                .isBlankPage(flag)
                                .tenantId(entity.getTenantId())
                                .templateId(entity.getTemplateId())
                                .processId(entity.getProcessId())
                                .templateName(entity.getTemplateName())
                                .rootPipelineId(entity.getRootPipelineId())
                                .modelName(modelResponse.getModelName())
                                .modelVersion(modelResponse.getModelVersion())
                                .build());


                    });
                }
            } else {
                parentObj.add(DataExtractionOutputTable.builder()
                        .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                        .groupId(groupId)
                        .paperNo(entity.getPaperNo())
                        .status("FAILED")
                        .stage("DATA_EXTRACTION")
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
                    .status("FAILED")
                    .stage("DATA_EXTRACTION")
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
        return parentObj;
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

