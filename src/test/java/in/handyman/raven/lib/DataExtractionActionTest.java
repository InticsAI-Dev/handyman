package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DataExtraction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class DataExtractionActionTest {
    @Test
    void dataExtraction() throws Exception {
        DataExtraction dataExtraction= DataExtraction.builder()
                .name("data extraction after copro optimization")
                .resourceConn("intics_agadia_db_conn")
                .condition(true)
                .processId("138980184199100180")
                .resultTable("data_extraction")
                .querySet("select origin_id,group_id,processed_file_path as file_path,paper_no,tenant_id,template_id,process_id from info.paper_itemizer where status='COMPLETED' and process_id='138980184199100180' ;")
                .build();
        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url","http://localhost:10182/copro/preprocess/text_extraction");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("write.batch.size","5")));
        DataExtractionAction dataExtractionAction=new DataExtractionAction(actionExecutionAudit,log,dataExtraction);
        dataExtractionAction.execute();

    }

}