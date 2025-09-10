package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.SorItemMappingExecutor;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.*;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        boolean matched = transform(input);
        List<SorItemMappingOutputTable> sorItemMappingOutputTables = new ArrayList<>();
        Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(sorItemExecutor.getResourceConn());
        log.info(marker, "aws sor item mapping  action  started for origin Id {} paper No {} ", input.getOriginId(), input.getPaperNo());
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            final String selectQuery = sorItemExecutor.getQuerySet();
            log.info(marker, "aws sor item mapping  action {} has been started ", selectQuery);
            if(matched) {
                /*List<SorItemMappingInputTable> inputTableList = jdbi.withHandle(handle -> handle.createQuery(selectQuery)
                        .mapToBean(SorItemMappingInputTable.class)
                        .list());
                System.out.println(inputTableList);*/


                jdbi.useTransaction(handle -> {
                    final String insQuery = buildInsertQuery();
                    SorItemMappingOutputTable insertData = SorItemMappingOutputTable.builder()
                            .createdOn(input.getCreatedOn())
                            .createdUserId(input.getCreatedUserId())
                            .lastUpdatedOn(input.getLastUpdatedOn())
                            .lastUpdatedUserId(input.getLastUpdatedUserId())
                            .inputFilePath(input.getInputFilePath())
                            .inputResponseJson(input.getInputResponseJson())
                            .totalResponseJson(input.getTotalResponseJson())
                            .paperNo(input.getPaperNo())
                            .originId(input.getOriginId())
                            .processId(input.getProcessId())
                            .actionId(input.getActionId())
                            .process(input.getProcess())
                            .groupId(input.getGroupId())
                            .tenantId(input.getTenantId())
                            .rootPipelineId(input.getRootPipelineId())
                            .batchId(input.getBatchId())
                            .modelRegistry(input.getModelRegistry())
                            .status(input.getStatus())
                            .stage(input.getStage())
                            .message(input.getMessage())
                            .category(input.getCategory())
                            .request(input.getRequest())
                            .answer(input.getAnswer())
                            .endpoint(input.getEndpoint())
                            .sorItemName(input.getSorItemName())
                            .build();

                    getInsertIntoOutputTable(handle, insQuery, insertData);


                });
            }

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
                .execute();
    }

    private String buildInsertQuery() {
        return "INSERT INTO " + sorItemExecutor.getOutputTable() + " (\n" +  // <-- added (
                "  created_on, created_user_id, last_updated_on, last_updated_user_id,\n" +
                "  input_file_path, input_response_json, total_response_json, paper_no, origin_id,\n" +
                "  process_id, action_id, \"process\", group_id, tenant_id, root_pipeline_id, batch_id,\n" +
                "  model_registry, status, stage, message, category, request, answer, endpoint, sor_item_name\n" +
                ") \n" +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);\n";
    }

    public  boolean transform(SorItemMappingInputTable input) {

        Map<String, List<String>> synonymMap = new HashMap<>();
        List<String> memberNameList  = new ArrayList<>();
        memberNameList.add("Patient name");
        memberNameList.add("Patient full name");
        memberNameList.add("Subscriber name");
        memberNameList.add("Subscriber full name");
        memberNameList.add("Patient name");
        memberNameList.add("Patient full name");
        memberNameList.add("member name");
        memberNameList.add("member full name");
        synonymMap.put("member_full_name",memberNameList);


        String sorItemName = input.getSorItemName();
        boolean matched = false;

        if (sorItemName != null) {
            for (Map.Entry<String, List<String>> entry : synonymMap.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();

                // check case-insensitive match with synonyms
                for (String value : values) {
                    String cleaned = sorItemName.replaceAll("[^a-zA-Z0-9 ]", "");
                    if (cleaned.equalsIgnoreCase(value)) {
                        input.setSorItemName(key); // replace with canonical key
                        matched = true;
                        break;
                    }
                }
                if (matched) break;
            }
        }
        return matched;
    }
}
