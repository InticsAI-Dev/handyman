package in.handyman.raven.lib;


import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.SorItemMappingExecutor;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.*;

import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.*;

import java.util.*;

public class SorItemConsumerProcess implements CoproProcessor.ConsumerProcess<SorItemMappingInputTable, SorItemMappingOutputTable>{

    private final Logger log;
    private final Marker marker;
    private final ActionExecutionAudit action;
    private final SorItemMappingExecutor sorItemExecutor;

    public SorItemConsumerProcess(Logger log, Marker marker, ActionExecutionAudit action, SorItemMappingExecutor sorItemExecutor) {
        this.log = log;
        this.marker = marker;
        this.action = action;
        this.sorItemExecutor = sorItemExecutor;
    }
    @Override
    public List<SorItemMappingOutputTable> process(URL endpoint, SorItemMappingInputTable input) throws Exception {
        List<SorItemMappingOutputTable> transformedInputs = transform(input);
        List<SorItemMappingOutputTable> sorItemMappingOutputTables = new ArrayList<>();
        Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(sorItemExecutor.getResourceConn());
        log.info(marker, "aws sor item mapping  action  started for origin Id {} paper No {} ", input.getOriginId(), input.getPaperNo());
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            final String selectQuery = sorItemExecutor.getQuerySet();
            log.info(marker, "aws sor item mapping  action {} has been started ", selectQuery);

                transformedInputs.forEach(transformedInput ->{
                    jdbi.useTransaction(handle -> {
                        final String insQuery = buildInsertQuery();
                        SorItemMappingOutputTable insertData = SorItemMappingOutputTable.builder()
                                .createdOn(transformedInput.getCreatedOn())
                                .createdUserId(transformedInput.getCreatedUserId())
                                .lastUpdatedOn(transformedInput.getLastUpdatedOn())
                                .lastUpdatedUserId(transformedInput.getLastUpdatedUserId())
                                .inputFilePath(transformedInput.getInputFilePath())
                                .inputResponseJson(transformedInput.getInputResponseJson())
                                .totalResponseJson(transformedInput.getTotalResponseJson())
                                .paperNo(transformedInput.getPaperNo())
                                .originId(transformedInput.getOriginId())
                                .processId(transformedInput.getProcessId())
                                .actionId(transformedInput.getActionId())
                                .process(transformedInput.getProcess())
                                .groupId(transformedInput.getGroupId())
                                .tenantId(transformedInput.getTenantId())
                                .rootPipelineId(transformedInput.getRootPipelineId())
                                .batchId(transformedInput.getBatchId())
                                .modelRegistry(transformedInput.getModelRegistry())
                                .status(transformedInput.getStatus())
                                .stage(transformedInput.getStage())
                                .message(transformedInput.getMessage())
                                .category(transformedInput.getCategory())
                                .request(transformedInput.getRequest())
                                .answer(transformedInput.getAnswer())
                                .endpoint(transformedInput.getEndpoint())
                                .sorItemName(transformedInput.getSorItemName())
                                .confidence(transformedInput.getConfidence())
                                .bbox(transformedInput.getBbox())
                                .build();

                        getInsertIntoOutputTable(handle, insQuery, insertData);
                });
                });


            log.info(marker, " Llm json parser action has been completed {}  ", sorItemExecutor.getName());

        } catch (Exception e) {
            action.getContext().put(sorItemExecutor.getName() + ".isSuccessful", "false");
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in execute method for Llm json parser action", handymanException, action);

        }
        return sorItemMappingOutputTables;
    }

    private int getInsertIntoOutputTable(Handle handle, String insQuery, SorItemMappingOutputTable insertData) {
        return handle.createUpdate(insQuery)
                .bind(0, insertData.getCreatedOn())
                .bind(1, insertData.getCreatedUserId())
                .bind(2, insertData.getLastUpdatedOn())
                .bind(3, insertData.getLastUpdatedUserId())
                .bind(4, insertData.getInputFilePath())
                .bind(5, insertData.getInputResponseJson())
                .bind(6, insertData.getTotalResponseJson())
                .bind(7, insertData.getPaperNo())
                .bind(8, insertData.getOriginId())
                .bind(9, insertData.getProcessId())
                .bind(10, insertData.getActionId())
                .bind(11, insertData.getProcess())
                .bind(12, insertData.getGroupId())
                .bind(13, insertData.getTenantId())
                .bind(14, insertData.getRootPipelineId())
                .bind(15, insertData.getBatchId())
                .bind(16, insertData.getModelRegistry())
                .bind(17, insertData.getStatus())
                .bind(18, insertData.getStage())
                .bind(19, insertData.getMessage())
                .bind(20, insertData.getCategory())
                .bind(21, insertData.getRequest())
                .bind(22, insertData.getAnswer())
                .bind(23, insertData.getEndpoint())
                .bind(24, insertData.getSorItemName())
                .bind(25,insertData.getConfidence())
                .bind(26,insertData.getBbox())
                .execute();
    }

    private String buildInsertQuery() {
        return "INSERT INTO " + sorItemExecutor.getOutputTable() + " (\n" +  // <-- added (
                "  created_on, created_user_id, last_updated_on, last_updated_user_id,\n" +
                "  input_file_path, input_response_json, total_response_json, paper_no, origin_id,\n" +
                "  process_id, action_id, \"process\", group_id, tenant_id, root_pipeline_id, batch_id,\n" +
                "  model_registry, status, stage, message, category, request, answer, endpoint, sor_item_name,confidence,bbox\n" +
                ") \n" +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?::jsonb);\n";
    }




    public List<SorItemMappingOutputTable> transform(SorItemMappingInputTable entity) throws JsonProcessingException {
        String inferResponse = entity.getTotalResponseJson();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonArray = mapper.readTree(inferResponse);

        List<SorItemMappingOutputTable> parentObj = new ArrayList<>();

        if (jsonArray.isArray()) {
            for (JsonNode item : jsonArray) {
                String key = item.get("key").asText().trim().replaceAll("[^a-zA-Z0-9 ]", "");
                String value = item.get("value").asText().trim();
                String confidence = item.get("confidence").asText().trim();

                String bboxJson = null;
                if (item.has("bbox") && !item.get("bbox").asText().trim().isEmpty()) {
                    String bboxString = item.get("bbox").asText().trim(); // BoundingBox(Width=0.26, Height=0.01, Left=0.21, Top=0.57)
                    bboxString = bboxString.replace("BoundingBox(", "").replace(")", "");
                    Map<String, Double> bboxMap = new HashMap<>();
                    for (String part : bboxString.split(",")) {
                        String[] kv = part.split("=");
                        bboxMap.put(kv[0].trim(), Double.parseDouble(kv[1].trim()));
                    }
                    bboxJson = new ObjectMapper().writeValueAsString(bboxMap); // Valid JSON string
                }

                if (!key.isEmpty() && !value.isEmpty()) {
                    SorItemMappingOutputTable row = SorItemMappingOutputTable.builder()
                            .createdOn(entity.getCreatedOn())
                            .createdUserId(String.valueOf(entity.getTenantId()))
                            .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                            .lastUpdatedUserId(String.valueOf(entity.getTenantId()))
                            .originId(entity.getOriginId())
                            .paperNo(entity.getPaperNo())
                            .groupId(entity.getGroupId())
                            .inputFilePath(entity.getInputFilePath())
                            .actionId(this.action.getActionId())
                            .tenantId(entity.getTenantId())
                            .processId(entity.getProcessId())
                            .rootPipelineId(entity.getRootPipelineId())
                            .process(entity.getProcess())
                            .batchId(entity.getBatchId())
                            .modelRegistry(entity.getModelRegistry())
                            .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                            .stage("AWS PARSER")
                            .category(entity.getCategory())
                            .message("AWS Parser Completed")
                            .request(entity.getRequest())
                            .sorItemName(key)
                            .answer(value)
                            .confidence(Double.parseDouble(confidence))
                            .bbox(bboxJson)
                            .endpoint(String.valueOf(entity.getEndpoint()))
                            .build();

                    parentObj.add(row);
                }
            }
        }

        return parentObj;

    }





}
