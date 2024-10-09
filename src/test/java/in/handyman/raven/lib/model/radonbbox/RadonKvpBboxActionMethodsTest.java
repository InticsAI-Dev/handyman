package in.handyman.raven.lib.model.radonbbox;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.RadonKvpBboxAction;
import in.handyman.raven.lib.model.RadonKvpBbox;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.channels.AcceptPendingException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class RadonKvpBboxActionMethodsTest {

    @Test
    void executeMethod() throws Exception {
        String querySet = "               \n" +
                "SELECT  a.paper_no,  a.origin_id,  a.group_id,    a.tenant_id, a.root_pipeline_id, a.batch_id,\n" +
                "                  a.model_registry,  b.file_path as input_file_path,\n" +
                "                  a.sor_container_name,(jsonb_agg(json_build_object('sorItemName',a.sor_item_name,'valueType','Printed' ,'answer',a.answer) )::varchar) as radon_output\n" +
                "                FROM sor_transaction.llm_json_parser_output_1963 a\n" +
                "                join info.source_of_truth sot on sot.origin_id = a.origin_id and sot.paper_no =a.paper_no \n" +
                "                join info.asset b on b.asset_id =sot.asset_id\n" +
                "                group by   \n" +
                "                    a.sor_container_name,\n" +
                "                    a.paper_no,\n" +
                "                    a.origin_id,\n" +
                "                   a.group_id, \n" +
                "                    a.tenant_id,\n" +
                "                    a.root_pipeline_id, \n" +
                "                    a.batch_id, \n" +
                "                    a.model_registry, \n" +
                "                    b.file_path ;";

        RadonKvpBbox radonKvpBbox = RadonKvpBbox.builder()
                .name("Radon kvp bbox")
                .condition(Boolean.TRUE)
                .consumerApiCount("1")
                .coproUrl("http://192.168.10.239:7600/v2/models/radon-bbox-service/versions/1/infer")
                .inputTable("sor_transaction.llm_json_parser_output_12345")
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

        RadonKvpBboxAction radonKvpBboxAction = new RadonKvpBboxAction(actionExecutionAudit, log, radonKvpBbox);
        radonKvpBboxAction.execute();


    }


}