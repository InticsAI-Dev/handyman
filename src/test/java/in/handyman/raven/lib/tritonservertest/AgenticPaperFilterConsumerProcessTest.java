package in.handyman.raven.lib.tritonservertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.AgenticPaperFilterAction;
import in.handyman.raven.lib.model.AgenticPaperFilter;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpExtractionResponse;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpOutput;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_AGENTIC_FILTER_OUTPUT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
public class AgenticPaperFilterConsumerProcessTest {

    @Test
    void tritonKryptonSuccess() throws Exception {

    }
//
//    @Test
//    void tritonKryptonFailure() throws Exception {
//        CoproRetryServiceAsync coproRetryService = mock(CoproRetryServiceAsync.class);
//        // ✅ Stub behavior to return completed future
//        CoproRetryServiceAsync.CoproResponse mockResponse = new CoproRetryServiceAsync.CoproResponse(400, "Success", "Key:Value".getBytes());
//        when(coproRetryService.callCoproApiWithRetryAsync(any(), any(), any(), any()))
//                .thenReturn(CompletableFuture.completedFuture(mockResponse));
//
//        AgenticPaperFilter dataExtraction = AgenticPaperFilter.builder()
//                .name("data extraction after copro optimization")
//                .resourceConn("intics_zio_db_conn")
//                .condition(true)
//                .endPoint("https://agentic.intics.ai/krypton-x/v2/models/krypton-x-service/versions/1/infer")
//                .processId("138980184199100180")
//                .resultTable("paper_filter.agentic_paper_filter_output")
//                .querySet("SELECT                       a.process_id, \n" +
//                        "                        a.tenant_id, \n" +
//                        "                        a.template_id, \n" +
//                        "                        a.group_id, \n" +
//                        "                        a.origin_id, \n" +
//                        "                        a.paper_no, \n" +
//                        "                        a.processed_file_path AS file_path,\n" +
//                        "                        b.root_pipeline_id,\n" +
//                        "                        c.template_name, \n" +
//                        "                        b.batch_id, \n" +
//                        "                        NOW() AS created_on, \n" +
//                        "                        r.base_prompt AS user_prompt, \n" +
//                        "                        r.system_prompt AS system_prompt\n" +
//                        "                        FROM info.auto_rotation a\n" +
//                        "                        LEFT JOIN info.template_detection_result c \n" +
//                        "                        ON c.origin_id = a.origin_id AND a.tenant_id = c.tenant_id\n" +
//                        "                        LEFT JOIN sor_meta.radon_prompt_table r \n" +
//                        "                        ON r.tenant_id = a.tenant_id\n" +
//                        "                        JOIN preprocess.preprocess_payload_queue_archive  b \n" +
//                        "                        ON a.origin_id = b.origin_id AND c.tenant_id = b.tenant_id\n" +
//                        "                        WHERE \n" +
//                        "                        a.status = 'COMPLETED' \n" +
//                        "                        AND b.status = 'COMPLETED' \n" +
//                        "                        and b.origin_id = 'ORIGIN-1';")
//                .build();
//        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
//        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://192.168.10.245:8300/v2/models/text-extractor-service/versions/1/infer");
//        actionExecutionAudit.setProcessId(138980079308730208L);
//        actionExecutionAudit.setActionId(1L);
//        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
//                Map.entry("okhttp.client.timeout", "20"),
//                Map.entry("replicate.request.api.token", "API_TOKEN"),
//                Map.entry("replicate.text.extraction.version", "1"),
//                Map.entry("copro.request.agentic.paper.filter.extraction.handler.name", "TRITON"),
//                Map.entry("agentic.paper.filter.consumer.API.count", "1"),
//                Map.entry("triton.request.activator", "true"),
//                Map.entry("preprocess.agentic.paper.filter.model.name", "KRYPTON"),
//                Map.entry("pipeline.copro.api.process.file.format", "FILE"),
//                Map.entry("agentic.paper.filter.activator", "true"),
//                Map.entry(ENCRYPT_AGENTIC_FILTER_OUTPUT, "false"),
//                Map.entry("page.content.min.length.threshold", "1"),
//                Map.entry("write.batch.size", "5")));
//        AgenticPaperFilterAction dataExtractionAction = new AgenticPaperFilterAction(actionExecutionAudit, log, dataExtraction, coproRetryService);
//        dataExtractionAction.execute();
//    }
}