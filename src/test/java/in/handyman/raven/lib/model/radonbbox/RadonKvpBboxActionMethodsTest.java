package in.handyman.raven.lib.model.radonbbox;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.RadonKvpBboxAction;
import in.handyman.raven.lib.model.RadonKvpBbox;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.channels.AcceptPendingException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class RadonKvpBboxActionMethodsTest {

    @Test
    void executeMethod() throws Exception {
        String querySet = "\n" +
                "SELECT  a.sor_container_name, a.paper_no,  a.origin_id,  a.group_id,    a.tenant_id, a.root_pipeline_id, a.batch_id,\n" +
                "a.model_registry, '/data/output/SYNT_167074460_C1_1.jpg' as input_file_path,b.asset_id,\n" +
                "(jsonb_agg(json_build_object('sorItemName',a.sor_item_name,'valueType',p.document_type,'answer',a.answer) )::varchar) as radon_output\n" +
                "        FROM sor_transaction.llm_json_parser_output_audit a\n" +
                "        join paper_classification.paper_classification_result p on a.origin_id = p.origin_id and a.paper_no= p.paper_no and a.tenant_id = p.tenant_id and a.batch_id = p.batch_id\n" +
                "        join info.source_of_truth sot on sot.origin_id = a.origin_id and sot.paper_no =a.paper_no\n" +
                "        join info.asset b on b.asset_id =sot.asset_id\n" +
                "WHERE a.tenant_id = 1 AND a.model_registry = 'RADON' and a.group_id ='525' and a.batch_id ='BATCH-525_0'\n" +
                "GROUP BY\n" +
                "        a.sor_container_name,\n" +
                "        a.paper_no,\n" +
                "        a.origin_id,\n" +
                "        a.group_id,\n" +
                "        a.tenant_id,\n" +
                "        a.root_pipeline_id,\n" +
                "        a.batch_id, b.asset_id,\n" +
                "        a.model_registry,\n" +
                "        b.file_path ,\n" +
                "        p.document_type;\n" ;

        RadonKvpBbox radonKvpBbox = RadonKvpBbox.builder()
                .name("Radon kvp bbox")
                .condition(Boolean.TRUE)
                .consumerApiCount("1")
                .coproUrl("http://192.168.10.248:7600/v2/models/radon-bbox-service/versions/1/infer")
                .inputTable("sor_transaction.llm_json_parser_output_audit")
                .outputTable("sor_transaction.radon_kvp_bbox_output_audit")
                .outputDir("/data/output/")
                .querySet(querySet)
                .resourceConn("intics_zio_db_conn")
                .tritonActivator("true")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.setProcessId(1L);
        actionExecutionAudit.getContext().put("write.batch.size", "1");
        actionExecutionAudit.getContext().put("read.batch.size", "1");
        actionExecutionAudit.getContext().put("okhttp.client.timeout", "1000");
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","2"),
                Map.entry("consumer.API.count","2"),
                Map.entry("database.encryption.activator", "true"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("encryption.activator", "true"),
                Map.entry("encryptionUrl", "http://192.168.10.248:10001/copro-utils/data-security/encrypt"),
                Map.entry("decryptionUrl","http://192.168.10.248:10001/copro-utils/data-security/decrypt"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size","2")));

        RadonKvpBboxAction radonKvpBboxAction = new RadonKvpBboxAction(actionExecutionAudit, log, radonKvpBbox);
        radonKvpBboxAction.execute();


    }


}