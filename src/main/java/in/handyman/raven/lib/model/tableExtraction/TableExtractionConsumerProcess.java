package in.handyman.raven.lib.model.tableExtraction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.paperItemizer.PaperItemizerDataItem;
import in.handyman.raven.lib.model.paperItemizer.PaperItemizerResponse;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.lib.model.zeroShotClassifier.ZeroShotClassifierDataItem;
import in.handyman.raven.util.UniqueID;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.postgresql.core.JavaVersion.other;

public class TableExtractionConsumerProcess implements CoproProcessor.ConsumerProcess<TableExtractionInputTable, TableExtractionOutputTable> {

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    private final String outputDir;

    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public TableExtractionConsumerProcess(Logger log, Marker aMarker, String outputDir, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.outputDir = outputDir;
        this.action = action;
    }

    @Override
    public List<TableExtractionOutputTable> process(URL endpoint, TableExtractionInputTable entity) throws Exception {
        log.info(aMarker, "coproProcessor consumer process started with endpoint {} and entity {}", endpoint, entity);
        List<TableExtractionOutputTable> parentObj = new ArrayList<>();

        String inputFilePath = entity.getFilePath();
        Long uniqueId = UniqueID.getId();
        String uniqueIdStr = String.valueOf(uniqueId);
        String outputDirectory = outputDir.concat("/").concat(String.valueOf(entity.getRootPipelineId())).concat("/").concat(entity.getOriginId()).concat("/").concat(uniqueIdStr);
        Long rootPipelineId = entity.getRootPipelineId();
        final String tableExtractionProcessName = "TABLE_EXTRACTION";
        Long actionId = action.getActionId();
        ObjectMapper objectMapper = new ObjectMapper();


        TableExtractionPayload tableExtractionPayload = new TableExtractionPayload();
        tableExtractionPayload.setInputFilePath(inputFilePath);
        tableExtractionPayload.setOutputDirectory(outputDir);
        tableExtractionPayload.setRootPipelineId(rootPipelineId);
        tableExtractionPayload.setActionId(actionId);
        tableExtractionPayload.setProcess(tableExtractionProcessName);
        String jsonInputRequest = objectMapper.writeValueAsString(tableExtractionPayload);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("TABLE EXTRACTION START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));
        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);




        log.info(aMarker, "coproProcessor mapper object node {}", jsonRequest);
        Request request = new Request.Builder().url(endpoint)
                .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();

        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} and outputDir {}", endpoint, inputFilePath, outputDirectory);
        }
        AtomicInteger atomicInteger = new AtomicInteger();
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        String templateId = entity.getTemplateId();
        String tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();


        try (Response response = httpclient.newCall(request).execute()) {

            if (log.isInfoEnabled())
                log.info(aMarker, "coproProcessor consumer process response with status{}, and message as {}, ", response.isSuccessful(), response.message());
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                ObjectMapper objectMappers = new ObjectMapper();
                TableExtractionResponse tableExtractionResponse = objectMappers.readValue(responseBody, TableExtractionResponse.class);
                if (tableExtractionResponse.getOutputs() != null && !tableExtractionResponse.getOutputs().isEmpty()) {
                    tableExtractionResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(tableExtractionDataItem ->
                        {
                            TableExtractionDataItem tableExtractionDataItem1 = null;
                            try {
                                tableExtractionDataItem1 = objectMapper.readValue(tableExtractionDataItem, TableExtractionDataItem.class);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                            if (tableExtractionDataItem1.getCsvTablesPath() != null && !tableExtractionDataItem1.getCsvTablesPath().isEmpty()) {
                                TableExtractionDataItem finalTableExtractionDataItem = tableExtractionDataItem1;
                                TableExtractionDataItem finalTableExtractionDataItem1 = tableExtractionDataItem1;
                                tableExtractionDataItem1.getCsvTablesPath().forEach(key -> {

                            if (finalTableExtractionDataItem.getTableResponse() != null && !finalTableExtractionDataItem.getTableResponse().getPayload().isEmpty()) {
                                finalTableExtractionDataItem.getTableResponse().getPayload().forEach(payloadItem -> {
                                    List<Integer> columns = payloadItem.getTableData().getColumns();
                                    List<List<String>> data = payloadItem.getTableData().getData();

                            parentObj.add(
                                    TableExtractionOutputTable
                                            .builder()
                                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                            .groupId(groupId)
                                            .templateId(templateId)
                                            .tenantId(tenantId)
                                            .processId(processId)
                                            .columns(columns)
                                            .data(data)
                                            .csvTablesPath(finalTableExtractionDataItem1.getCsvTablesPath())
                                            .paperNo(atomicInteger.incrementAndGet())
                                            .status("COMPLETED")
                                            .stage(tableExtractionProcessName)
                                            .message("Table Extraction macro completed")
                                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                            .rootPipelineId(rootPipelineId)
                                            .build());
                                });
                            }
                                });
                            }

                        });
                    });

                }


            } else {
                parentObj.add(
                        TableExtractionOutputTable
                                .builder()
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .groupId(groupId)
                                .processId(processId)
                                .templateId(templateId)
                                .tenantId(tenantId)
                                .paperNo(atomicInteger.incrementAndGet())
                                .status("FAILED")
                                .stage(tableExtractionProcessName)
                                .message(response.message())
                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                .rootPipelineId(rootPipelineId)
                                .build());
                log.error(aMarker, "Error in response {}", response.message());
            }

        } catch (Exception exception) {
            parentObj.add(
                    TableExtractionOutputTable
                            .builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .groupId(groupId)
                            .processId(processId)
                            .templateId(templateId)
                            .tenantId(tenantId)
                            .paperNo(atomicInteger.incrementAndGet())
                            .status("FAILED")
                            .stage(tableExtractionProcessName)
                            .message(exception.getMessage())
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .rootPipelineId(rootPipelineId)
                            .build());
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Table Extraction  consumer failed for originId " + originId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in request {}", request, exception);
        }
        atomicInteger.set(0);
        log.info(aMarker, "coproProcessor consumer process with output entity {}", parentObj);
        return parentObj;
    }
}

