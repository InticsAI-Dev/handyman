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
                .outputDir("/data/")
                .outputTable("bulletin_extraction.bulletin_extraction_result")
                .querySet("SELECT 'ORIGIN-1' as origin_id, '1' as paper_no,1 as group_id,'/data/output/68/87/3126/autorotation/auto_rotation/2022-10-26T9_58_10-Dooliquor-LLC_1.jpg' file_path, " +
                        "1 as tenant_id,1 as process_id,'/data/output/' output_dir,1 as root_pipeline_id,'bulletin' as process,1 as synonym_id,'extract all the key value pairs' prompt,'KVP' as section_header\n"
                        )
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