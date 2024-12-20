package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.audit.ExecutionStatus;
import in.handyman.raven.lambda.doa.audit.PipelineExecutionAudit;
import in.handyman.raven.lambda.doa.config.SpwResourceConfig;
import in.handyman.raven.lambda.process.LContext;
import in.handyman.raven.lambda.process.LambdaEngine;
import in.handyman.raven.lib.model.ImportCsvToDB;
import in.handyman.raven.lib.model.LoadCsv;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ImportCsvToDBActionTest {


    @Test
    void importCsv() throws Exception {

        ImportCsvToDB importCsvToDB=new ImportCsvToDB();
        importCsvToDB.setName("import csv into db");
        importCsvToDB.setValue(Collections.singletonList("/bh_control_data_1.csv"));
        importCsvToDB.setCondition(true);
        importCsvToDB.setTarget(SpwResourceConfig.builder().userName("postgres").resourceUrl("jdbc:postgresql://intics-db:5432/zio_pipeline_ui").password("xk56iIUYV1eihMke7W0gbnfnl4gBkArsF26kttM96R8utWA4hJ").build());
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
                .pipelineName("")
                .processLoadType("")
                .inheritedContext("")
                .build());

    }

}