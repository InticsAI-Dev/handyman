package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.adapters.comparison.ComparisonAdapter;
import in.handyman.raven.lib.adapters.comparison.ComparisonAdapterFactory;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.*;

/**
 * Consumer-side processing for control data comparison.
 * Runs adapter validation for each record, inserts results, and performs per-batch re-encryption (Option A).
 */
public class ControlDataComparisonConsumerProcess {

    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;

    public ControlDataComparisonConsumerProcess(ActionExecutionAudit action, Logger log, Marker aMarker) {
        this.action = action;
        this.log = log;
        this.aMarker = aMarker;
    }

    /**
     * Processes a batch (list) of records. Collects re-encryption requests and sends them in batch.
     *
     * @param inputRecords          list of input DTOs (usually size = 1 when CoproProcessor passes single entity)
     * @param decryptedActualMap    pre-decrypted actual values map (originId|sorItem -> value)
     * @param decryptedExtractedMap pre-decrypted extracted values map
     * @param jdbi                  Jdbi instance to insert/update DB
     * @param outputTable           output table name
     * @param kafkaComparison       kafka flag/context
     * @param encryption            InticsIntegrity encryption interface
     */
    public void process(List<ControlDataComparisonQueryInputTable> inputRecords,
                        Map<String, String> decryptedActualMap,
                        Map<String, String> decryptedExtractedMap,
                        Jdbi jdbi,
                        String outputTable,
                        String kafkaComparison,
                        InticsIntegrity encryption) throws JsonProcessingException {

        List<EncryptionRequestClass> encryptionRequests = new ArrayList<>();
        Map<String, ControlDataComparisonQueryInputTable> recordMap = new HashMap<>();

        boolean itemWiseEncryption = "true".equalsIgnoreCase(action.getContext().getOrDefault("encrypt.item.wise", "false"));
        boolean actualEncryption = "true".equalsIgnoreCase(action.getContext().getOrDefault("actual.encryption.variable", "false"));

        for (ControlDataComparisonQueryInputTable record : inputRecords) {
            try {
                // Apply decrypted values if present
                String originId = record.getOriginId();
                String sorItemName = record.getSorItemName();
                String mapKey = originId + "|" + sorItemName;

                if (decryptedActualMap != null && decryptedActualMap.containsKey(mapKey)) {
                    record.setActualValue(decryptedActualMap.get(mapKey));
                }
                if (decryptedExtractedMap != null && decryptedExtractedMap.containsKey(mapKey)) {
                    record.setExtractedValue(decryptedExtractedMap.get(mapKey));
                }

                // Adapter validation
                String adapterKey = record.getAllowedAdapter() != null ? record.getAllowedAdapter() : "string";
                ComparisonAdapter adapter = ComparisonAdapterFactory.getAdapter(adapterKey);
                Long mismatchCount = adapter.validate(record, action, log);

                // Determine match status
                String matchStatus = ControlDataComparisonAction.calculateValidationScores(
                        mismatchCount,
                        action.getContext().getOrDefault("control.data.one.touch.threshold", "1"),
                        action.getContext().getOrDefault("control.data.low.touch.threshold", "5")
                );

                // Insert result into output table
                ControlDataComparisonAction.insertExecutionInfoStatic(jdbi, outputTable, matchStatus, mismatchCount, record);

                // Prepare re-encryption requests if item-wise encryption enabled AND record.isEncrypted = 't'
                if (itemWiseEncryption && "t".equalsIgnoreCase(record.getIsEncrypted())) {
                    String extractedValue = record.getExtractedValue();
                    String actualValue = record.getActualValue();

                    if (actualEncryption) {
                        if (actualValue != null && !actualValue.trim().isEmpty()) {
                            EncryptionRequestClass er = new EncryptionRequestClass(record.getEncryptionPolicy(), actualValue, sorItemName + "|actual");
                            encryptionRequests.add(er);
                            recordMap.put(er.getKey(), record);
                        }
                    }

                    if (extractedValue != null && !extractedValue.trim().isEmpty()) {
                        EncryptionRequestClass er = new EncryptionRequestClass(record.getEncryptionPolicy(), extractedValue, sorItemName + "|extracted");
                        encryptionRequests.add(er);
                        recordMap.put(er.getKey(), record);
                    }
                }

            } catch (Exception e) {
                log.error(aMarker, "Error processing record for originId {} and sorItemName {}",
                        record.getOriginId(), record.getSorItemName(), e);
            }
        }

        // Perform batch encryption/re-encryption for this batch (Option A: per-consumer batch)
        if (!encryptionRequests.isEmpty()) {
            try {
                log.info(aMarker, "Starting batch encryption/re-encryption for {} items", encryptionRequests.size());
                List<EncryptionRequestClass> encryptedResults = encryption.encrypt(encryptionRequests);
                encryptedResults.forEach(result -> {
                    try {
                        String[] keyParts = result.getKey().split("\\|");
                        String sorItemName = keyParts[0];
                        String valueType = keyParts.length > 1 ? keyParts[1] : "extracted";
                        ControlDataComparisonQueryInputTable rec = recordMap.get(result.getKey());
                        if (rec != null) {
                            String originId = rec.getOriginId();
                            String paperNo = String.valueOf(rec.getPaperNo());
                            log.info(aMarker, "Re-encryption completed for sorItemName: {}, type: {}, originId: {}",
                                    sorItemName, valueType, originId);
                            // Update DB with re-encrypted value
                            String column = "actual".equals(valueType) ? "actual_value" : "extracted_value";
                            jdbi.useHandle(handle -> handle.createUpdate("UPDATE " + outputTable + " SET " + column + " = :value " +
                                            "WHERE origin_id = :originId AND sor_item_name = :sorItemName AND paper_no = :paperNo")
                                    .bind("value", result.getValue())
                                    .bind("originId", originId)
                                    .bind("sorItemName", sorItemName)
                                    .bind("paperNo", paperNo)
                                    .execute());
                        }
                    } catch (Exception innerEx) {
                        log.error(aMarker, "Error updating DB after re-encryption for key {}", result.getKey(), innerEx);
                    }
                });
                log.info(aMarker, "Batch encryption/re-encryption successful for {} items", encryptionRequests.size());
            } catch (Exception e) {
                log.error(aMarker, "Batch encryption/re-encryption failed", e);
            }
        }
    }
}
