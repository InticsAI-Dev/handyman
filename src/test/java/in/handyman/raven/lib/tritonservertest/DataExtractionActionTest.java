package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.DataExtractionAction;
import in.handyman.raven.lib.model.DataExtraction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class DataExtractionActionTest {
//    @Test
//    void dataExtraction() throws Exception {
//        DataExtraction dataExtraction = DataExtraction.builder()
//                .name("data extraction after copro optimization")
//                .resourceConn("intics_zio_db_conn")
//                .endPoint("${copro.data-extraction.url}")
//                .condition(true)
//                .processId("138980184199100180")
//                .resultTable("info.data_extraction")
//                .querySet("select 'INT-1' as origin_id, 1 as group_id,'/data/output/auto_rotation/h_hart_packet_0.jpg' as file_path, 1 as paper_no, 1 as tenant_id, 1 as template_id, 1 as root_pipeline_id, '138980184199100180' as process_id from info.paper_itemizer where status='COMPLETED' and origin_id='INT-1';;")
//                .build();
//        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
//        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://192.168.10.239:10182/copro/preprocess/text-extraction");
//        actionExecutionAudit.setProcessId(138980079308730208L);
//        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
//                Map.entry("okhttp.client.timeout", "20"),
//                Map.entry("text.extraction.consumer.API.count", "1"),
//                Map.entry("write.batch.size", "5")));
//        DataExtractionAction dataExtractionAction = new DataExtractionAction(actionExecutionAudit, log, dataExtraction);
//        dataExtractionAction.execute();
//
//    }

    @Test
    void dataExtraction() throws Exception {
        DataExtraction dataExtraction = DataExtraction.builder()
                .name("data extraction after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .endPoint("http://192.168.10.248:8300/v2/models/text-extractor-service/versions/1/infer")
                .condition(true)
                .processId("138980184199100180")
                .resultTable("info.data_extraction")
                .querySet("select 'INT-1' as origin_id, 1 as group_id,'/data/output/646/preprocess/paper_itemizer/pdf_to_image/SYNT_166522063_c1/SYNT_166522063_c1_1.jpg' as file_path, 1 as paper_no, 1 as tenant_id, 1 as template_id, 1 as root_pipeline_id, '138980184199100180' as process_id, '1' as batch_id")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://192.168.10.248:8300/v2/models/text-extractor-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("text.extraction.consumer.API.count", "1"),
                Map.entry("write.batch.size", "5")));
        DataExtractionAction dataExtractionAction = new DataExtractionAction(actionExecutionAudit, log, dataExtraction);
        dataExtractionAction.execute();

    }


    @Test
    void tritonServer() throws Exception {
        DataExtraction dataExtraction = DataExtraction.builder()
                .name("data extraction after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("http://192.168.10.239:10182/copro/preprocess/text-extraction")
                .processId("138980184199100180")
                .resultTable("info.data_extraction")
                .querySet("SELECT 1 as process_id, 1 as tenant_id, 1 as template_id, 1 as group_id, 'INT-1' as origin_id, 1 as paper_no, '/data/output/auto_rotation/h_hart_packet_0.jpg' as file_path, 1 as root_pipeline_id, 'TEXT_EXTRACTOR' as template_name")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://192.168.10.239:10182/copro/preprocess/text-extraction");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("text.extraction.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size", "5")));
        DataExtractionAction dataExtractionAction = new DataExtractionAction(actionExecutionAudit, log, dataExtraction);
        dataExtractionAction.execute();

    }


}