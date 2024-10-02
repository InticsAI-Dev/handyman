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

        TrinityModel trinityModel = TrinityModel.builder()
                .name("DIE model testing")
                .condition(true)
                .outputDir("/data/output/")
                .requestUrl("http://192.168.10.248:8900/v2/models/argon-vqa-service/versions/1/infer")
                .resourceConn("intics_zio_db_conn")
                .forkBatchSize("1")
                .questionSql("SELECT \n" +
                        "    (jsonb_agg(\n" +
                        "        json_build_object(\n" +
                        "            'question', a.question, \n" +
                        "            'questionId', a.question_id, \n" +
                        "            'synonymId', a.synonym_id, \n" +
                        "            'sorItemName', a.sor_item_name)\n" +
                        "        )\n" +
                        "    )::varchar as attributes,\n" +
                        "    '/data/input/ut_classify_filter/ut_input/paper_class_ut/SYNT_166731563_c3_04_1.jpg' as file_path, \n" +
                        "    a.model_registry,  \n" +
                        "    a.origin_id,\n" +
                        "    a.paper_no,\n" +
                        "    a.tenant_id, \n" +
                        "    st.group_id, \n" +
                        "    a.root_pipeline_id, \n" +
                        "    a.model_registry_id, \n" +
                        "    'Primary' as qn_category, \n" +
                        "    '1' as process_id, \n" +
                        "    a.batch_id\n" +
                        "FROM \n" +
                        "    macro.sor_transaction_tqa_audit a\n" +
                        "JOIN \n" +
                        "    sor_transaction.sor_transaction_payload_queue_archive st \n" +
                        "ON \n" +
                        "    st.origin_id = a.origin_id\n" +
                        "WHERE \n" +
                        "    a.model_registry = 'XENON' \n" +
                        "    AND a.tenant_id = 1 \n" +
                        "    AND st.group_id = '67' \n" +
                        "    AND a.batch_id = 'BATCH-67_0'\n" +
                        "    AND a.origin_id = 'ORIGIN-246'\n" +
                        "GROUP BY \n" +
                        "    a.model_registry,\n" +
                        "    a.origin_id, \n" +
                        "    a.paper_no, \n" +
                        "    a.tenant_id,\n" +
                        "    st.group_id, \n" +
                        "    a.root_pipeline_id, \n" +
                        "    a.model_registry_id, \n" +
                        "    a.batch_id;\n")
                .responseAs("sor_transaction_tqa_123456")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.handwritten.url", "http://copro.valuation:10189/copro/attribution/kvp-docnet");
        actionExecutionAudit.getContext().put("okhttp.client.timeout", "20");
        actionExecutionAudit.getContext().put("gen_group_id.group_id", "1");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "5")));

        TrinityModelAction trinityModelAction = new TrinityModelAction(actionExecutionAudit, log, trinityModel);
        trinityModelAction.execute();
    }


    @Test
    void executePrinted() throws Exception {

        TrinityModel trinityModel = TrinityModel.builder()
                .name("DIE model testing")
                .condition(true)
                .outputDir("dir")
                .requestUrl("http://192.168.10.248:9000/v2/models/xenon-vqa-service/versions/1/infer")
                .resourceConn("intics_zio_db_conn")
                .forkBatchSize("1")
                .questionSql("SELECT (jsonb_agg(json_build_object('question', (a.question), 'questionId', a.question_id, 'synonymId', a.synonym_id, 'sorItemName', a.sor_item_name)))::varchar as attributes,\n" +
                        "'/data/input/ut_classify_filter/ut_input/paper_class_ut/SYNT_166731563_c3_04_1.jpg' as file_path, a.model_registry,  a.origin_id,a.paper_no,a.tenant_id, st.group_id, a.root_pipeline_id, a.model_registry_id, 'Primary' as qn_category, '1' as process_id,\n" +
                        "a.batch_id FROM macro.sor_transaction_tqa_audit a\n" +
                        "join sor_transaction.sor_transaction_payload_queue_archive st on st.origin_id=a.origin_id\n" +
                        "where a.model_registry = 'XENON'\n" +
                        "AND a.tenant_id = 1 \n" +
                        "AND st.group_id = '3' \n" +
                        "AND a.batch_id = 'BATCH-3_0'\n" +
                        "and a.origin_id = 'ORIGIN-3'\n" +
                        "group by a.file_path,  a.model_registry,a.origin_id, a.paper_no , a.tenant_id,st.group_id , a.root_pipeline_id, a.model_registry_id, a.batch_id\n" +
                        "limit 1;")
                .responseAs("macro.docnet_attribution_response_123888888")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.handwritten.url", "http://192.168.10.248:9000/v2/models/xenon-vqa-service/versions/1/infer");
        actionExecutionAudit.getContext().put("okhttp.client.timeout", "20");
        actionExecutionAudit.getContext().put("gen_group_id.group_id", "1");
        actionExecutionAudit.getContext().put("tenant_id", "1");
        actionExecutionAudit.getContext().put("ARGON.VQA.START", "ARGON VQA START");
        actionExecutionAudit.getContext().put("XENON.VQA.START", "XENON VQA START");
        actionExecutionAudit.getContext().put("KRYPTON.MODEL.START", "KRYPTON MODEL START");
        actionExecutionAudit.getContext().put("BORON.VQA.START", "BORON VQA START");
        actionExecutionAudit.getContext().put("VQA.VALUATION", "VQA VALUATION");
        actionExecutionAudit.getContext().put("triton.request.activator", "true");
        actionExecutionAudit.setActionId(12L);
        actionExecutionAudit.setRootPipelineId(1993L);
        actionExecutionAudit.setPipelineId(12345L);
        actionExecutionAudit.setProcessId(49254L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "5")));

        TrinityModelAction trinityModelAction = new TrinityModelAction(actionExecutionAudit, log, trinityModel);
        trinityModelAction.execute();
    }
}