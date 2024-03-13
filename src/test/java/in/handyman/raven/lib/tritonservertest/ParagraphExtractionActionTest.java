package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.BulletInExtractionAction;
import in.handyman.raven.lib.ParagraphExtractionAction;
import in.handyman.raven.lib.model.BulletInExtraction;
import in.handyman.raven.lib.model.ParagraphExtraction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class ParagraphExtractionActionTest {

    @Test
    public void tritonTest() throws Exception {
        ParagraphExtraction bulletInExtraction= ParagraphExtraction.builder()
                .name("bulletin extraction")
                .condition(true)
                .endpoint("http://192.168.10.240:10195/copro/mistral_llm_based_on_image")
                .resourceConn("intics_zio_db_conn")
                .outputDir("/data/")
                .outputTable("bulletin_extraction.bulletin_extraction_result")
                .querySet("SELECT origin_id, paper_no, group_id, file_path, tenant_id, process_id, output_dir, root_pipeline_id, process, synonym_id, prompt, section_header\n" +
                        "FROM bulletin_extraction.bulletin_extraction_input_table_1234;")
                .build();

        ActionExecutionAudit ac=new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("paragraph.extraction.consumer.API.count","1");
        ac.getContext().put("write.batch.size","1");
        ac.getContext().put("read.batch.size","1");
        ac.getContext().put("triton.request.paragraph.extraction.activator","false");



        ParagraphExtractionAction paragraphExtractionAction=new ParagraphExtractionAction(ac,log,bulletInExtraction);

        paragraphExtractionAction.execute();

    }


}