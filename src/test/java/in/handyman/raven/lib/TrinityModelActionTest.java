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
                .outputDir("/home/christopher.paulraj@zucisystems.com/Downloads/data/")
                .requestUrl("http://192.168.10.248:8900/v2/models/argon-vqa-service/versions/1/infer")
                .resourceConn("intics_zio_db_conn")
                .forkBatchSize("1")
                .questionSql(" SELECT array_agg(a.question) as questions, a.file_path, a.document_type as paper_type,\n" +
                        "a.model_registry as model_registry, a.tenant_id, a.batch_id \n" +
                        "FROM macro.sor_transaction_tqa_audit a\n" +
                        "where a.document_type='Printed' and a.model_registry = 'XENON'\n" +
                        "and a.root_pipeline_id = '4705' group by a.file_path, paper_type, a.model_registry, a.tenant_id, a.batch_id ;\n ")
                .responseAs("sor_transaction_tqa_49254")
                .build();
        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.handwritten.url","http://copro.valuation:10189/copro/attribution/kvp-docnet");
        actionExecutionAudit.getContext().put("okhttp.client.timeout","20");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("tenant_id", "1");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setActionId(12L);
        actionExecutionAudit.setRootPipelineId(1993L);
        actionExecutionAudit.setPipelineId(12345L);
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
                .outputDir("/home/christopher.paulraj@zucisystems.com/Downloads/data/")
                .requestUrl("http://192.168.10.248:9000/v2/models/xenon-vqa-service/versions/1/infer")
                .resourceConn("intics_zio_db_conn")
                .forkBatchSize("1")
                .questionSql("\n" +
                        "\n" +
                        "SELECT array_agg(a.question) as questions, a.file_path, a.document_type as paper_type,\n" +
                        "a.model_registry as model_registry, a.tenant_id, a.batch_id \n" +
                        "FROM macro.sor_transaction_tqa_audit a\n" +
                        "where a.document_type='Printed' and a.model_registry = 'XENON'\n" +
                        "and a.root_pipeline_id = '4705' group by a.file_path, paper_type, a.model_registry, a.tenant_id, a.batch_id ;\n" +
                        "\n")
                .responseAs("docnet_attribution_response_123")
                .build();
        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.handwritten.url","http://192.168.10.248:9000/v2/models/xenon-vqa-service/versions/1/infer");
        actionExecutionAudit.getContext().put("okhttp.client.timeout","20");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("tenant_id", "1");
        actionExecutionAudit.setActionId(12L);
        actionExecutionAudit.setRootPipelineId(1993L);
        actionExecutionAudit.setPipelineId(12345L);
        actionExecutionAudit.setProcessId(49254L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("triton.request.activator","true"),
                Map.entry("write.batch.size","5")));

        TrinityModelAction trinityModelAction=new TrinityModelAction(actionExecutionAudit,log,trinityModel);
        trinityModelAction.execute();
    }
}