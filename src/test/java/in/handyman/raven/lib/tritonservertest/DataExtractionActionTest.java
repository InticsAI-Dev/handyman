package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.DataExtractionAction;
import in.handyman.raven.lib.model.DataExtraction;
import in.handyman.raven.lib.model.textextraction.DataExtractionConsumerProcess;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;

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
                .querySet("select 'INT-1' as origin_id, 1 as group_id,'/data/output/646/preprocess/paper_itemizer/pdf_to_image/SYNT_166522063_c1/SYNT_166522063_c1_1.jpg' as file_path, 1 as paper_no, 1 as tenant_id, 1 as template_id, 1 as root_pipeline_id, '138980184199100180' as process_id")
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
                .endPoint("http://192.168.10.248:8300/v2/models/text-extractor-service/versions/1/infer")
                .processId("138980184199100180")
                .resultTable("info.data_extraction")
                .querySet("SELECT 1 as batch_id, encode as base64img, 1 as process_id, 1 as tenant_id, 1 as template_id, 1 as group_id, 'INT-1' as origin_id, 1 as paper_no, '/data/output/auto_rotation/h_hart_packet_0.jpg' as file_path, 1 as root_pipeline_id, 'TEXT_EXTRACTOR' as template_name " +
                        "from macro.file_details_truth_audit")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://192.168.10.248:8300/v2/models/text-extractor-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("text.extraction.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "false"),

                Map.entry("replicate.request.api.token", ""),
                Map.entry("", ""),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size", "5")));
        DataExtractionAction dataExtractionAction = new DataExtractionAction(actionExecutionAudit, log, dataExtraction);
        dataExtractionAction.execute();

    }

    @Test
    void replicateServer() throws Exception {
        DataExtraction dataExtraction = DataExtraction.builder()
                .name("data extraction after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("https://api.runpod.ai/v2/6suxkm7225dbxg/runsync")
                .processId("138980184199100180")
                .resultTable("info.data_extraction")
                .querySet("SELECT 1 as batch_id, 1 as process_id, 1 as tenant_id,now() as created_on, 1 as template_id, 1 as group_id, 'INT-1' as origin_id, 1 as paper_no, '/data/output/1/preprocess/paper_itemizer/pdf_to_image/SYNT_166730538_c2/SYNT_166730538_c2_1.jpg' as file_path, 1 as root_pipeline_id, 'TEXT_EXTRACTOR' as template_name \n" )
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "https://api.runpod.ai/v2/6suxkm7225dbxg/runsync");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("text.extraction.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("replicate.request.api.token", "rpa_CL6G4IM6TIUB9MVMYED07G29IFG5NST4YJPD6OQB147pd2"),
                Map.entry("copro.request.activator.handler.name", "REPLICATE"),
                Map.entry("page.content.min.length.threshold", "5"),
                Map.entry("replicate.text.extraction.version", ""),
//                Map.entry("replicate.request.api.token", ""),
                Map.entry("", ""),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size", "5")));
        DataExtractionAction dataExtractionAction = new DataExtractionAction(actionExecutionAudit, log, dataExtraction);
        dataExtractionAction.execute();

    }


}
