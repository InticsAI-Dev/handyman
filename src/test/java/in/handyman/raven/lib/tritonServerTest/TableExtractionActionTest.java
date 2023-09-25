package in.handyman.raven.lib.tritonServerTest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TableExtraction;
import in.handyman.raven.lib.TableExtractionAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class TableExtractionActionTest {

    @Test
    void tritonServer() throws Exception {
        TableExtraction tableExtraction= TableExtraction.builder()
                .name("Text extraction macro test after copro optimization")
                .resourceConn("intics_agadia_db_conn")
                .condition(true)
                .processId("999")
                .resultTable("table-extraction.")
                .outputDir("/data/output")
                .querySet("SELECT\n" +
                        "    'INT-1' as origin_id,\n" +
                        "    1 as group_id,\n" +
                        "    '/data/output/pdf_to_image/SYNT_166838894_c1/SYNT_166838894_c1_0.jpg' as file_path,\n" +
                        "    1 as tenant_id,\n" +
                        "    1 as template_id,\n" +
                        "    1 as process_id,\n" +
                        "    1 as root_pipeline_id\n")
                .build();
        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.table-extraction.url","http://localhost:10194/copro/table-extraction"),
                Map.entry("read.batch.size","5"),
                Map.entry("table.extraction.consumer.API.count","1"),
                Map.entry("write.batch.size","5")));

        TableExtractionAction tableExtractionAction=new TableExtractionAction(actionExecutionAudit,log,tableExtraction);
        tableExtractionAction.execute();
    }

}
