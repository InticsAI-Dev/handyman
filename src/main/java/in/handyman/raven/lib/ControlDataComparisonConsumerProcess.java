package in.handyman.raven.lib;

import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.adapters.comparison.ComparisonAdapter;
import in.handyman.raven.lib.adapters.comparison.ComparisonAdapterFactory;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;

/**
 * Consumer process for ControlDataComparison for use with CoproProcessor.
 *
 * Responsibilities:
 * - Decrypt incoming values (item-wise if configured)
 * - Validate values using adapters
 * - Classify match results
 * - Insert processed records in batch
 * - Re-encrypt values back and update DB
 */
public class ControlDataComparisonConsumerProcess
        implements CoproProcessor.ConsumerProcess<ControlDataComparisonQueryInputTable, ControlDataComparisonResult> {

    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;
    private final String resourceConn;
    private final Jdbi jdbi;
    private final InticsIntegrity encryption;
    private final boolean itemWiseEncryption;
    private final boolean actualEncryption;

    public ControlDataComparisonConsumerProcess(Logger log,
                                                Marker aMarker,
                                                ActionExecutionAudit action,
                                                String resourceConn) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.resourceConn = resourceConn;
        this.jdbi = ResourceAccess.rdbmsJDBIConn(resourceConn);
        this.encryption = SecurityEngine.getInticsIntegrityMethod(action, log);

        this.itemWiseEncryption = "true".equalsIgnoreCase(action.getContext()
                .getOrDefault(ENCRYPT_ITEM_WISE_ENCRYPTION, "false"));
        this.actualEncryption = "true".equalsIgnoreCase(action.getContext()
                .getOrDefault("actual.encryption.variable", "false"));
    }

    /**
     * Process a batch of ControlDataComparisonQueryInputTable records.
     */
    @Override
    public List<ControlDataComparisonResult> process(URL endpoint, ControlDataComparisonQueryInputTable entity) throws Exception {
        // Wrap single entity in a list to handle batch processing
        List<ControlDataComparisonQueryInputTable> batch = Collections.singletonList(entity);

        log.info(aMarker, "Processing batch of size {}", batch.size());

        // Decrypted maps
        Map<String, String> decryptedActualMap = new HashMap<>();
        Map<String, String> decryptedExtractedMap = new HashMap<>();

        // 1) Decrypt if configured
        if (itemWiseEncryption) {
            decryptBatch(batch, decryptedActualMap, decryptedExtractedMap);
        }

        // 2) Prepare re-encryption
        List<EncryptionRequestClass> reEncryptionRequests = new ArrayList<>();
        Map<String, ControlDataComparisonQueryInputTable> recordMapForReencrypt = new HashMap<>();

        // 3) Batch insert
        String insertQuery = action.getContext().getOrDefault("insertQuery", ""); // you may pass insertQuery from context
        if (insertQuery.isEmpty()) {
            log.warn(aMarker, "Insert query is not provided in context, skipping DB insert");
            return Collections.emptyList();
        }

        jdbi.useHandle(handle -> {
            try (PreparedStatement pstmt = handle.getConnection().prepareStatement(insertQuery)) {
                for (ControlDataComparisonQueryInputTable record : batch) {
                    processRecord(record, pstmt, decryptedActualMap, decryptedExtractedMap,
                            reEncryptionRequests, recordMapForReencrypt);
                }
                pstmt.executeBatch();
            }
        });

        // 4) Re-encrypt if needed
        handleReEncryption(insertQuery, reEncryptionRequests, recordMapForReencrypt);

        return Collections.emptyList(); // return empty as default
    }

    // ----------------- Helper Methods -----------------

    private void decryptBatch(List<ControlDataComparisonQueryInputTable> batch,
                              Map<String, String> decryptedActualMap,
                              Map<String, String> decryptedExtractedMap) {

        Map<String, List<ControlDataComparisonQueryInputTable>> grouped =
                batch.stream()
                        .filter(r -> "t".equalsIgnoreCase(r.getIsEncrypted()))
                        .collect(Collectors.groupingBy(ControlDataComparisonQueryInputTable::getOriginId));

        for (Map.Entry<String, List<ControlDataComparisonQueryInputTable>> entry : grouped.entrySet()) {
            String originId = entry.getKey();
            List<ControlDataComparisonQueryInputTable> items = entry.getValue();

            // Actual decryption
            if (actualEncryption) {
                List<EncryptionRequestClass> actualReqs = items.stream()
                        .filter(r -> r.getActualValue() != null && !r.getActualValue().trim().isEmpty())
                        .map(r -> new EncryptionRequestClass(r.getEncryptionPolicy(), r.getActualValue(), r.getSorItemName()))
                        .collect(Collectors.toList());
                decryptValues(actualReqs, decryptedActualMap, originId, "ACTUAL");
            }

            // Extracted decryption
            List<EncryptionRequestClass> extractedReqs = items.stream()
                    .filter(r -> r.getExtractedValue() != null && !r.getExtractedValue().trim().isEmpty())
                    .map(r -> new EncryptionRequestClass(r.getEncryptionPolicy(), r.getExtractedValue(), r.getSorItemName()))
                    .collect(Collectors.toList());
            decryptValues(extractedReqs, decryptedExtractedMap, originId, "EXTRACTED");
        }
    }

    private void decryptValues(List<EncryptionRequestClass> requests,
                               Map<String, String> decryptedMap,
                               String originId,
                               String type) {
        if (requests.isEmpty()) return;
        try {
            List<EncryptionRequestClass> decrypted = encryption.decrypt(requests);
            decrypted.forEach(d -> decryptedMap.put(originId + "|" + d.getKey(), d.getValue()));
        } catch (Exception e) {
            log.error(aMarker, "{} decryption failed for originId {}", type, originId, e);
        }
    }

    private void processRecord(ControlDataComparisonQueryInputTable record,
                               PreparedStatement pstmt,
                               Map<String, String> decryptedActualMap,
                               Map<String, String> decryptedExtractedMap,
                               List<EncryptionRequestClass> reEncryptionRequests,
                               Map<String, ControlDataComparisonQueryInputTable> recordMapForReencrypt) throws Exception {

        String originId = record.getOriginId();
        String sorItemName = record.getSorItemName();
        String mapKey = originId + "|" + sorItemName;

        // Apply decrypted values
        if (decryptedActualMap.containsKey(mapKey)) {
            record.setActualValue(decryptedActualMap.get(mapKey));
        }
        if (decryptedExtractedMap.containsKey(mapKey)) {
            record.setExtractedValue(decryptedExtractedMap.get(mapKey));
        }

        // Validation
        String adapterKey = record.getAllowedAdapter() != null ? record.getAllowedAdapter() : "string";
        ComparisonAdapter adapter = ComparisonAdapterFactory.getAdapter(adapterKey);
        Long mismatchCount = adapter.validate(record, action, log);

        String oneTouch = action.getContext().getOrDefault("control.data.one.touch.threshold", "1");
        String lowTouch = action.getContext().getOrDefault("control.data.low.touch.threshold", "5");
        String matchStatus = ControlDataComparisonConsumerProcess.calculateValidationScores(mismatchCount, oneTouch, lowTouch);

        // Classification
        String classification = determineClassification(record.getActualValue(), record.getExtractedValue(), matchStatus);

        int idx = 1;
        pstmt.setObject(idx++, record.getRootPipelineId());
        pstmt.setObject(idx++, java.sql.Date.valueOf(java.time.LocalDate.now()));
        pstmt.setObject(idx++, record.getGroupId());
        pstmt.setObject(idx++, record.getFileName());
        pstmt.setObject(idx++, record.getOriginId());
        pstmt.setObject(idx++, record.getBatchId());
        pstmt.setObject(idx++, record.getPaperNo());
        pstmt.setObject(idx++, record.getActualValue());
        pstmt.setObject(idx++, record.getExtractedValue());
        pstmt.setObject(idx++, matchStatus);
        pstmt.setObject(idx++, mismatchCount);
        pstmt.setObject(idx++, record.getTenantId());
        pstmt.setObject(idx++, classification);
        pstmt.setObject(idx++, record.getSorContainerId());
        pstmt.setObject(idx++, record.getSorItemName());
        pstmt.setObject(idx++, record.getSorItemId());

        pstmt.addBatch();

        // Prepare re-encryption
        if (itemWiseEncryption && "t".equalsIgnoreCase(record.getIsEncrypted())) {
            if (actualEncryption && record.getActualValue() != null && !record.getActualValue().isEmpty()) {
                EncryptionRequestClass er = new EncryptionRequestClass(record.getEncryptionPolicy(), record.getActualValue(), sorItemName + "|actual");
                reEncryptionRequests.add(er);
                recordMapForReencrypt.put(er.getKey(), record);
            }
            if (record.getExtractedValue() != null && !record.getExtractedValue().isEmpty()) {
                EncryptionRequestClass er = new EncryptionRequestClass(record.getEncryptionPolicy(), record.getExtractedValue(), sorItemName + "|extracted");
                reEncryptionRequests.add(er);
                recordMapForReencrypt.put(er.getKey(), record);
            }
        }
    }
    private void handleReEncryption(String insertQuery,
                                    List<EncryptionRequestClass> reEncryptionRequests,
                                    Map<String, ControlDataComparisonQueryInputTable> recordMapForReencrypt) {
        if (reEncryptionRequests.isEmpty()) {
            log.debug(aMarker, "No re-encryption requests for this batch");
            return;
        }

        try {
            log.info(aMarker, "Starting batch re-encryption for {} items", reEncryptionRequests.size());
            List<EncryptionRequestClass> encryptedResults = encryption.encrypt(reEncryptionRequests);
            String outputTable = extractTableNameFromInsertQuery(insertQuery);

            for (EncryptionRequestClass res : encryptedResults) {
                try {
                    String[] parts = res.getKey().split("\\|");
                    String sorItemName = parts[0];
                    String valueType = parts.length > 1 ? parts[1] : "extracted";
                    ControlDataComparisonQueryInputTable record = recordMapForReencrypt.get(res.getKey());

                    if (record == null) continue;

                    String column = "actual".equalsIgnoreCase(valueType) ? "actual_value" : "extracted_value";
                    String updateSql = "UPDATE " + outputTable +
                            " SET " + column + " = :value " +
                            " WHERE origin_id = :originId AND sor_item_name = :sorItemName AND paper_no = :paperNo";

                    jdbi.useHandle(handle -> handle.createUpdate(updateSql)
                            .bind("value", res.getValue())
                            .bind("originId", record.getOriginId())
                            .bind("sorItemName", sorItemName)
                            .bind("paperNo", record.getPaperNo())
                            .execute());

                } catch (Exception e) {
                    log.error(aMarker, "Failed DB update after re-encryption for key {}", res.getKey(), e);
                }
            }
            log.info(aMarker, "Batch re-encryption completed (items processed: {})", reEncryptionRequests.size());
        } catch (Exception e) {
            log.error(aMarker, "Batch encryption/re-encryption failed", e);
        }
    }

    // ----------------- Utility methods -----------------

    private String determineClassification(String actualValue, String extractedValue, String matchStatus) {
        String a = actualValue == null ? "" : actualValue.trim();
        String e = extractedValue == null ? "" : extractedValue.trim();

        if ("NO TOUCH".equals(matchStatus) && a.isEmpty() && e.isEmpty()) return "TN";
        if ("NO TOUCH".equals(matchStatus) && !a.isEmpty() && !e.isEmpty()) return "TP";
        if (a.isEmpty() && !e.isEmpty()) return "FN";
        if (!a.isEmpty() && (e.isEmpty() || !"NO TOUCH".equals(matchStatus))) return "FP";
        return "UNKNOWN";
    }

    public static String calculateValidationScores(Long mismatchCount, String oneTouch, String lowTouch) {
        if (mismatchCount == 0) return "NO TOUCH";
        if (mismatchCount <= Long.parseLong(oneTouch)) return "ONE TOUCH";
        if (mismatchCount <= Long.parseLong(lowTouch)) return "LOW TOUCH";
        return "HIGH TOUCH";
    }

    private String extractTableNameFromInsertQuery(String insertQuery) {
        if (insertQuery == null) return "";
        String upper = insertQuery.toUpperCase(Locale.ROOT);
        int idx = upper.indexOf("INSERT INTO");
        if (idx < 0) return "";
        int start = idx + "INSERT INTO".length();
        int paren = insertQuery.indexOf("(", start);
        return (paren < 0)
                ? insertQuery.substring(start).trim().split("\\s+")[0]
                : insertQuery.substring(start, paren).trim();
    }
}
