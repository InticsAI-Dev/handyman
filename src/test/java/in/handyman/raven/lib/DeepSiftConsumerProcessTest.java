package in.handyman.raven.lib;

import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.deep.sift.DeepSiftConsumerProcess;
import in.handyman.raven.lib.model.deep.sift.DeepSiftInputTable;
import in.handyman.raven.lib.model.deep.sift.DeepSiftOutputTable;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepSiftConsumerProcessTest {

    private static final Logger log = LoggerFactory.getLogger(DeepSiftConsumerProcessTest.class);

    @Test
    void testTritonServerExecutionWithDirectFile() throws Exception {
        final String filePath = "/home/dineshkumar.anandan@zucisystems.com/Downloads/Elevance-Health/Medical/Issue Samples/auth_id/COMM__P1_IP ER RCAP20250605001838_1.jpg";
        final URL endpoint = new URL("http://localhost:9001/xenon-textract");

        File inputFile = new File(filePath);
        assertTrue(inputFile.exists(), "Input file does not exist: " + filePath);
        assertTrue(inputFile.canRead(), "Input file is not readable: " + filePath);

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setProcessId(5443L);
        actionExecutionAudit.setActionId(21352L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("deep.sift.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("copro.request.deep.sift.handler.name", "TRITON"),
                Map.entry("pipeline.deep.sift.encryption", "true"),
                Map.entry("actionId", "21352"),
                Map.entry("write.batch.size", "5"),
                Map.entry("deep.sift.page.content.min.length.threshold", "1"),
                Map.entry("copro.isretry.enabled", "false"),
                Map.entry("deep.sift.extraction.activator", "true"),
                Map.entry("deep.sift.route.tess4j", "false"),
                Map.entry("deep.sift.bbox.extraction.activator", "true")
        ));

        DeepSiftInputTable entity = DeepSiftInputTable.builder()
                .originId("ORIGIN-1160")
                .groupId(579)
                .inputFilePath(filePath)
                .createdOn(new Timestamp(System.currentTimeMillis()))
                .createdBy("test-user")
                .rootPipelineId(5443L)
                .tenantId(1L)
                .batchId("BATCH-579_1")
                .paperNo(1)
                .sourceDocumentType("MEDICAL")
                .modelId(1)
                .modelName("XENON")
                .build();

        Marker marker = MarkerFactory.getMarker("DEEP_SIFT_TEST");
        FileProcessingUtils fileProcessingUtils = new FileProcessingUtils(log, marker, actionExecutionAudit);

        DeepSiftConsumerProcess consumerProcess = new DeepSiftConsumerProcess(
                log,
                marker,
                actionExecutionAudit,
                fileProcessingUtils,
                "BASE64"
        );

        List<DeepSiftOutputTable> results = assertDoesNotThrow(
                () -> consumerProcess.process(endpoint, entity),
                "Consumer process should not throw"
        );

        DeepSiftOutputTable output = results.getFirst();
        log.info("Test completed | originId={} paperNo={} rootPipelineId={} status={} "
                        + "extractedTextLen={} bboxJsonLen={}",
                output.getOriginId(),
                output.getPaperNo(),
                output.getRootPipelineId(),
                output.getStatus(),
                output.getExtractedText() != null ? output.getExtractedText().length() : 0,
                output.getExtractedTextWithBbox() != null ? output.getExtractedTextWithBbox().length() : 0);

        assertEquals("COMPLETED", output.getStatus(), "Status should be COMPLETED");
        assertNotNull(output.getExtractedText(), "Extracted text should not be null");
        assertNotNull(output.getExtractedTextWithBbox(),
                "Bbox JSON should be populated when deep.sift.bbox.extraction.activator=true");
    }

    @Test
    void testTritonServerExecutionWithDirectFileTess4j() throws Exception {
        final String filePath = "/home/dineshkumar.anandan@zucisystems.com/Downloads/Elevance-Health/Medical/Issue Samples/auth_id/COMM__P1_IP ER RCAP20250605001838_1.jpg";
        final URL endpoint = new URL("http://localhost:9001/xenon-textract");

        File inputFile = new File(filePath);
        assertTrue(inputFile.exists(), "Input file does not exist: " + filePath);
        assertTrue(inputFile.canRead(), "Input file is not readable: " + filePath);

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setProcessId(5443L);
        actionExecutionAudit.setActionId(21352L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("deep.sift.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("copro.request.deep.sift.handler.name", "TRITON"),
                Map.entry("pipeline.deep.sift.encryption", "true"),
                Map.entry("actionId", "21352"),
                Map.entry("write.batch.size", "5"),
                Map.entry("deep.sift.page.content.min.length.threshold", "1"),
                Map.entry("copro.isretry.enabled", "false"),
                Map.entry("deep.sift.extraction.activator", "true"),
                Map.entry("deep.sift.route.tess4j", "true"),
                Map.entry("deep.sift.bbox.extraction.activator", "true")
        ));

        DeepSiftInputTable entity = DeepSiftInputTable.builder()
                .originId("ORIGIN-1160")
                .groupId(579)
                .inputFilePath(filePath)
                .createdOn(new Timestamp(System.currentTimeMillis()))
                .createdBy("test-user")
                .rootPipelineId(5443L)
                .tenantId(1L)
                .batchId("BATCH-579_1")
                .paperNo(1)
                .sourceDocumentType("MEDICAL")
                .modelId(1)
                .modelName("XENON")
                .build();

        Marker marker = MarkerFactory.getMarker("DEEP_SIFT_TEST");
        FileProcessingUtils fileProcessingUtils = new FileProcessingUtils(log, marker, actionExecutionAudit);

        DeepSiftConsumerProcess consumerProcess = new DeepSiftConsumerProcess(
                log,
                marker,
                actionExecutionAudit,
                fileProcessingUtils,
                "BASE64"
        );

        List<DeepSiftOutputTable> results = assertDoesNotThrow(
                () -> consumerProcess.process(endpoint, entity),
                "Consumer process should not throw"
        );

        DeepSiftOutputTable output = results.getFirst();
        log.info("Test completed | originId={} paperNo={} rootPipelineId={} status={} "
                        + "extractedTextLen={} bboxJsonLen={}",
                output.getOriginId(),
                output.getPaperNo(),
                output.getRootPipelineId(),
                output.getStatus(),
                output.getExtractedText() != null ? output.getExtractedText().length() : 0,
                output.getExtractedTextWithBbox() != null ? output.getExtractedTextWithBbox().length() : 0);

        assertEquals("COMPLETED", output.getStatus(), "Status should be COMPLETED");
        assertNotNull(output.getExtractedText(), "Extracted text should not be null");
        assertNotNull(output.getExtractedTextWithBbox(),
                "Bbox JSON should be populated when deep.sift.bbox.extraction.activator=true");
    }
}