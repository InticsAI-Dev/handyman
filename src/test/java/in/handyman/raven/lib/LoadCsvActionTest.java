package in.handyman.raven.lib;


import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.LoadCsv;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoadCsvActionTest {

    @Test
    void loadCsvTest() throws Exception {

        LoadCsv loadCsv=new LoadCsv();
        loadCsv.setName("Load csv into the db");
        loadCsv.setCondition(true);
        loadCsv.setTo("intics_zio_db_conn");
        loadCsv.setPid("12345");
        loadCsv.setLimit("10");
        loadCsv.setValue("");
        loadCsv.setSource("/home/anandh.andrews@zucisystems.com/Downloads/bh_control_data_1.csv");



        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("alchemy.origin.valuation.url","http://localhost:8189/alchemy/api/v1/valuation/origin");
        actionExecutionAudit.getContext().put("alchemyAuth.token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNzE3ODQ4NDAzLCJpYXQiOjE3MTc3NjIwMDMsImVtYWlsIjoibml2YXJhX2RlbW9AaW50aWNzLmFpIn0.iKZtp1SyCEWX934YP6xKGSGlSgy6SMWeE6ur16W_Q_Y");
        actionExecutionAudit.getContext().put("alchemyAuth.tenantId","74");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("write.batch.size","1");
        actionExecutionAudit.getContext().put("read.batch.size","1");

            LoadCsvAction loadCsvAction=new LoadCsvAction(actionExecutionAudit,log,loadCsv);
            loadCsvAction.execute();
    }
}
