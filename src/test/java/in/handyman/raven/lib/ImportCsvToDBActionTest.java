package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.audit.PipelineExecutionAudit;
import in.handyman.raven.lambda.doa.config.SpwResourceConfig;
import in.handyman.raven.lambda.process.LContext;
import in.handyman.raven.lambda.process.LambdaEngine;
import in.handyman.raven.lib.model.ImportCsvToDB;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

@Slf4j
class ImportCsvToDBActionTest {


    @Test
    void importCsv() throws Exception {

        ImportCsvToDB importCsvToDB=new ImportCsvToDB();
        importCsvToDB.setName("import csv into db");
        importCsvToDB.setValue(Collections.singletonList("/home/anandh.andrews@zucisystems.com/Downloads/test_control_data.csv"));
        importCsvToDB.setCondition(true);
        importCsvToDB.setTarget("intics_zio_db_conn");
        importCsvToDB.setWriteThreadCount("5");
        importCsvToDB.setBatchSize("5");
        importCsvToDB.setTableName("bh_control_data_1");

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("alchemy.origin.valuation.url","http://localhost:8189/alchemy/api/v1/valuation/origin");
        actionExecutionAudit.getContext().put("alchemyAuth.token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNzE3ODQ4NDAzLCJpYXQiOjE3MTc3NjIwMDMsImVtYWlsIjoibml2YXJhX2RlbW9AaW50aWNzLmFpIn0.iKZtp1SyCEWX934YP6xKGSGlSgy6SMWeE6ur16W_Q_Y");
        actionExecutionAudit.getContext().put("alchemyAuth.tenantId","74");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("write.batch.size","1");
        actionExecutionAudit.getContext().put("read.batch.size","1");


        ImportCsvToDBAction loadCsvAction=new ImportCsvToDBAction(actionExecutionAudit,log,importCsvToDB);
        loadCsvAction.execute();
    }


    @Test
    void importCsvLambda(){
        final PipelineExecutionAudit pipelineExecutionAudit = LambdaEngine.start(LContext.builder()
                .pipelineName("import.csv.into.db")
                .processLoadType("FILE")
                .inheritedContext(Map.ofEntries(Map.entry("inbound_id","1")
                        ,Map.entry("tenant_id","1")
                        ,Map.entry("input_csv_file_directory","/home/anandh.andrews@zucisystems.com/Downloads/bh_control_data_1.csv")
                        ,Map.entry("input_csv_file_name","bh_control_data_1")
                        ,Map.entry("channel_id","1")
                        ,Map.entry("created_user_id","1")
                        ,Map.entry("origin_type","ORIGIN")
                        ,Map.entry("transaction_id","1")
                        ,Map.entry("workspace_id","1")
                        ,Map.entry("document_type","HEALTH_CARE")
                ))
                .build());

        pipelineExecutionAudit.getPipelineId();



    }

}