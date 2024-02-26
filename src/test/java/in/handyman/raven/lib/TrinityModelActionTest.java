package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TrinityModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class TrinityModelActionTest {

    @Test
    void executeHandwritten() throws Exception {

        TrinityModel trinityModel=TrinityModel.builder()
                .name("DIE model testing")
                .condition(true)
                .outputDir("dir")
                .requestUrl("http://192.168.10.239:10189/copro/attribution/kvp-docnet")
                .resourceConn("intics_agadia_db_conn")
                .forkBatchSize("1")
                .questionSql("  SELECT a.question, a.file_path, a.document_type as paperType, a.model_registry  FROM\n" +
                        "macro.sor_transaction_tqa_audit a\n" +
                        "join sor_transaction.sor_transaction_pipeline_audit st on st.group_id =a.group_id \n" +
                        "where a.document_type='Printed' and a.model_registry = 'xenon'\n" +
                        "and a.tenant_id = 1 and st.group_id='43'; ")
                .responseAs("sor_transaction_tqa_49254")
                .build();
        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.handwritten.url","http://copro.valuation:10189/copro/attribution/kvp-docnet");
        actionExecutionAudit.getContext().put("okhttp.client.timeout","20");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("write.batch.size","5")));

        TrinityModelAction trinityModelAction=new TrinityModelAction(actionExecutionAudit,log,trinityModel);
        trinityModelAction.execute();
    }



    @Test
    void executePrinted() throws Exception {

        TrinityModel trinityModel=TrinityModel.builder()
                .name("DIE model testing")
                .condition(true)
                .outputDir("dir")
                .requestUrl("http://copro.impira:10193/copro/attribution/kvp-attribution-dqa-new")
                .resourceConn("intics_agadia_db_conn")
                .forkBatchSize("1")
                .questionSql("   SELECT a.question, a.file_path, a.document_type as paperType FROM\n" +
                        "\t                   macro.sor_transaction_tqa_${init_process_id.process_id} a\n" +
                        "\t                     join sor_transaction.sor_transaction_payload_queue st on st.origin_id=a.origin_id\n" +
                        "\t                        where a.document_type='Printed';      ")
                .responseAs("macro.sor_transaction_tqa_49254")
                .build();
        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.printed.url","http://copro.impira:10193/copro/attribution/kvp-attribution-dqa-new");
        actionExecutionAudit.getContext().put("okhttp.client.timeout","20");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("write.batch.size","5")));

        TrinityModelAction trinityModelAction=new TrinityModelAction(actionExecutionAudit,log,trinityModel);
        trinityModelAction.execute();
    }
}