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
                .endPoint("https://intics.elevance.ngrok.dev/text-extractor/v2/models/text-extractor-service/versions/1/infer")
                .condition(true)
                .processId("138980184199100180")
                .resultTable("info.data_extraction")
                .querySet("SELECT a.process_id, a.tenant_id, a.template_id, a.group_id, a.origin_id, a.paper_no, a.processed_file_path as file_path,b.root_pipeline_id,c.template_name, b.batch_id, now() as created_on, r.base_prompt as user_prompt, r.system_prompt as system_prompt\n" +
                        " FROM info.auto_rotation a\n" +
                        " left join info.template_detection_result c on c.origin_id=a.origin_id and a.tenant_id=c.tenant_id\n" +
                        " left join sor_meta.radon_prompt_table r on r.tenant_id=a.tenant_id\n" +
                        " join preprocess.preprocess_payload_error_queue b on a.origin_id=b.origin_id and c.tenant_id=b.tenant_id\n" +
                        " where  a.root_pipeline_id =3928;")
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
                .endPoint("https:///v1/deployments/inticsai-dev/deploy-mindee-ocr/predictions")
                .processId("138980184199100180")
                .resultTable("info.data_extraction")
                .querySet("SELECT 1 as batch_id, encode as base64img, 1 as process_id, 1 as tenant_id, 1 as template_id, 1 as group_id, 'INT-1' as origin_id, 1 as paper_no, '/data/input/auto_rotation/26_2.jpg' as file_path, 1 as root_pipeline_id, 'TEXT_EXTRACTOR' as template_name \n" +
                        "from macro.file_details_truth_audit\n" +
                        "limit 1;")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://127.0.0.1:5000/v1/predictions");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("replicate.request.api.token", "API_TOKEN"),
                Map.entry("replicate.text.extraction.version", "1"),
                Map.entry("text.extraction.consumer.API.count", "1"),
                Map.entry("copro.request.activator.handler.name", "TRITON"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("text.extraction.model.name", "ARGON"),
                Map.entry("page.content.min.length.threshold", "1"),
                Map.entry("write.batch.size", "5")));
        DataExtractionAction dataExtractionAction = new DataExtractionAction(actionExecutionAudit, log, dataExtraction);
        dataExtractionAction.execute();

    }

    @Test
    void tritonArgonServer() throws Exception {
        String filePath = "/data/output/processed_images/19-11-2024_02_11_00/tenant_128/group_15/preprocess/autorotation/auto_rotation/PERNIENTAKIS D. COMMERCIAL AND INDUSTRIAL LIMITED LIABILITY COMPANY_1.jpg";
        DataExtraction dataExtraction = DataExtraction.builder()
                .name("data extraction after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("http://192.168.10.245:8300/v2/models/text-extractor-service/versions/1/infer")
                .processId("138980184199100180")
                .resultTable("info.data_extraction")
                .querySet("SELECT 'batch-1' as batch_id, 1 as process_id, 1 as tenant_id, 1 as template_id, 1 as group_id, 'INT-1' as origin_id," +
                        " 1 as paper_no, " +
                        "'" + filePath + "' as file_path," +
                        " 1 as root_pipeline_id, 'TEXT_EXTRACTOR' as template_name, 'Extract all the page content and return as text and dont add preambles' as prompt")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://192.168.10.245:8300/v2/models/text-extractor-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("replicate.request.api.token", "API_TOKEN"),
                Map.entry("replicate.text.extraction.version", "1"),
                Map.entry("copro.request.activator.handler.name", "TRITON"),
                Map.entry("text.extraction.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("text.extraction.model.name", "ARGON"),
                Map.entry("page.content.min.length.threshold", "1"),
                Map.entry("write.batch.size", "5")));
        DataExtractionAction dataExtractionAction = new DataExtractionAction(actionExecutionAudit, log, dataExtraction);
        dataExtractionAction.execute();

    }

    @Test
    void tritonKryptonServer() throws Exception {
        String filePath = "/data/output/processed_images/19-11-2024_02_11_00/tenant_128/group_15/preprocess/autorotation/auto_rotation/PERNIENTAKIS D. COMMERCIAL AND INDUSTRIAL LIMITED LIABILITY COMPANY_1.jpg";
        DataExtraction dataExtraction = DataExtraction.builder()
                .name("data extraction after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("http://192.168.10.245:8300/v2/models/text-extractor-service/versions/1/infer")
                .processId("138980184199100180")
                .resultTable("info.data_extraction")
                .querySet("SELECT 'batch-1' as batch_id, 1 as process_id, 1 as tenant_id, 1 as template_id, 1 as group_id, 'INT-1' as origin_id," +
                        " 1 as paper_no, " +
                        "'" + filePath + "' as file_path," +
                        " 1 as root_pipeline_id, 'TEXT_EXTRACTOR' as template_name, 'Extract all the page content and return as text and dont add preambles' as prompt")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://192.168.10.245:8300/v2/models/text-extractor-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("replicate.request.api.token", "API_TOKEN"),
                Map.entry("replicate.text.extraction.version", "1"),
                Map.entry("copro.request.text.extraction.handler.name", "TRITON"),
                Map.entry("text.extraction.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("preprocess.text.extraction.model.name", "KRYPTON"),
                Map.entry("page.content.min.length.threshold", "1"),
                Map.entry("write.batch.size", "5")));
        DataExtractionAction dataExtractionAction = new DataExtractionAction(actionExecutionAudit, log, dataExtraction);
        dataExtractionAction.execute();

    }


    @Test
    void replicateArgonServer() throws Exception {
        String filePath = "/data/output/processed_images/19-11-2024_02_11_00/tenant_128/group_15/preprocess/autorotation/auto_rotation/PERNIENTAKIS D. COMMERCIAL AND INDUSTRIAL LIMITED LIABILITY COMPANY_1.jpg";
        DataExtraction dataExtraction = DataExtraction.builder()
                .name("data extraction after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("https:///v1/deployments/inticsai-dev/deploy-mindee-ocr/predictions")
                .processId("138980184199100180")
                .resultTable("info.data_extraction")
                .querySet("SELECT 'batch-1' as batch_id, 1 as process_id, 1 as tenant_id, 1 as template_id, 1 as group_id, 'INT-1' as origin_id," +
                        " 1 as paper_no, " +
                        "'" + filePath + "' as file_path," +
                        " 1 as root_pipeline_id, 'TEXT_EXTRACTOR' as template_name, 'Extract all the page content and return as text and dont add preambles' as prompt")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("replicate.request.api.token", "API_TOKEN"),
                Map.entry("replicate.text.extraction.version", "1"),
                Map.entry("copro.request.text.extraction.handler.name", "RUNPOD"),
                Map.entry("text.extraction.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("preprocess.text.extraction.model.name", "ARGON"),
                Map.entry("page.content.min.length.threshold", "1"),
                Map.entry("write.batch.size", "5")));
        DataExtractionAction dataExtractionAction = new DataExtractionAction(actionExecutionAudit, log, dataExtraction);
        dataExtractionAction.execute();

    }

    @Test
    void replicateKryptonServer() throws Exception {
        String filePath = "/data/output/processed_images/19-11-2024_02_11_00/tenant_128/group_15/preprocess/autorotation/auto_rotation/PERNIENTAKIS D. COMMERCIAL AND INDUSTRIAL LIMITED LIABILITY COMPANY_1.jpg";
        DataExtraction dataExtraction = DataExtraction.builder()
                .name("data extraction after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("https://api.runpod.ai/v2/6suxkm7225dbxg/runsync")
                .processId("138980184199100180")
                .resultTable("info.data_extraction")
                .querySet("SELECT a.process_id, a.tenant_id, a.template_id, a.group_id, a.origin_id, a.paper_no, a.processed_file_path as file_path,\n" +
                        "b.root_pipeline_id,c.template_name, b.batch_id, now() as created_on, r.base_prompt as user_prompt,\n" +
                        "r.system_prompt as system_prompt                   \n" +
                        "FROM info.auto_rotation a left join info.template_detection_result c on c.origin_id=a.origin_id\n" +
                        "  and a.tenant_id=c.tenant_id  left join sor_meta.radon_prompt_table r on r.tenant_id=a.tenant_id                     \n" +
                        "  join preprocess.preprocess_payload_error_queue b on a.origin_id=b.origin_id and c.tenant_id=b.tenant_id\n" +
                        "where a.group_id=1 and a.tenant_id = 1 and b.batch_id ='BATCH-1_0' \n" +
                        "and r.process='TEXT_EXTRACTION' and r.use_case='PREPROCESS' and r.document_type='HEALTH_CARE';")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "https://api.runpod.ai/v2/6suxkm7225dbxg/runsync");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("replicate.request.api.token", "API_TOKEN"),
                Map.entry("replicate.text.extraction.version", "1"),
                Map.entry("copro.request.text.extraction.handler.name", "RUNPOD"),
                Map.entry("text.extraction.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("preprocess.text.extraction.model.name", "ARGON"),
                Map.entry("page.content.min.length.threshold", "1"),
                Map.entry("write.batch.size", "5")));
        DataExtractionAction dataExtractionAction = new DataExtractionAction(actionExecutionAudit, log, dataExtraction);
        dataExtractionAction.execute();

    }


}