package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.AgenticPaperFilterAction;
import in.handyman.raven.lib.DataExtractionAction;
import in.handyman.raven.lib.model.AgenticPaperFilter;
import in.handyman.raven.lib.model.DataExtraction;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import java.io.File;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_AGENTIC_FILTER_OUTPUT;

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
                Map.entry("copro.request.text.extraction.handler.name", "REPLICATE"),
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
                .endPoint("https:///v1/deployments/inticsai-dev/deploy-mindee-ocr/predictions")
                .processId("138980184199100180")
                .resultTable("info.data_extraction")
                .querySet("SELECT 'batch-1' as batch_id, 1 as process_id, 1 as tenant_id, 1 as template_id, 1 as group_id, 'INT-1' as origin_id," +
                        " 1 as paper_no, " +
                        "'" + filePath + "' as file_path," +
                        " 1 as root_pipeline_id, 'TEXT_EXTRACTOR' as template_name, 'Extract all the page content and return as text and dont add preambles' as prompt")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://192.168.10.241:7700/v2/models/krypton-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("replicate.request.api.token", "API_TOKEN"),
                Map.entry("replicate.text.extraction.version", "1"),
                Map.entry("copro.request.activator.handler.name", "REPLICATE"),
                Map.entry("text.extraction.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("text.extraction.model.name", "KRYPTON"),
                Map.entry("page.content.min.length.threshold", "1"),
                Map.entry("write.batch.size", "5")));
        DataExtractionAction dataExtractionAction = new DataExtractionAction(actionExecutionAudit, log, dataExtraction);
        dataExtractionAction.execute();

    }

    @Test
    void tritonKrypton() throws Exception {
        AgenticPaperFilter dataExtraction = AgenticPaperFilter.builder()
                .name("data extraction after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("http://172.203.90.211:8000/predict")
                .processId("377")
                //.containerName("Service_Container")
                .resultTable("paper_filter.agentic_paper_filter_output_audit")
                .querySet("SELECT\n" +
                        "  '377' AS origin_id,\n" +
                        "  11 AS group_id,\n" +
                        "  '/home/sindhujaa.kamaraj@zucisystems.com/Downloads/40_image 3/40_image/allkeywords_1.png' AS file_path,\n" +
                        "  5 AS paper_no,\n" +
                        "  1 AS tenant_id,\n" +
                        "  'TEMPLATE-001' AS template_id,\n" +
                        "  377 AS process_id,\n" +
                        "  377 AS root_pipeline_id,\n" +
                        "  'Dummy Template Name' AS template_name,\n" +
                        "  'BATCH-11_0' AS batch_id,\n" +
                        "  NOW() AS created_on,\n" +
                        "  'Please classify the attached AUMI-BH document by identifying the explicit presence (true) or absence (false) of each container according to the updated System Prompt detection criteria above. Return only the final JSON object with all five boolean values—no additional text.' AS user_prompt,\n" +
                        "  'You are an advanced document classification assistant for healthcare forms—specifically Pega’s AUMI-BH (Automated Utilization Management Interface – Behavioral Health) prior-authorization documents.\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"Context (AUMI-BH):\\n\" +\n" +
                        "                \"  AUMI-BH documents are behavioral-health prior-authorization forms. They typically include information about the patient (member), the referring provider (the clinician submitting the request), the servicing provider (the facility or clinician who will render the service), servicing facility details (e.g., hospital or clinic where services occur), and service-level details (e.g., CPT/ICD-10 codes, authorization IDs, admission/discharge dates, urgency indicators, levels of care/service). Our goal is to never miss any document that contains at least one piece of these data types.\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"Your job is to analyze the OCR-extracted text from an AUMI-BH document and output a strict JSON indicating true/false for each of these five predefined containers:\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"- MEMBER_DETAILS  \\n\" +\n" +
                        "                \"- REFERRING_PROVIDER_DETAILS  \\n\" +\n" +
                        "                \"- SERVICING_PROVIDER_DETAILS  \\n\" +\n" +
                        "                \"- SERVICING_FACILITY_DETAILS  \\n\" +\n" +
                        "                \"- SERVICING_DETAILS  \\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"Always return all containers explicitly—never omit any—and never return an empty or partial JSON.\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"Detection Criteria:\\n\" +\n" +
                        "                \"You must ONLY mark a container as true if you find at least ONE of its Field Aliases (listed below) in the text (case-insensitive, handle singular/plural variations, allow punctuation/spacing differences). Do NOT rely on section headers alone—only explicit Field Aliases count.\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"Containers and Updated Field Aliases\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"MEMBER_DETAILS  \\n\" +\n" +
                        "                \"- Name: [\\\"Member Name\\\", \\\"Patient Name\\\", \\\"Subscriber Name\\\", \\\"Enrollee Name\\\", \\\"First Name\\\", \\\"Last Name\\\", \\\"Full Name\\\"]  \\n\" +\n" +
                        "                \"- Date of Birth: [\\\"DOB\\\", \\\"Date of Birth\\\", \\\"Birth Date\\\"]  \\n\" +\n" +
                        "                \"- Age: [\\\"Age\\\", \\\"Patient Age\\\", \\\"Member Age\\\"]  \\n\" +\n" +
                        "                \"- Gender: [\\\"Gender\\\", \\\"Sex\\\", \\\"Patient Sex\\\", \\\"Member Sex\\\"]  \\n\" +\n" +
                        "                \"- Address: [\\\"Member Address\\\", \\\"Patient Address\\\", \\\"Residence Address\\\"]  \\n\" +\n" +
                        "                \"- City/State/Zip: [\\\"Member City\\\", \\\"Member State\\\", \\\"Member Zip\\\", \\\"Member Postal Code\\\", \\\"Patient City\\\", \\\"Patient State\\\", \\\"Patient Zip\\\", \\\"Patient Postal Code\\\"]  \\n\" +\n" +
                        "                \"- Identifiers: [\\\"Member ID\\\", \\\"Medicaid ID\\\", \\\"Group ID\\\", \\\"Patient ID\\\", \\\"Mclient ID\\\", \\\"Subscriber ID\\\"]\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"REFERRING_PROVIDER_DETAILS  \\n\" +\n" +
                        "                \"- Name: [\\\"Referring Provider Name\\\", \\\"Requesting Provider Name\\\", \\\"Referrer Name\\\", \\\"Ordering Physician\\\", \\\"Consulting Physician\\\"]  \\n\" +\n" +
                        "                \"- NPI: [\\\"Referring NPI\\\", \\\"Provider NPI\\\", \\\"NPI #\\\", \\\"NPI No.\\\"]  \\n\" +\n" +
                        "                \"- Identifiers: [\\\"Tax ID\\\", \\\"TIN\\\", \\\"Provider ID\\\", \\\"Employer ID\\\"]  \\n\" +\n" +
                        "                \"- Specialty: [\\\"Specialty\\\", \\\"Speciality\\\"]  \\n\" +\n" +
                        "                \"- Location: [\\\"Referring Provider Address\\\", \\\"Office Address\\\", \\\"Office Location\\\", \\\"Provider City\\\", \\\"Provider State\\\", \\\"Provider Zip\\\", \\\"Provider Postal Code\\\"]  \\n\" +\n" +
                        "                \"- Contact: [\\\"Provider Phone\\\", \\\"Office Phone\\\", \\\"Provider Fax\\\", \\\"Office Fax\\\"]\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"SERVICING_PROVIDER_DETAILS  \\n\" +\n" +
                        "                \"- Name: [\\\"Provider Name\\\", \\\"Rendering Provider Name\\\", \\\"Attending Physician\\\", \\\"Therapist\\\", \\\"Practice Name\\\", \\\"Physician Name\\\"]  \\n\" +\n" +
                        "                \"- NPI: [\\\"Provider NPI\\\", \\\"NPI#\\\", \\\"NPI No.\\\", \\\"Organization NPI\\\"]  \\n\" +\n" +
                        "                \"- Identifiers: [\\\"Tax ID\\\", \\\"TIN\\\", \\\"Provider ID\\\"]  \\n\" +\n" +
                        "                \"- Specialty: [\\\"Specialty\\\", \\\"Speciality\\\"]  \\n\" +\n" +
                        "                \"- Location: [\\\"Provider Address\\\", \\\"Office Address\\\", \\\"Provider City\\\", \\\"Provider State\\\", \\\"Provider Zip\\\", \\\"Provider Postal Code\\\"]  \\n\" +\n" +
                        "                \"- Contact: [\\\"Provider Phone\\\", \\\"Office Phone\\\", \\\"Provider Fax\\\", \\\"Office Fax\\\"]\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"SERVICING_FACILITY_DETAILS  \\n\" +\n" +
                        "                \"- Name: [\\\"Facility Name\\\", \\\"Admitting Facility\\\", \\\"Hospital Name\\\", \\\"Clinic Name\\\", \\\"Agency Name\\\", \\\"Site Name\\\"]  \\n\" +\n" +
                        "                \"- Location: [\\\"Facility Address\\\", \\\"Facility City\\\", \\\"Facility State\\\", \\\"Facility Zip\\\", \\\"Facility Postal Code\\\"]  \\n\" +\n" +
                        "                \"- Contact: [\\\"Facility Phone\\\", \\\"Facility Fax\\\", \\\"Facility Email\\\"]  \\n\" +\n" +
                        "                \"- Identifiers: [\\\"Facility Tax ID\\\", \\\"Facility TIN\\\"]\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"SERVICING_DETAILS  \\n\" +\n" +
                        "                \"- Codes: [\\\"CPT Code\\\", \\\"CPT\\\", \\\"Procedure Code\\\", \\\"Proc Code\\\", \\\"ICD-10\\\", \\\"ICD Code\\\", \\\"ICD Codes\\\", \\\"Diagnosis\\\", \\\"Diagnosis Code\\\", \\\"Diagnoses\\\", \\\"Diagnoses Code\\\"]  \\n\" +\n" +
                        "                \"- Identifiers: [\\\"Auth ID\\\", \\\"Authorization ID\\\", \\\"MMS ID\\\"]  \\n\" +\n" +
                        "                \"- Dates: [\\\"Admit Date\\\", \\\"Admission Date\\\", \\\"Discharge Date\\\", \\\"Date of Service\\\", \\\"Service Start Date\\\", \\\"Service End Date\\\"]  \\n\" +\n" +
                        "                \"- Metrics: [\\\"Level of Care\\\", \\\"Level of Service\\\", \\\"Urgent\\\", \\\"Urgency\\\", \\\"RUSH\\\", \\\"STAT\\\", \\\"Expedite\\\", \\\"Immediate\\\"]  \\n\" +\n" +
                        "                \"- Details: [\\\"Procedure Description\\\", \\\"Service Description\\\", \\\"Requested Service\\\"]\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"Classification Logic (Strict):\\n\" +\n" +
                        "                \"- MEMBER_DETAILS, REFERRING_PROVIDER_DETAILS, SERVICING_PROVIDER_DETAILS, SERVICING_FACILITY_DETAILS:  \\n\" +\n" +
                        "                \"  - Mark true if at least one Field Alias from that container appears anywhere in the text.  \\n\" +\n" +
                        "                \"  - Otherwise, mark false.\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"- SERVICING_DETAILS:  \\n\" +\n" +
                        "                \"  - Mark true if at least one Field Alias (from Codes, Identifiers, Dates, Metrics, or Details) appears.  \\n\" +\n" +
                        "                \"  - Otherwise, mark false.\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"Intelligent Handling of Variations:\\n\" +\n" +
                        "                \"1. Case-insensitive matching: “cpt code”, “CPT CODE”, and “Cpt Code” all match.  \\n\" +\n" +
                        "                \"2. Singular/plural variations: “Diagnosis” matches “Diagnoses,” “Diagnosis Code” matches “Diagnosis Codes,” etc. “Provider Name” matches “Provider Names.”  \\n\" +\n" +
                        "                \"3. Punctuation/spacing tolerance: “ICD-10”, “ICD10”, “ICD 10” all match. “DOB”, “D.O.B.”, “DOB:” all match.  \\n\" +\n" +
                        "                \"4. Do NOT rely on header terms alone (“Patient,” “Provider,” etc.)—only explicit Field Aliases count.  \\n\" +\n" +
                        "                \"5. Ignore section-header aliases if no Field Alias is present.\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"Final Output (strict JSON only):\\n\" +\n" +
                        "                \"```json\\n\" +\n" +
                        "                \"{\\n\" +\n" +
                        "                \"  \\\"MEMBER_DETAILS\\\": false,\\n\" +\n" +
                        "                \"  \\\"REFERRING_PROVIDER_DETAILS\\\": false,\\n\" +\n" +
                        "                \"  \\\"SERVICING_PROVIDER_DETAILS\\\": false,\\n\" +\n" +
                        "                \"  \\\"SERVICING_FACILITY_DETAILS\\\": false,\\n\" +\n" +
                        "                \"  \\\"SERVICING_DETAILS\\\": false\\n\" +\n" +
                        "                \"}\\n\" +\n" +
                        "                \"```\\n\" +\n" +
                        "                \"\\n\" +\n" +
                        "                \"No explanations or additional text allowed.' AS system_prompt,\n" +
                        "  'iVBORw0KGgoAAAANSUhEUgAAAAUA' AS base64_img,\n" +
                        "  'KRYPTON' AS model_name;\n")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.agentic.paper.filter.url", "http://172.203.90.211:8000/predict");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("replicate.request.api.token", "API_TOKEN"),
                Map.entry("copro.processor.thread.creator", "FIXED_THREAD"),
                Map.entry("replicate.text.extraction.version", "1"),
                Map.entry("copro.request.agentic.paper.filter.extraction.handler.name", "TRITON"),
                Map.entry("agentic.paper.filter.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("preprocess.agentic.paper.filter.model.name", "KRYPTON"),
                Map.entry("pipeline.copro.api.process.file.format", "BASE64"),
                Map.entry(ENCRYPT_AGENTIC_FILTER_OUTPUT, "false"),
                Map.entry("page.content.min.length.threshold", "1"),
                Map.entry("write.batch.size", "5"),
                Map.entry("copro.isretry.enabled", "false")));
        AgenticPaperFilterAction dataExtractionAction = new AgenticPaperFilterAction(actionExecutionAudit, log, dataExtraction);
        dataExtractionAction.execute();

    }

//    @Test
//    void multiREquestKryptonAndOptimus() throws Exception {
//
//        String folderPath = "/home/sindhujaa.kamaraj@zucisystems.com/Downloads/40_image 3/40_image"; // 🔁 Change this path to your actual folder
//        File folder = new File(folderPath);
//
//        File[] imageFiles = folder.listFiles((dir, name) ->
//                name.toLowerCase().endsWith(".png") ||
//                        name.toLowerCase().endsWith(".jpg") ||
//                        name.toLowerCase().endsWith(".jpeg")
//        );
//
//        if (imageFiles == null || imageFiles.length == 0) {
//            System.out.println("No image files found in folder: " + folderPath);
//            return;
//        }
//
//        List<File> imageList = Arrays.asList(imageFiles);
//        int mid = imageList.size() / 2;
//
//        List<File> optimusFiles = imageList.subList(0, mid);
//        List<File> kryptonFiles = imageList.subList(mid, imageList.size());
//
//        for (File file : optimusFiles) {
//            sendImageToModelApi(file, "OPTIMUS");
//        }
//
//        for (File file : kryptonFiles) {
//            sendImageToModelApi(file, "KRYPTON");
//        }
//    }
//    private void sendImageToModelApi(File file, String model) throws Exception {
//        byte[] imageBytes = Files.readAllBytes(file.toPath());
//        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
//
//        ObjectMapper mapper = new ObjectMapper();
//        ObjectNode requestBody = mapper.createObjectNode();
//        requestBody.put("model", model);
//        requestBody.put("fileName", file.getName());
//        requestBody.put("fileContent", base64Image);
//        // Add any other required fields like processId, templateId, etc.
//
//        String endpoint = "http://172.203.90.211:8000/predict";
//
//        HttpPost post = new HttpPost(endpoint);
//        post.setHeader("Content-Type", "application/json");
//        post.setEntity(new StringEntity(mapper.writeValueAsString(requestBody), StandardCharsets.UTF_8));
//
//        try (CloseableHttpClient client = HttpClients.createDefault();
//             CloseableHttpResponse response = client.execute(post)) {
//
//            String responseString = EntityUtils.toString(response.getEntity());
//            System.out.println("[" + model + "] Image: " + file.getName() + " → Response: ");
//        }
//    }

    @Test
    void tritonKryptonAndOptimusForFolder() throws Exception {
        // Folder path to your images

       int apiCount = 25;
        String folderPath = "/home/sindhujaa.kamaraj@zucisystems.com/Downloads/40_image 3/40_image";  // Replace this with the actual folder path
        Integer api = 1;
        // Get list of image files from the folder
        File folder = new File(folderPath);
        File[] imageFiles = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".png") ||
                        name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".jpeg")
        );

        if (imageFiles == null || imageFiles.length == 0) {
            throw new Exception("No images found in the folder: " + folderPath);
        }

        // Split image files for Optimus and Krypton (half for Optimus and half for Krypton)
        int mid = imageFiles.length / 2;
        List<File> kryptonImages = Arrays.asList(Arrays.copyOfRange(imageFiles, 0, imageFiles.length));
        //List<File> optimusImages = Arrays.asList(Arrays.copyOfRange(imageFiles, mid, imageFiles.length));

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            try {
                for (int i = 0; i < kryptonImages.size(); i += apiCount) {
                    List<File> batch = kryptonImages.subList(i, Math.min(i + apiCount, kryptonImages.size()));
                    for (File file : batch) {
                        //System.out.println("📄 Running: " + file.getName() + " | Model: KRYPTON");
                        processImageForModel(file);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });



        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
    }

    private void processImageForModel(File file) throws Exception {

        System.out.println("Processing file: " + file.getName());

    // Assuming we want to alternate between KRYPTON and OPTIMUS for each file
    String modelName = (file.hashCode() % 2 == 0) ? "KRYPTON" : "OPTIMUS";  // Just an example logic

    // Log the model being used for this file
//    System.out.println("Using model: " + modelName);

    // Assuming the image files are being sent in base64 format as per the original code
    byte[] imageBytes = java.nio.file.Files.readAllBytes(file.toPath());
    String base64Image = java.util.Base64.getEncoder().encodeToString(imageBytes);

    // Define different prompts based on the model name
    String userPrompt = "";
    String systemPrompt = "";

    if ("KRYPTON".equalsIgnoreCase(modelName)) {
        userPrompt = "Please classify the attached AUMI-BH document by identifying the explicit presence (true) or absence (false) of each container according to the updated System Prompt detection criteria above. Return only the final JSON object with all five boolean values—no additional text.";

        systemPrompt = "You are an advanced document classification assistant for healthcare forms—specifically Pega’s AUMI-BH (Automated Utilization Management Interface – Behavioral Health) prior-authorization documents.\n" +
                "\n" +
                "Context (AUMI-BH):\n" +
                "  AUMI-BH documents are behavioral-health prior-authorization forms. They typically include information about the patient (member), the referring provider (the clinician submitting the request), the servicing provider (the facility or clinician who will render the service), servicing facility details (e.g., hospital or clinic where services occur), and service-level details (e.g., CPT/ICD-10 codes, authorization IDs, admission/discharge dates, urgency indicators, levels of care/service). Our goal is to never miss any document that contains at least one piece of these data types.\n" +
                "\n" +
                "Your job is to analyze the OCR-extracted text from an AUMI-BH document and output a strict JSON indicating true/false for each of these five predefined containers:\n" +
                "\n" +
                "- MEMBER_DETAILS  \n" +
                "- REFERRING_PROVIDER_DETAILS  \n" +
                "- SERVICING_PROVIDER_DETAILS  \n" +
                "- SERVICING_FACILITY_DETAILS  \n" +
                "- SERVICING_DETAILS  \n" +
                "\n" +
                "Always return all containers explicitly—never omit any—and never return an empty or partial JSON.\n" +
                "\n" +
                "Detection Criteria:\n" +
                "You must ONLY mark a container as true if you find at least ONE of its Field Aliases (listed below) in the text (case-insensitive, handle singular/plural variations, allow punctuation/spacing differences). Do NOT rely on section headers alone—only explicit Field Aliases count.\n" +
                "\n" +
                "Containers and Updated Field Aliases\n" +
                "\n" +
                "MEMBER_DETAILS  \n" +
                "- Name: [\"Member Name\", \"Patient Name\", \"Subscriber Name\", \"Enrollee Name\", \"First Name\", \"Last Name\", \"Full Name\"]  \n" +
                "- Date of Birth: [\"DOB\", \"Date of Birth\", \"Birth Date\"]  \n" +
                "- Age: [\"Age\", \"Patient Age\", \"Member Age\"]  \n" +
                "- Gender: [\"Gender\", \"Sex\", \"Patient Sex\", \"Member Sex\"]  \n" +
                "- Address: [\"Member Address\", \"Patient Address\", \"Residence Address\"]  \n" +
                "- City/State/Zip: [\"Member City\", \"Member State\", \"Member Zip\", \"Member Postal Code\", \"Patient City\", \"Patient State\", \"Patient Zip\", \"Patient Postal Code\"]  \n" +
                "- Identifiers: [\"Member ID\", \"Medicaid ID\", \"Group ID\", \"Patient ID\", \"Mclient ID\", \"Subscriber ID\"]\n" +
                "\n" +
                "REFERRING_PROVIDER_DETAILS  \n" +
                "- Name: [\"Referring Provider Name\", \"Requesting Provider Name\", \"Referrer Name\", \"Ordering Physician\", \"Consulting Physician\"]  \n" +
                "- NPI: [\"Referring NPI\", \"Provider NPI\", \"NPI #\", \"NPI No.\"]  \n" +
                "- Identifiers: [\"Tax ID\", \"TIN\", \"Provider ID\", \"Employer ID\"]  \n" +
                "- Specialty: [\"Specialty\", \"Speciality\"]  \n" +
                "- Location: [\"Referring Provider Address\", \"Office Address\", \"Office Location\", \"Provider City\", \"Provider State\", \"Provider Zip\", \"Provider Postal Code\"]  \n" +
                "- Contact: [\"Provider Phone\", \"Office Phone\", \"Provider Fax\", \"Office Fax\"]\n" +
                "\n" +
                "SERVICING_PROVIDER_DETAILS  \n" +
                "- Name: [\"Provider Name\", \"Rendering Provider Name\", \"Attending Physician\", \"Therapist\", \"Practice Name\", \"Physician Name\"]  \n" +
                "- NPI: [\"Provider NPI\", \"NPI#\", \"NPI No.\", \"Organization NPI\"]  \n" +
                "- Identifiers: [\"Tax ID\", \"TIN\", \"Provider ID\"]  \n" +
                "- Specialty: [\"Specialty\", \"Speciality\"]  \n" +
                "- Location: [\"Provider Address\", \"Office Address\", \"Provider City\", \"Provider State\", \"Provider Zip\", \"Provider Postal Code\"]  \n" +
                "- Contact: [\"Provider Phone\", \"Office Phone\", \"Provider Fax\", \"Office Fax\"]\n" +
                "\n" +
                "SERVICING_FACILITY_DETAILS  \n" +
                "- Name: [\"Facility Name\", \"Admitting Facility\", \"Hospital Name\", \"Clinic Name\", \"Agency Name\", \"Site Name\"]  \n" +
                "- Location: [\"Facility Address\", \"Facility City\", \"Facility State\", \"Facility Zip\", \"Facility Postal Code\"]  \n" +
                "- Contact: [\"Facility Phone\", \"Facility Fax\", \"Facility Email\"]  \n" +
                "- Identifiers: [\"Facility Tax ID\", \"Facility TIN\"]\n" +
                "\n" +
                "SERVICING_DETAILS  \n" +
                "- Codes: [\"CPT Code\", \"CPT\", \"Procedure Code\", \"Proc Code\", \"ICD-10\", \"ICD Code\", \"ICD Codes\", \"Diagnosis\", \"Diagnosis Code\", \"Diagnoses\", \"Diagnoses Code\"]  \n" +
                "- Identifiers: [\"Auth ID\", \"Authorization ID\", \"MMS ID\"]  \n" +
                "- Dates: [\"Admit Date\", \"Admission Date\", \"Discharge Date\", \"Date of Service\", \"Service Start Date\", \"Service End Date\"]  \n" +
                "- Metrics: [\"Level of Care\", \"Level of Service\", \"Urgent\", \"Urgency\", \"RUSH\", \"STAT\", \"Expedite\", \"Immediate\"]  \n" +
                "- Details: [\"Procedure Description\", \"Service Description\", \"Requested Service\"]\n" +
                "\n" +
                "Classification Logic (Strict):\n" +
                "- MEMBER_DETAILS, REFERRING_PROVIDER_DETAILS, SERVICING_PROVIDER_DETAILS, SERVICING_FACILITY_DETAILS:  \n" +
                "  - Mark true if at least one Field Alias from that container appears anywhere in the text.  \n" +
                "  - Otherwise, mark false.\n" +
                "\n" +
                "- SERVICING_DETAILS:  \n" +
                "  - Mark true if at least one Field Alias (from Codes, Identifiers, Dates, Metrics, or Details) appears.  \n" +
                "  - Otherwise, mark false.\n" +
                "\n" +
                "Intelligent Handling of Variations:\n" +
                "1. Case-insensitive matching: “cpt code”, “CPT CODE”, and “Cpt Code” all match.  \n" +
                "2. Singular/plural variations: “Diagnosis” matches “Diagnoses,” “Diagnosis Code” matches “Diagnosis Codes,” etc. “Provider Name” matches “Provider Names.”  \n" +
                "3. Punctuation/spacing tolerance: “ICD-10”, “ICD10”, “ICD 10” all match. “DOB”, “D.O.B.”, “DOB:” all match.  \n" +
                "4. Do NOT rely on header terms alone (“Patient,” “Provider,” etc.)—only explicit Field Aliases count.  \n" +
                "5. Ignore section-header aliases if no Field Alias is present.\n" +
                "\n" +
                "Final Output (strict JSON only):\n" +
                "```json\n" +
                "{\n" +
                "  \"MEMBER_DETAILS\": false,\n" +
                "  \"REFERRING_PROVIDER_DETAILS\": false,\n" +
                "  \"SERVICING_PROVIDER_DETAILS\": false,\n" +
                "  \"SERVICING_FACILITY_DETAILS\": false,\n" +
                "  \"SERVICING_DETAILS\": false\n" +
                "}\n" +
                "```\n" +
                "\n" +
                "No explanations or additional text allowed.";
    } else if ("OPTIMUS".equalsIgnoreCase(modelName)) {
        userPrompt = "Does the document contain any of the following **service-related keywords**?\n" +
                "\n" +
                "* Codes: CPT, ICD-10, Diagnosis, Procedure Code\n" +
                "* Auth IDs: Auth ID, Authorization ID, MMS ID, Ref Number\n" +
                "* Dates: Start Date, Admission Date, Date of Service, Discharge Date\n" +
                "* Urgency: Urgent, STAT, Emergency, RUSH, Level of Care\n" +
                "\n" +
                "If **any one** of these appears anywhere in the document, respond: **Yes**\n" +
                "Otherwise, respond: **No**\n" +
                "\n" +
                "Answer directly.\n" +
                "Respond concisely.";

        systemPrompt = "You are a document classification assistant.\n" +
                "\n" +
                "\tYour task is to determine if a medical insurance or prior-authorization document contains **requested or authorized service information** for the member.\n" +
                "\n" +
                "\tThis includes:\n" +
                "\n" +
                "\t* Procedure or diagnosis codes (e.g., CPT, ICD-10)\n" +
                "\t* Authorization or reference numbers (e.g., Auth ID, Ref Number)\n" +
                "\t* Service or admission dates\n" +
                "\t* Urgency indicators (e.g., Urgent, STAT, Emergency)\n" +
                "\n" +
                "\tAnswer directly.\n" +
                "\tRespond concisely.\n" +
                "\tReturn only a single word: **Yes** or **No**.";
    }

    // Create the AgenticPaperFilter object with dynamic prompts
    AgenticPaperFilter dataExtraction = AgenticPaperFilter.builder()
            .name("data extraction after copro optimization")
            .resourceConn("intics_zio_db_conn")
            .condition(true)
            .endPoint("http://172.203.90.211:8000/predict")
            .processId("377")
            .resultTable("paper_filter.agentic_paper_filter_output_audit")
            .querySet("SELECT\n" +
                    "  '377' AS origin_id,\n" +
                    "  11 AS group_id,\n" +
                    "  '" + file.getAbsolutePath() + "' AS file_path,\n" +
                    "  5 AS paper_no,\n" +
                    "  1 AS tenant_id,\n" +
                    "  'TEMPLATE-001' AS template_id,\n" +
                    "  377 AS process_id,\n" +
                    "  377 AS root_pipeline_id,\n" +
                    "  'Dummy Template Name' AS template_name,\n" +
                    "  'BATCH-11_0' AS batch_id,\n" +
                    "  NOW() AS created_on,\n" +
                    "  '" + userPrompt + "' AS user_prompt,\n" +
                    "  '" + systemPrompt + "' AS system_prompt,\n" +
                    "  '" + base64Image + "' AS base64_img,\n" +
                    "  '" + modelName + "' AS model_name;\n")
            .build();

    ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
    actionExecutionAudit.getContext().put("copro.agentic.paper.filter.url", "http://172.203.90.211:8000/predict");
    actionExecutionAudit.setProcessId(138980079308730208L);
    actionExecutionAudit.setActionId(1L);
    actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
            Map.entry("okhttp.client.timeout", "20"),
                    Map.entry("replicate.request.api.token", "API_TOKEN"),
                    Map.entry("copro.processor.thread.creator", "FIXED_THREAD"),
                    Map.entry("replicate.text.extraction.version", "1"),
                    Map.entry("copro.request.agentic.paper.filter.extraction.handler.name", "TRITON"),
                    Map.entry("agentic.paper.filter.consumer.API.count", "1"),
                    Map.entry("triton.request.activator", "true"),
                    Map.entry("preprocess.agentic.paper.filter.model.name", modelName),
            Map.entry("pipeline.copro.api.process.file.format", "BASE64"),
            Map.entry(ENCRYPT_AGENTIC_FILTER_OUTPUT, "false"),
            Map.entry("page.content.min.length.threshold", "1"),
            Map.entry("write.batch.size", "5"),
            Map.entry("copro.isretry.enabled", "false")));

    AgenticPaperFilterAction dataExtractionAction = new AgenticPaperFilterAction(actionExecutionAudit, log, dataExtraction);
    dataExtractionAction.execute();
    }

}