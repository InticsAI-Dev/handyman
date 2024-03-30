package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.BulletInExtractionAction;
import in.handyman.raven.lib.model.BulletInExtraction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class BulletInExtractionActionTest {

    @Test
    public void tritonTest() throws Exception {
        BulletInExtraction bulletInExtraction= BulletInExtraction.builder()
                .name("bulletin extraction")
                .condition(true)
                .endpoint("http://192.168.10.240:10195/copro/mistral_llm_based_on_image")
                .resourceConn("intics_zio_db_conn")
                .outputDir("/data/output/")
                .outputTable("bulletin_extraction.bulletin_extraction_result")
                .querySet("SELECT origin_id, paper_no, group_id, file_path, tenant_id, process_id, output_dir, root_pipeline_id, process, synonym_id, prompt, section_header, 'batch_1' as batch_id\n" +
                        "FROM bulletin_extraction.bulletin_extraction_input_table_audit;")
                .build();

        ActionExecutionAudit ac=new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("bulletin.extraction.consumer.API.count","1");
        ac.getContext().put("write.batch.size","1");
        ac.getContext().put("read.batch.size","1");
        ac.getContext().put("triton.request.bulletin.extraction.activator","false");



        BulletInExtractionAction bulletInExtractionAction=new BulletInExtractionAction(ac,log,bulletInExtraction);

        bulletInExtractionAction.execute();

    }


}