package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DrugMatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;


    @Slf4j
    public class DrugMatchTest {

        @Test
        void tritonServer() throws Exception {

            final DrugMatch urgencyTriageModel = DrugMatch.builder()
                    .condition(true)
                    .drugCompare("md_lookup_systemKey.drug_name_response_1234567")
                    .name("master data for drug")
                    .resourceConn("intics_zio_db_conn")
                    .inputSet("  SELECT b.origin_id ,b.eoc_identifier,b.document_id ,b.paper_no,b.values as drug_name,'' as jcode,'1' as root_pipeline_id, a.batch_id, b.sor_item_name\n" +
                            "                    FROM md_lookup_systemKey.master_data_payload_queue a\n" +
                            "                    JOIN eoc_response.eoc_trans b\n" +
                            "                    on a.origin_id =b.origin_id\n" +
                            "                    and b.sor_item_name='member_name'\n" +
                            "                     and a.batch_id = 'batch_1_0';")
                    .build();


            ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
              actionExecutionAudit.setProcessId(138980079308730208L);
            actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                    Map.entry("consumer.masterdata.API.count", "1"),
                    Map.entry("triton.request.activator", "true"),
                    Map.entry("actionId", "1"),
                    Map.entry("drugname.api.url","http://192.168.10.239:9200/v2/models/cos-service/versions/1/infer"),
                    Map.entry("write.batch.size", "5")));

            DrugMatchAction action1 = new DrugMatchAction(actionExecutionAudit, log, urgencyTriageModel);
            action1.execute();


        }
    }


