package in.handyman.raven.lib.tritonServerTest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.TrinityModelAction;
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
                .questionSql("   SELECT a.question, a.file_path, '' as paperType FROM\n" +
                        "\t                   macro.sor_transaction_tqa_49254 a\n" +
                        "\t                     join sor_transaction.sor_transaction_payload_queue st on st.origin_id=a.origin_id\n" +
                        "\t                        where a.document_type='Handwritten';      ")
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
                .resourceConn("intics_zio_db_conn")
                .forkBatchSize("1")
                .questionSql("   SELECT 'what is patient name ' as question, '/data/output/22/preprocess/autorotation/auto_rotation/Humana_Form_1_0.jpg' as file_path, 'Printed' as paperType;\n")
                .responseAs("sor_transaction_tqa_49254")
                .build();
        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.printed.url","http://copro.impira:10193/copro/attribution/kvp-attribution-dqa-new");
        actionExecutionAudit.getContext().put("okhttp.client.timeout","20");
        actionExecutionAudit.getContext().put("triton.request.activator","true");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setRootPipelineId(56L);
        actionExecutionAudit.setActionId(12L);

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("write.batch.size","5")));

        TrinityModelAction trinityModelAction=new TrinityModelAction(actionExecutionAudit,log,trinityModel);
        trinityModelAction.execute();
    }



    @Test
    void executeTritonServerHandwritten() throws Exception {


        TrinityModel trinityModel=TrinityModel.builder()
                .name("DIE model testing")
                .condition(true)
                .outputDir("dir")
                .requestUrl("http://192.168.10.245:8900/v2/models/ernie-service/versions/1/infer")
                .resourceConn("intics_agadia_db_conn")
                .forkBatchSize("1")
                .questionSql("   SELECT question,'/data/output/pdf_to_image/SYNT_166838894_c1/SYNT_166838894_c1_1.jpg' as file_path, 'Handwritten' paperType FROM\n" +
                        "\t                   sor_meta.sor_question where document_id='TMP-AGD-001';      ")
                .responseAs("sor_transaction_tqa_12345")
                .build();
        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.printed.url","http://192.168.10.245:8900/v2/models/ernie-service/versions/1/infer");
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
    void executeTritonServerPrinted() throws Exception {

        TrinityModel trinityModel=TrinityModel.builder()
                .name("DIE model testing")
                .condition(true)
                .outputDir("dir")
                .requestUrl("http://192.168.10.245:8900/v2/models/ernie-service/versions/1/infer")
                .resourceConn("intics_agadia_db_conn")
                .forkBatchSize("1")
                .questionSql("   SELECT question,'/data/output/pdf_to_image/SYNT_166838894_c1/SYNT_166838894_c1_1.jpg' as file_path, 'Printed' paperType FROM\n" +
                        "\t                   sor_meta.sor_question where a.document_id='TMP-AGD-001';      ")
                .responseAs("sor_transaction_tqa_12345")
                .build();
        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.printed.url","http://192.168.10.245:8900/v2/models/ernie-service/versions/1/infer");
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