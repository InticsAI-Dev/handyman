package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.ParagraphExtractionAction;
import in.handyman.raven.lib.model.ParagraphExtraction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ParagraphExtractionActionTest {

    @Test
    public void tritonTest() throws Exception {
        ParagraphExtraction paragraphExtraction = ParagraphExtraction.builder()
                .name("paragraph extraction")
                .condition(true)
                .endpoint("http://192.168.10.240:10196/copro/mistral_llm_based_on_image")
                .resourceConn("intics_zio_db_conn")
                .outputDir("/data/")
                .outputTable("paragraph_extraction.paragraph_extraction_result")
                .querySet("SELECT 'ORIGIN-2093' as origin_id, 1 as paper_no, 1503 as group_id, '/data/output/81/1503/42793/autorotation/auto_rotation/QTN3864 - BOQ for the Fit-Out Works in Polo Ralph Lauren at MOE_1.jpg' as file_path, 81 as tenant_id, 1 as process_id, '/data/output/1503/paragraph_extraction/' as output_dir, 42793 as root_pipeline_id, 'PARAGRAPH_EXTRACTION' as process, 13658 as synonym_id, 'You are an intelligent bullet point extractor specialized in extracting payment terms from quotations. Payment terms typically include percentages of payments split into four bullet points. Your task is to extract payment terms from the provided quotations. Payment terms are usually found in the second part and at the end of the quotation content.     Your output should be in the array of string not array of dict or others like this Please ensure that your response starts with, ###response_Start and ends with ###response_End. Make sure to read the prompt thoroughly for complete understanding.' as prompt, 'Payment terms' as section_header, 'batch_id' as batch_id\n")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("paragraph.extraction.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("triton.request.paragraph.extraction.activator", "false");


        ParagraphExtractionAction paragraphExtractionAction = new ParagraphExtractionAction(ac, log, paragraphExtraction);

        paragraphExtractionAction.execute();

    }


}