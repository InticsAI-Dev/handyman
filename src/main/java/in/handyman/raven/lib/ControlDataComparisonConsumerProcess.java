package in.handyman.raven.lib;

import com.azure.json.implementation.jackson.core.JsonProcessingException;
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
    List<ControlDataComparisonQueryInputTable> originalRecords;
    Map<String, String> decryptedExtractedMap;
    Map<String, String> decryptedActualMap;
    String outputTable;

    public ControlDataComparisonConsumerProcess(Logger log,
                                                Marker aMarker,
                                                ActionExecutionAudit action,
                                                String resourceConn,
                                                List<ControlDataComparisonQueryInputTable> originalRecords,
                                                Map<String, String> decryptedActualMap,
                                                Map<String, String> decryptedExtractedMap,
                                                String outputTable) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.resourceConn = resourceConn;
        this.jdbi = ResourceAccess.rdbmsJDBIConn(resourceConn);
        this.encryption = SecurityEngine.getInticsIntegrityMethod(action, log);
        this.originalRecords = originalRecords;
        this.decryptedActualMap = decryptedActualMap;
        this.decryptedExtractedMap = decryptedExtractedMap;
        this.outputTable = outputTable;


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
            List<EncryptionRequestClass> encryptionRequests = new ArrayList<>();
            Map<String, ControlDataComparisonQueryInputTable> recordMap = new HashMap<>();
            boolean itemWiseEncryption = "true".equalsIgnoreCase(action.getContext().getOrDefault(ENCRYPT_ITEM_WISE_ENCRYPTION, "false"));
            boolean actualEncryption = "true".equalsIgnoreCase(action.getContext().getOrDefault("actual.encryption.variable", "false"));

            for (ControlDataComparisonQueryInputTable record : originalRecords) {
                String originId = record.getOriginId();
                String sorItemName = record.getSorItemName();
                String key = originId + "|" + sorItemName;

                // Apply decrypted values if available
                if (decryptedActualMap.containsKey(key)) {
                    record.setActualValue(decryptedActualMap.get(key));
                }
                if (decryptedExtractedMap.containsKey(key)) {
                    record.setExtractedValue(decryptedExtractedMap.get(key));
                }

                String extractedValue = record.getExtractedValue();
                String actualValue = record.getActualValue();
                String isEncrypted = record.getIsEncrypted();


                // Prepare for re-encryption based on conditions
                if (itemWiseEncryption && "t".equalsIgnoreCase(isEncrypted)) {
                    if (actualEncryption) {
                        // Re-encrypt both actual and extracted values
                        if (actualValue != null && !actualValue.trim().isEmpty()) {
                            encryptionRequests.add(new EncryptionRequestClass(record.getEncryptionPolicy(), actualValue, sorItemName + "|actual"));
                            recordMap.put(sorItemName + "|actual", record);
                            log.info(aMarker, "Preparing re-encryption for actualValue for sorItemName: {}, originId: {}", sorItemName, originId);
                        } else {
                            log.info(aMarker, "Skipping re-encryption for actualValue (null or empty) for sorItemName: {}, originId: {} when isEncrypted is true", sorItemName, originId);
                        }
                        if (extractedValue != null && !extractedValue.trim().isEmpty()) {
                            encryptionRequests.add(new EncryptionRequestClass(record.getEncryptionPolicy(), extractedValue, sorItemName + "|extracted"));
                            recordMap.put(sorItemName + "|extracted", record);
                            log.info(aMarker, "Preparing re-encryption for extractedValue for sorItemName: {}, originId: {}", sorItemName, originId);
                        } else {
                            log.info(aMarker, "Skipping re-encryption for extractedValue (null or empty) for sorItemName: {}, originId: {} when isEncrypted is true", sorItemName, originId);
                        }
                    } else {
                        // Re-encrypt only extracted values
                        if (extractedValue != null && !extractedValue.trim().isEmpty()) {
                            encryptionRequests.add(new EncryptionRequestClass(record.getEncryptionPolicy(), extractedValue, sorItemName + "|extracted"));
                            recordMap.put(sorItemName + "|extracted", record);
                            log.info(aMarker, "Preparing re-encryption for extractedValue for sorItemName: {}, originId: {}", sorItemName, originId);
                        } else {
                            log.info(aMarker, "Skipping re-encryption for extractedValue (null or empty) for sorItemName: {}, originId: {} when isEncrypted is true", sorItemName, originId);
                        }
                    }
                }
            }

            // Perform batch encryption/re-encryption
            if (!encryptionRequests.isEmpty()) {
                try {
                    log.info(aMarker, "Starting batch encryption/re-encryption for {} items", encryptionRequests.size());
                    List<EncryptionRequestClass> encryptedResults = encryption.encrypt(encryptionRequests);
                    encryptedResults.forEach(result -> {
                        String[] keyParts = result.getKey().split("\\|");
                        String sorItemName = keyParts[0];
                        String valueType = keyParts[1]; // "extracted" or "actual"
                        ControlDataComparisonQueryInputTable record = recordMap.get(result.getKey());
                        if (record != null) {
                            String originId = record.getOriginId();
                            String paperNo = String.valueOf(record.getPaperNo());
                            log.info(aMarker, "Re-encryption completed for sorItemName: {}, type: {}, originId: {}",
                                    sorItemName, valueType, originId);
                            // Update the database with re-encrypted values
                            String column = "actual".equals(valueType) ? "actual_value" : "extracted_value";
                            jdbi.useHandle(handle -> handle.createUpdate("UPDATE " + outputTable + " SET " + column + " = :value " +
                                            "WHERE origin_id = :originId AND sor_item_name = :sorItemName AND paper_no = :paperNo")
                                    .bind("value", result.getValue())
                                    .bind("originId", originId)
                                    .bind("sorItemName", sorItemName)
                                    .bind("paperNo", paperNo)
                                    .execute());
                        }
                    });
                    log.info(aMarker, "Batch encryption/re-encryption successful for {} items", encryptedResults.size());
                } catch (Exception e) {
                    log.error(aMarker, "Batch encryption/re-encryption failed", e);
                }
            }
            return null;
    }

    private String doControlDataValidationByAdapters(
            ControlDataComparisonQueryInputTable comparisonInputLineItem,
            Jdbi jdbi,
            String outputTable) {
        String lowTouch = action.getContext().get("control.data.low.touch.threshold");
        String oneTouch = action.getContext().get("control.data.one.touch.threshold");

        String adapterKey = comparisonInputLineItem.getAllowedAdapter() != null ? comparisonInputLineItem.getAllowedAdapter() : "string";
        ComparisonAdapter adapter = ComparisonAdapterFactory.getAdapter(adapterKey);

        Long mismatchCount = adapter.validate(comparisonInputLineItem, action, log);

        String matchStatus = calculateValidationScores(mismatchCount,oneTouch,lowTouch);

        log.info("Inserting {} type data validation at {}:", adapterKey, outputTable);
        insertExecutionInfo(
                jdbi,
                outputTable,
                matchStatus,
                mismatchCount,
                comparisonInputLineItem
        );
        return comparisonInputLineItem.getExtractedValue();
    }

    private void insertExecutionInfo(Jdbi jdbi, String outputTable, String matchStatus,
                                     Long mismatchCount,ControlDataComparisonQueryInputTable controlDataInputLineItem) {
        String classification = determineClassification(controlDataInputLineItem.getActualValue(), controlDataInputLineItem.getExtractedValue(), matchStatus);
        jdbi.useHandle(handle -> handle.createUpdate("INSERT INTO " + outputTable + " (" + "root_pipeline_id, created_on, group_id, file_name, origin_id, batch_id, " + "paper_no, actual_value, extracted_value, match_status, mismatch_count, " + "tenant_id, classification, sor_container_id, sor_item_name, sor_item_id" + ") VALUES (" + ":rootPipelineId, :createdOn, :groupId, :fileName, :originId, :batchId, :paperNo, " + ":actualValue, :extractedValue, :matchStatus, :mismatchCount, :tenantId, " + ":classification, :sorContainerId, :sorItemName, :sorItemId" + ");").bind("rootPipelineId", controlDataInputLineItem.getRootPipelineId()).bind("createdOn", LocalDate.now()).bind("groupId", controlDataInputLineItem.getGroupId()).bind("fileName", controlDataInputLineItem.getFileName()).bind("originId", controlDataInputLineItem.getOriginId()).bind("batchId", controlDataInputLineItem.getBatchId()).bind("paperNo", controlDataInputLineItem.getPaperNo()).bind("actualValue", controlDataInputLineItem.getActualValue()).bind("extractedValue", controlDataInputLineItem.getExtractedValue()).bind("matchStatus", matchStatus).bind("mismatchCount", mismatchCount).bind("tenantId", controlDataInputLineItem.getTenantId()).bind("classification", classification).bind("sorContainerId", controlDataInputLineItem.getSorContainerId()).bind("sorItemName", controlDataInputLineItem.getSorItemName()).bind("sorItemId", controlDataInputLineItem.getSorItemId()).execute());
    }


    private String determineClassification(String actualValue, String extractedValue, String matchStatus) {
        String normalizedActual = actualValue == null ? "" : actualValue.trim();
        String normalizedExtracted = extractedValue == null ? "" : extractedValue.trim();

        boolean actualEmpty = normalizedActual.isEmpty();
        boolean extractedEmpty = normalizedExtracted.isEmpty();

        if ("NO TOUCH".equals(matchStatus) && actualEmpty && extractedEmpty) {
            return "TN";
        }

        if ("NO TOUCH".equals(matchStatus) && !actualEmpty && !extractedEmpty) {
            return "TP";
        }

        if (actualEmpty && !extractedEmpty) {
            return "FN";
        }

        if (!actualEmpty && (extractedEmpty || !"NO TOUCH".equals(matchStatus))) {
            return "FP";
        }

        return "UNKNOWN";
    }

    public static String calculateValidationScores(Long mismatchCount,String oneTouch,String lowTouch) {
        String matchStatus;

        if (mismatchCount == 0) {
            matchStatus = "NO TOUCH";
        } else if (mismatchCount <= Long.parseLong(oneTouch)) {
            matchStatus = "ONE TOUCH";
        } else if (mismatchCount <= Long.parseLong(lowTouch)) {
            matchStatus = "LOW TOUCH";
        } else {
            matchStatus = "HIGH TOUCH";
        }
        return matchStatus;
    }
}
