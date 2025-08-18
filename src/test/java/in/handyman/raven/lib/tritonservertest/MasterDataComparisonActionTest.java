package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.MasterdataComparisonAction;
import in.handyman.raven.lib.model.MasterdataComparison;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;
@Slf4j
public class MasterDataComparisonActionTest {

    @Test
    void tritonServer() throws Exception {
        final MasterdataComparison build = MasterdataComparison.builder()
                .condition(true)
                .name("MasterDataComparison")
                .endPoint("http://192.168.10.240:9200/v2/models/cos-service/versions/1/infer")
//                .endPoint("http://192.168.10.239:10192/copro/similarity/cos-sim")
                .matchResult("md_lookup_systemKey.provider_address_match")
                .inputSet("SELECT a.origin_id ,a.paper_no ,a.eoc_identifier,now(), a.provider_address  as extracted_value ,b.provider_address  as actual_value,\n" +
                        "                a.root_pipeline_id as root_pipeline_id\n" +
                        "                FROM md_lookup_systemKey.provider_address a\n" +
                        "                LEFT JOIN master_data.provider_metadata b\n" +
                        "                ON a.sk_provider_npi=b.provider_npi\n" +
                        "                and a.sk_provider_name=b.provider_name\n" +
                        "                WHERE a.group_id='16' and a.tenant_id = 1 and 4 = 4")
//                .inputSet("SELECT 'INT-1' AS originId, 1 AS paperNo, 1 AS groupId,1 as tenantId, 1 AS eocIdentifier, ARRAY['dinesh kumar', 'dinesh', 'kumar'] AS extractedValue,'dinesh' AS actualValue, 1 AS rootPipelineId")
                .resourceConn("intics_zio_db_conn")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
//        actionExecutionAudit.getContext().put("copro.masterdata-comparison.url", "http://192.168.10.239:10192/copro/similarity/cos-sim");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("consumer.masterdata.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size", "5")));

        MasterdataComparisonAction action1 = new MasterdataComparisonAction(actionExecutionAudit, log, build);
        action1.execute();
    }
}


