package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lib.adapters.comparison.ComparisonAdapter;
import in.handyman.raven.lib.adapters.comparison.ComparisonAdapterFactory;
import in.handyman.raven.lib.model.ControlDataComparison;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import in.handyman.raven.util.CommonQueryUtil;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;

/**
 * Action to perform Control Data Comparison using CoproProcessor (producer/consumer).
 * Keeps original logic: decryption, validation, re-encryption, insertion, classification.
 */
@ActionExecution(actionName = "ControlDataComparison")
public class ControlDataComparisonAction implements IActionExecution {
    private final ActionExecutionAudit action;
    private final ControlDataComparison controlDataComparison;
    private final Logger log;
    private final Marker aMarker;

    public ControlDataComparisonAction(final ActionExecutionAudit action, final Logger log, final Object controlDataComparison) {
        this.controlDataComparison = (ControlDataComparison) controlDataComparison;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker("ControlDataComparison:" + this.controlDataComparison.getName());
    }

    @Override
    public void execute() throws Exception {
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(controlDataComparison.getResourceConn());
        try {
            log.info(aMarker, "Control Data Comparison Action for {} has been started", controlDataComparison.getName());

            String outputTable = controlDataComparison.getOutputTable();
            String querySet = controlDataComparison.getQuerySet();
            final List<ControlDataComparisonQueryInputTable> controlDataComparisonQueryInputTables = getControlDataComparisonQueryInputTables(jdbi, querySet);

            log.info(aMarker, "Total rows returned from the query: {}", controlDataComparisonQueryInputTables.size());

            String kafkaComparison = action.getContext().getOrDefault("kafka.production.activator", "false");
            InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);

            Map<String, String> decryptedActualMap = new HashMap<>();
            Map<String, String> decryptedExtractedMap = new HashMap<>();
            boolean itemWiseEncryption = "true".equalsIgnoreCase(action.getContext().getOrDefault(ENCRYPT_ITEM_WISE_ENCRYPTION, "false"));
            boolean actualEncryption = "true".equalsIgnoreCase(action.getContext().getOrDefault("actual.encryption.variable", "false"));

            // Pre-decryption logic (grouped by origin) — same as single-threaded logic
            if (itemWiseEncryption) {
                Map<String, List<ControlDataComparisonQueryInputTable>> groupedByOrigin = controlDataComparisonQueryInputTables.stream()
                        .filter(r -> "t".equalsIgnoreCase(r.getIsEncrypted()))
                        .collect(Collectors.groupingBy(ControlDataComparisonQueryInputTable::getOriginId));

                for (Map.Entry<String, List<ControlDataComparisonQueryInputTable>> entry : groupedByOrigin.entrySet()) {
                    String originId = entry.getKey();
                    List<ControlDataComparisonQueryInputTable> encryptedItems = entry.getValue();

                    // Decrypt actual values if configured
                    if (actualEncryption) {
                        List<EncryptionRequestClass> actualValueFields = encryptedItems.stream()
                                .filter(r -> r.getActualValue() != null && !r.getActualValue().trim().isEmpty())
                                .map(r -> new EncryptionRequestClass(r.getEncryptionPolicy(), r.getActualValue(), r.getSorItemName()))
                                .collect(Collectors.toList());

                        if (!actualValueFields.isEmpty()) {
                            try {
                                log.info(aMarker, "Decrypting ACTUAL values for originId: {}", originId);
                                List<EncryptionRequestClass> decryptedActuals = encryption.decrypt(actualValueFields);
                                decryptedActuals.forEach(decrypted -> {
                                    String key = originId + "|" + decrypted.getKey();
                                    decryptedActualMap.put(key, decrypted.getValue());
                                });
                                log.info(aMarker, "Actual value decryption successful for originId: {}", originId);
                            } catch (Exception e) {
                                log.error(aMarker, "Actual value decryption failed for originId: {}", originId, e);
                            }
                        }
                    }

                    // Decrypt extracted values
                    List<EncryptionRequestClass> extractedValueFields = encryptedItems.stream()
                            .filter(r -> r.getExtractedValue() != null && !r.getExtractedValue().trim().isEmpty())
                            .map(r -> new EncryptionRequestClass(r.getEncryptionPolicy(), r.getExtractedValue(), r.getSorItemName()))
                            .collect(Collectors.toList());

                    if (!extractedValueFields.isEmpty()) {
                        try {
                            log.info(aMarker, "Decrypting EXTRACTED values for originId: {}", originId);
                            List<EncryptionRequestClass> decryptedExtracted = encryption.decrypt(extractedValueFields);
                            decryptedExtracted.forEach(decrypted -> {
                                String key = originId + "|" + decrypted.getKey();
                                decryptedExtractedMap.put(key, decrypted.getValue());
                            });
                            log.info(aMarker, "Extracted value decryption successful for originId: {}", originId);
                        } catch (Exception e) {
                            log.error(aMarker, "Extracted value decryption failed for originId: {}", originId, e);
                        }
                    }
                }
            } else {
                log.info(aMarker, "Skipping decryption as itemWiseEncryption is false");
            }

            // Start async producer/consumer via CoproProcessor (consumers will handle per-batch re-encryption)
            BlockingQueue<ControlDataComparisonQueryInputTable> queue = new LinkedBlockingQueue<>();
            ControlDataComparisonQueryInputTable stoppingSeed = new ControlDataComparisonQueryInputTable();
            List<URL> nodes = new ArrayList<>();

            CoproProcessor<ControlDataComparisonQueryInputTable, ControlDataComparisonResult> coproProcessor =
                    new CoproProcessor<>(
                            queue,
                            ControlDataComparisonResult.class,
                            ControlDataComparisonQueryInputTable.class,
                            controlDataComparison.getResourceConn(),
                            log,
                            stoppingSeed,
                            nodes,
                            action
                    );

            // Feed queue by starting the producer using the same querySet
            coproProcessor.startProducer(controlDataComparison.getQuerySet(), Integer.parseInt(action.getContext().getOrDefault("read.batch.size", "100")));

            // Create consumer process instance
            ControlDataComparisonConsumerProcess consumerProcessor = new ControlDataComparisonConsumerProcess(action, log, aMarker);

            // Start consumers; each consumer will call the consumerProcessor.process per entity
            coproProcessor.startConsumer(
                    controlDataComparison.getOutputTable(),
                    Integer.parseInt(action.getContext().getOrDefault("consumer.API.count", "4")),
                    Integer.parseInt(action.getContext().getOrDefault("write.batch.size", "50")),
                    (endpoint, record) -> {
                        // consumer lambda invoked per record by CoproProcessor/InBoundBatchDataConsumer
                        // we return an empty list because actual row building to insert is handled inside consumerProcessor
                        try {
                            consumerProcessor.process(
                                    Collections.singletonList(record),
                                    decryptedActualMap,
                                    decryptedExtractedMap,
                                    jdbi,
                                    controlDataComparison.getOutputTable(),
                                    kafkaComparison,
                                    encryption
                            );
                        } catch (JsonProcessingException e) {
                            log.error(aMarker, "Error while processing record inside consumer lambda", e);
                        }
                        return Collections.emptyList();
                    }
            );

            log.info(aMarker, "Control Data Comparison Action has been completed: {}", controlDataComparison.getName());
            action.getContext().put(controlDataComparison.getName() + ".isSuccessful", "true");

        } catch (Exception e) {
            action.getContext().put(controlDataComparison.getName() + ".isSuccessful", "false");
            log.error(aMarker, "Error in execute method for Control Data Comparison", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Control data comparison failed", handymanException, action);
            throw handymanException;
        }
    }

    /**
     * Helper: query DB and map to input DTO list.
     */
    @NotNull
    public List<ControlDataComparisonQueryInputTable> getControlDataComparisonQueryInputTables(Jdbi jdbi, String querySet) {
        final List<ControlDataComparisonQueryInputTable> controlDataComparisonQueryInputTables = new ArrayList<>();

        jdbi.useTransaction(handle -> {
            final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(querySet);
            AtomicInteger i = new AtomicInteger(0);
            formattedQuery.forEach(sqlToExecute -> {
                log.info(aMarker, "Executing query {} from index {}", sqlToExecute, i.getAndIncrement());
                Query query = handle.createQuery(sqlToExecute);
                List<ControlDataComparisonQueryInputTable> results = query
                        .mapToBean(ControlDataComparisonQueryInputTable.class)
                        .list();
                controlDataComparisonQueryInputTables.addAll(results);
                log.info(aMarker, "Executed query from index {}", i.get());
            });
        });
        return controlDataComparisonQueryInputTables;
    }

    /**
     * Used by consumers to insert the validation result into DB.
     */
    public static void insertExecutionInfoStatic(org.jdbi.v3.core.Jdbi jdbi, String outputTable, String matchStatus,
                                                 Long mismatchCount, ControlDataComparisonQueryInputTable record) {
        jdbi.useHandle(handle -> {
            handle.createUpdate("INSERT INTO " + outputTable + " (" +
                            "root_pipeline_id, created_on, group_id, file_name, origin_id, batch_id, " +
                            "paper_no, actual_value, extracted_value, match_status, mismatch_count, " +
                            "tenant_id, classification, sor_container_id, sor_item_name, sor_item_id" +
                            ") VALUES (" +
                            ":rootPipelineId, :createdOn, :groupId, :fileName, :originId, :batchId, " +
                            ":paperNo, :actualValue, :extractedValue, :matchStatus, :mismatchCount, " +
                            ":tenantId, :classification, :sorContainerId, :sorItemName, :sorItemId" +
                            ")")
                    .bind("rootPipelineId", record.getRootPipelineId())
                    .bind("createdOn", LocalDate.now())
                    .bind("groupId", record.getGroupId())
                    .bind("fileName", record.getFileName())
                    .bind("originId", record.getOriginId())
                    .bind("batchId", record.getBatchId())
                    .bind("paperNo", record.getPaperNo())
                    .bind("actualValue", record.getActualValue())
                    .bind("extractedValue", record.getExtractedValue())
                    .bind("matchStatus", matchStatus)
                    .bind("mismatchCount", mismatchCount)
                    .bind("tenantId", record.getTenantId())
                    .bind("classification", determineClassification(record.getActualValue(), record.getExtractedValue(), matchStatus))
                    .bind("sorContainerId", record.getSorContainerId())
                    .bind("sorItemName", record.getSorItemName())
                    .bind("sorItemId", record.getSorItemId())
                    .execute();
        });
    }

    /**
     * Determine classification for insert (TP, TN, FP, FN, UNKNOWN).
     */
    static String determineClassification(String actualValue, String extractedValue, String matchStatus) {
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

    public static String calculateValidationScores(Long mismatchCount, String oneTouch, String lowTouch) {
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


    @Override
    public boolean executeIf() throws Exception {
        return controlDataComparison.getCondition();
    }
}