package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AumiOutboundResponse;
import in.handyman.raven.lib.model.OutboundResponse;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class OutboundResponseActionTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Jdbi jdbi;

    @BeforeEach
    void setUp() {
        // Initialize JDBI connection
        jdbi = ResourceAccess.rdbmsJDBIConn("intics_zio_db_conn");
        // Clear the table before each test
        jdbi.useHandle(handle -> {
            handle.execute("DELETE FROM product_outbound.product_response_details");
            log.info("Cleared product_outbound.product_response_details table");
        });
    }

        @Test
    void testingOutboundResponse() throws Exception {
        OutboundResponse outboundResponse= OutboundResponse.builder()
                .name("outbound")
                .resourceConn("intics_zio_db_conn")
                .productResponseDetails("product_outbound.product_response_details")
                .querySet("SELECT \n" +
                        "    -- Fields from the first query (Valuation Prediction)\n" +
                        "    vp.predicted_value,\n" +
                        "    rkoa.paper_no,\n" +
                        "    si.sor_item_name AS item_name,\n" +
                        "    vp.precision,\n" +
                        "    COALESCE(vp.left_pos, 0) AS x,\n" +
                        "    COALESCE(vp.right_pos, 0) - COALESCE(vp.left_pos, 0) AS width,\n" +
                        "    COALESCE(vp.lower_pos, 0) AS y,\n" +
                        "    COALESCE(vp.upper_pos, 0) - COALESCE(vp.lower_pos, 0) AS height,\n" +
                        " \n" +
                        "    -- Fields from the second query (Full Meta Query)\n" +
                        "    a.file_extension, \n" +
                        "    a.transaction_id, \n" +
                        "    a.file_name, \n" +
                        "    t.created_on AS transaction_created_on, \n" +
                        "    t.last_updated_on, \n" +
                        "    soo.created_on AS source_created_on,\n" +
                        "    apsa.id AS apsa_id,\n" +
                        "    apsa.origin_id AS apsa_origin_id,\n" +
                        "    apsa.paper_no AS apsa_paper_no,\n" +
                        "    apsa.root_pipeline_id AS apsa_root_pipeline_id,\n" +
                        "    apsa.batch_id,\n" +
                        "    apsa.tenant_id AS apsa_tenant_id,\n" +
                        "    apsa.group_id,\n" +
                        "    apsa.is_candidate_paper AS apsa_is_candidate_paper,\n" +
                        "    apsa.paper_level_score,\n" +
                        "    plsa.id AS plsa_id,\n" +
                        "    plsa.origin_id AS plsa_origin_id,\n" +
                        "    plsa.paper_no AS plsa_paper_no,\n" +
                        "    plsa.entity_score,\n" +
                        "    plsa.max_doc_score,\n" +
                        "    plsa.max_distance,\n" +
                        "    plsa.is_candidate_paper AS plsa_is_candidate_paper,\n" +
                        "    plsa.root_pipeline_id AS plsa_root_pipeline_id,\n" +
                        "    plsa.tenant_id AS plsa_tenant_id,\n" +
                        " \n" +
                        "    -- Fields from the third query (Ingestion File Details)\n" +
                        "    ifd.request_txn_id, \n" +
                        "    ifd.upload_req_ack, \n" +
                        "    ifd.document_id,\n" +
                        "    ifd.document_type -- Corrected from idfd.document_type to ifd.document_type\n" +
                        "FROM \n" +
                        "    info.source_of_origin soo \n" +
                        "LEFT JOIN \n" +
                        "    info.asset a \n" +
                        "    ON soo.asset_id = a.asset_id \n" +
                        "LEFT JOIN \n" +
                        "    info.\"transaction\" t \n" +
                        "    ON t.transaction_id = a.transaction_id \n" +
                        "LEFT JOIN \n" +
                        "    paper_filter.agentic_paper_level_score_audit apsa \n" +
                        "    ON apsa.origin_id = soo.origin_id \n" +
                        "    AND apsa.tenant_id = soo.tenant_id \n" +
                        "    AND apsa.root_pipeline_id = soo.root_pipeline_id \n" +
                        "LEFT JOIN \n" +
                        "    paper.paper_level_score_audit plsa \n" +
                        "    ON plsa.origin_id = soo.origin_id \n" +
                        "    AND plsa.tenant_id = soo.tenant_id \n" +
                        "    AND plsa.root_pipeline_id = soo.root_pipeline_id \n" +
                        "    AND plsa.paper_no = apsa.paper_no\n" +
                        "LEFT JOIN \n" +
                        "    valuation.prediction vp \n" +
                        "    ON vp.origin_id = soo.origin_id \n" +
                        "    AND vp.tenant_id = soo.tenant_id \n" +
                        "    AND vp.root_pipeline_id = soo.root_pipeline_id\n" +
                        "LEFT JOIN \n" +
                        "    sor_transaction.radon_kvp_output_audit rkoa \n" +
                        "    ON vp.origin_id = rkoa.origin_id \n" +
                        "    AND rkoa.paper_no = apsa.paper_no\n" +
                        "LEFT JOIN \n" +
                        "    sor_meta.sor_tsynonym st \n" +
                        "    ON vp.synonym_id = st.synonym_id\n" +
                        "LEFT JOIN \n" +
                        "    sor_meta.sor_item si \n" +
                        "    ON st.sor_item_id = si.sor_item_id\n" +
                        "LEFT JOIN (\n" +
                        "    SELECT \n" +
                        "        ifd.request_txn_id, \n" +
                        "        ifd.upload_req_ack, \n" +
                        "        ifd.document_id,\n" +
                        "        idfd.transaction_id,\n" +
                        "        idfd.document_type, -- Include document_type in the derived table\n" +
                        "        ROW_NUMBER() OVER (PARTITION BY idfd.transaction_id ORDER BY idfd.download_completed_on DESC) AS rn\n" +
                        "    FROM \n" +
                        "        inbound_config.ingestion_file_details ifd\n" +
                        "    JOIN \n" +
                        "        inbound_config.ingestion_downloaded_file_details idfd \n" +
                        "        ON ifd.inbound_transaction_id = idfd.inbound_transaction_id\n" +
                        "    WHERE \n" +
                        "        idfd.transaction_id = 'TRZ-6'\n" +
                        "        AND idfd.file_name = 'allkeywords'\n" +
                        ") ifd \n" +
                        "    ON a.transaction_id = ifd.transaction_id \n" +
                        "    AND ifd.rn = 1\n" +
                        "WHERE \n" +
                        "    soo.tenant_id = 1\n" +
                        "    AND soo.origin_id = 'ORIGIN-1'\n" +
                        "    AND soo.root_pipeline_id = 1;")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.setRootPipelineId(1L);
        action.getContext().put("tenant_id","1");

        OutboundResponseAction outboundResponseAction=new OutboundResponseAction(action ,log,outboundResponse);
        outboundResponseAction.execute();
    }

    @Test
    void testConsumerBatchWithHardcodedJson() throws Exception {
        // Hardcoded JSON input
        String hardcodedJson = "{\n" +
                "  \"requestTxnId\": \"N/A\",\n" +
                "  \"status\": \"SUCCESS\",\n" +
                "  \"errorMessage\": \"\",\n" +
                "  \"errorMessageDetail\": \"\",\n" +
                "  \"errorCd\": null,\n" +
                "  \"documentId\": \"1\",\n" +
                "  \"metadata\": {\n" +
                "    \"documentType\": \"UMFAX\",\n" +
                "    \"documentExtension\": \"pdf\",\n" +
                "    \"transactionId\": \"1\",\n" +
                "    \"inboundDocumentName\": \"allkeywords\",\n" +
                "    \"processStartTime\": null,\n" +
                "    \"processEndTime\": \"2025-07-31T01:19:28.843779699\",\n" +
                "    \"processingTimeMs\": 0,\n" +
                "    \"processedAt\": \"2025-07-12T01:32:01.435595\",\n" +
                "    \"pageCount\": 3,\n" +
                "    \"candidatePaper\": [1, 7, 2],\n" +
                "    \"overallConfidence\": 0\n" +
                "  },\n" +
                "  \"aumipayload\": {\n" +
                "    \"authId\": {\"value\": \"UM54883491\", \"page\": 0, \"confidence\": 50, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"hcid\": {\"value\": \"306A06703\", \"page\": 0, \"confidence\": 100, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"medicaidId\": {\"value\": \"345678235667\", \"page\": 0, \"confidence\": 90, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"memberLastName\": {\"value\": \"Joanne Roytlauf\", \"page\": 0, \"confidence\": 100, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"memberFirstName\": {\"value\": \"\", \"page\": 0, \"confidence\": 50, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"memberDOB\": {\"value\": \"1927-08-23\", \"page\": 0, \"confidence\": 100, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"groupId\": {\"value\": \"\", \"page\": 0, \"confidence\": 50, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"memberGender\": {\"value\": \"\", \"page\": 0, \"confidence\": 50, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"memberAddressLine1\": {\"value\": \"46y Sutherland Center,Plano,TX,75074\", \"page\": 0, \"confidence\": 90, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"memberCity\": {\"value\": \"Plano\", \"page\": 0, \"confidence\": 100, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"memberZipCode\": {\"value\": \"75074\", \"page\": 0, \"confidence\": 100, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"memberState\": {\"value\": \"TX\", \"page\": 0, \"confidence\": 100, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"levelOfService\": {\"value\": \"Urgent\", \"page\": 0, \"confidence\": 100, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"serviceFromDate\": {\"value\": \"2025-02-19\", \"page\": 0, \"confidence\": 100, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"serviceToDate\": {\"value\": \"\", \"page\": 0, \"confidence\": 50, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"authAdmitDate\": {\"value\": \"\", \"page\": 0, \"confidence\": 50, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"authDischargeDate\": {\"value\": \"\", \"page\": 0, \"confidence\": 50, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"authDischargeDisposition\": {\"value\": \"\", \"page\": 0, \"confidence\": 50, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "    \"providers\": null,\n" +
                "    \"serviceModifiers\": [{\n" +
                "      \"cd\": {\"value\": \"G2214HH\", \"page\": 0, \"confidence\": 50, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "      \"desc\": {\"value\": \"\", \"page\": 0, \"confidence\": 50, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}},\n" +
                "      \"lineNumber\": {\"value\": \"\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}\n" +
                "    }],\n" +
                "    \"diagnosis\": null,\n" +
                "    \"additionalProperties\": [{\n" +
                "      \"propName\": \"AUTH_KEYWORD\",\n" +
                "      \"propValue\": \"Inpatient psych\",\n" +
                "      \"page\": 0,\n" +
                "      \"confidence\": 50,\n" +
                "      \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}\n" +
                "    }, {\n" +
                "      \"propName\": \"AUTH_KEYWORD\",\n" +
                "      \"propValue\": \"Substance use RTC\",\n" +
                "      \"page\": 0,\n" +
                "      \"confidence\": 50,\n" +
                "      \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}\n" +
                "    }, {\n" +
                "      \"propName\": \"AUTH_ADDL_KEYWORD\",\n" +
                "      \"propValue\": \"URGENT\",\n" +
                "      \"page\": 0,\n" +
                "      \"confidence\": 50,\n" +
                "      \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}\n" +
                "    }],\n" +
                "    \"authorizationIndicators\": null\n" +
                "  },\n" +
                "  \"customResponse\": null\n" +
                "}";

        // Parse JSON into AumiOutboundResponse
        AumiOutboundResponse response;
        try {
            response = objectMapper.readValue(hardcodedJson, AumiOutboundResponse.class);
            response.setCustomResponse(hardcodedJson);
            log.debug("Parsed AumiOutboundResponse: {}", response);
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", hardcodedJson, e);
            throw e;
        }

        // Setup action and context
        ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.setRootPipelineId(1L);
        action.getContext().put("tenant_id", "1");

        // Create OutboundResponse configuration
        OutboundResponse outboundResponse = OutboundResponse.builder()
                .name("outbound")
                .resourceConn("intics_zio_db_con")
                .productResponseDetails("product_outbound.product_response_details")
                .querySet("")
                .condition(true)
                .build();

        // Initialize OutboundResponseAction
        OutboundResponseAction actionInstance = new OutboundResponseAction(action, log, outboundResponse);

        // Execute consumerBatch and query in a single handle to avoid transaction conflicts
        List<Map<String, Object>> insertedRecords = jdbi.withHandle(handle -> {
            handle.getConnection().setAutoCommit(false); // Disable autoCommit
            try {
                actionInstance.consumerBatch(jdbi, List.of(response));
                return handle.createQuery("SELECT * FROM product_outbound.product_response_details WHERE document_id = :documentId")
                        .bind("documentId", "1")
                        .mapToMap()
                        .list();
            } finally {
                handle.getConnection().setAutoCommit(true); // Reset autoCommit
            }
        });

        // Debug: Log all records in the table
        List<Map<String, Object>> allRecords = jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM product_outbound.product_response_details")
                        .mapToMap()
                        .list()
        );
        log.info("All records in table: {}", allRecords);

        // Assertions
        assertNotNull(insertedRecords, "Inserted records should not be null");
        assertEquals(1, insertedRecords.size(), "Exactly one record should be inserted");
        Map<String, Object> record = insertedRecords.get(0);

        assertEquals("SUCCESS", record.get("status"), "status should match");
        assertEquals("1", record.get("document_id"), "document_id should match");
        assertEquals("pdf", record.get("extension"), "extension should match");
        assertEquals("UM54883491", record.get("origin_id"), "origin_id should match");
        assertEquals("1", record.get("transaction_id"), "transaction_id should match");
        assertNotNull(record.get("batch_id"), "batch_id should not be null");
        assertEquals(hardcodedJson, record.get("custom_response").toString(), "custom_response should match hardcoded JSON");
        assertEquals(1L, ((Number) record.get("tenant_id")).longValue(), "tenant_id should match");
        assertNotNull(record.get("created_on"), "created_on should not be null");
        assertEquals(1, record.get("version"), "version should match");

        log.info("Inserted record: {}", record);
    }
}
