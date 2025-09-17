package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.adapters.comparison.ComparisonAdapter;
import in.handyman.raven.lib.adapters.comparison.ComparisonAdapterFactory;
import in.handyman.raven.lib.model.ControlDataComparison;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import in.handyman.raven.lib.ControlDataComparisonAction.ControlDataComparisonOutputTable;
import in.handyman.raven.lib.model.triton.PipelineName;
import in.handyman.raven.util.CommonQueryUtil;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Query;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;
import static in.handyman.raven.lib.ControlDataComparisonAction.ACTUAL_ENCRYPTION_VARIABLE;

public class ControlDataComparisonConsumerProcess implements CoproProcessor.ConsumerProcess<ControlDataComparisonQueryInputTable, ControlDataComparisonOutputTable> {
    public static final String PROCESS_NAME = PipelineName.CONTROL_DATA.getProcessName();
    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;
    private final ObjectMapper mapper = new ObjectMapper();
    private String jdbiResourceName;
    private ControlDataComparison controlDataComparison;

    public ControlDataComparisonConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action, String jdbiResourceName, ControlDataComparison controlDataComparison) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.jdbiResourceName = jdbiResourceName;
        this.controlDataComparison = controlDataComparison;
    }

    @Override
    public List<ControlDataComparisonOutputTable> process(URL endpoint, ControlDataComparisonQueryInputTable entity) throws Exception {
        try {
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(controlDataComparison.getResourceConn());
            log.info(aMarker, "Control Data Comparison Action for {} has been started", controlDataComparison.getName());

            String outputTable = controlDataComparison.getOutputTable();
            String querySet = controlDataComparison.getQuerySet();
            final List<ControlDataComparisonQueryInputTable> controlDataComparisonQueryInputTables = getControlDataComparisonQueryInputTables(jdbi, querySet);
            log.info(aMarker, "Total rows returned from the query: {}", controlDataComparisonQueryInputTables.size());

            // Decryption initial handler (used for global decryption phase)
            InticsIntegrity encryptionHandler = SecurityEngine.getInticsIntegrityMethod(action, log);
            log.info(aMarker, "Encryption Handler initialized: {}", encryptionHandler.getEncryptionMethod());

            // Decrypt (kept single-threaded to preserve original behavior and batch decrypt pattern)
            performDecryption(controlDataComparisonQueryInputTables, encryptionHandler);
            log.info(aMarker, "Decryption process completed");

            // Parallelize validation + encryption + insert by partitioning the records across threads
            invokeValidationPerRecordMultiThreaded(controlDataComparisonQueryInputTables, jdbi, outputTable);
            log.info(aMarker, "Control Data Comparison Action has been completed: {}", controlDataComparison.getName());
            action.getContext().put(controlDataComparison.getName() + ".isSuccessful", "true");
        } catch (Exception e) {
            action.getContext().put(controlDataComparison.getName() + ".isSuccessful", "false");
            log.error(aMarker, "Error in execute method for Control Data Comparison", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Control data comparison failed", handymanException, action);
            throw handymanException;
        }
        return null;
    }


    //DEBUG ON YOUR OWN RISK
    private void performDecryption(List<ControlDataComparisonQueryInputTable> records, InticsIntegrity encryptionHandler) {
        log.info(aMarker, "Total records to process for decryption: {}", records.size());
        boolean itemWiseEncryption = Boolean.parseBoolean(
                action.getContext().getOrDefault(ENCRYPT_ITEM_WISE_ENCRYPTION, "false"));
        boolean actualEncryption = Boolean.parseBoolean(
                action.getContext().getOrDefault(ACTUAL_ENCRYPTION_VARIABLE, "false"));

        if (itemWiseEncryption) {
            log.info(aMarker, "Starting Decryption process for item-wise encryption");
            decryptAndUpdate(records, encryptionHandler, true);
        } else {
            log.info(aMarker, "Skipping Decryption as itemWiseEncryption is false");
        }

        if (actualEncryption) {
            log.info(aMarker, "Starting Decryption process for actual value encryption");
            decryptAndUpdate(records, encryptionHandler, false);
        } else {
            log.info(aMarker, "Skipping Decryption as actualEncryption is false");
        }
    }

    //DEBUG ON YOUR OWN RISK
    private void performEncryption(
            List<ControlDataComparisonQueryInputTable> controlDataComparisonQueryInputTables,
            InticsIntegrity encryptionHandler
    ) {
        log.info(aMarker, "Total records to process for encryption: {}", controlDataComparisonQueryInputTables.size());
        boolean itemWiseEncryption = Boolean.parseBoolean(
                action.getContext().getOrDefault(ENCRYPT_ITEM_WISE_ENCRYPTION, "false")
        );
        boolean actualEncryption = Boolean.parseBoolean(
                action.getContext().getOrDefault(ACTUAL_ENCRYPTION_VARIABLE, "false")
        );

        if (itemWiseEncryption) {
            log.info(aMarker, "Starting Encryption process for item-wise encryption");
            encryptAndApply(controlDataComparisonQueryInputTables, encryptionHandler, true);
        } else {
            log.info(aMarker, "Skipping Encryption as itemWiseEncryption is false");
        }

        if (actualEncryption) {
            log.info(aMarker, "Starting Encryption process for actual encryption");
            encryptAndApply(controlDataComparisonQueryInputTables, encryptionHandler, false);
        } else {
            log.info(aMarker, "Skipping Encryption as actualEncryption is false");
        }
    }

    private void decryptAndUpdate(
            List<ControlDataComparisonQueryInputTable> records, InticsIntegrity encryptionHandler, boolean isExtracted
    ) {
        log.info(aMarker, "Total records to process for decryption (isExtracted={}): {}", isExtracted, records.size());
        List<EncryptionRequestClass> requests = buildDecryptionRequests(records, isExtracted);
        if (requests.isEmpty()) {
            return;
        }
        List<EncryptionRequestClass> responses = encryptionHandler.decrypt(requests);
        Map<String, List<EncryptionRequestClass>> groupedResponses = groupResponsesByRecordId(responses);
        applyDecryptedValues(records, groupedResponses, isExtracted);
    }

    /**
     * Builds decryption requests from records.
     */
    private List<EncryptionRequestClass> buildDecryptionRequests(
            List<ControlDataComparisonQueryInputTable> records, boolean isExtracted
    ) {
        List<EncryptionRequestClass> requests = new ArrayList<>();
        for (ControlDataComparisonQueryInputTable r : records) {
            if (!"t".equalsIgnoreCase(r.getIsEncrypted())) continue;
            String rawVal = isExtracted ? r.getExtractedValue() : r.getActualValue();
            if (rawVal == null || rawVal.isEmpty()) continue;
            if ("multi_value".equalsIgnoreCase(r.getLineItemType()) && rawVal.contains(",")) {
                addMultiValueRequests(requests, r, rawVal);
            } else {
                requests.add(newRequest(String.valueOf(r.getId()), rawVal, r.getEncryptionPolicy()));
            }
        }
        return requests;
    }

    /**
     * Adds split requests for multi-value fields.
     */
    private void addMultiValueRequests(List<EncryptionRequestClass> requests, ControlDataComparisonQueryInputTable record, String rawVal) {
        log.info("Adding multi-value requests for record ID: {}", record.getId());
        String[] parts = rawVal.split(",");
        for (int i = 0; i < parts.length; i++) {
            String trimmed = parts[i].trim();
            if (!trimmed.isEmpty()) {
                requests.add(newRequest(record.getId() + "_" + i, trimmed, record.getEncryptionPolicy()));
            }
        }
    }

    /**
     * Builds a single request object.
     */
    private EncryptionRequestClass newRequest(String key, String value, String policy) {
        log.info("Creating request - Key: {}, Policy: {}", key, policy);
        return EncryptionRequestClass.builder()
                .key(key)
                .value(value)
                .policy(policy)
                .build();
    }

    /**
     * Groups responses by record id (ignores multi-value suffix).
     */
    private Map<String, List<EncryptionRequestClass>> groupResponsesByRecordId(
            List<EncryptionRequestClass> responses
    ) {
        log.info("Grouping responses by record ID, total responses: {}", responses.size());
        return responses.stream()
                .collect(Collectors.groupingBy(resp -> {
                    String key = resp.getKey();
                    return key.contains("_") ? key.substring(0, key.indexOf("_")) : key;
                }));
    }

    /**
     * Applies decrypted values back to records.
     */
    private void applyDecryptedValues(List<ControlDataComparisonQueryInputTable> records, Map<String, List<EncryptionRequestClass>> groupedResponses, boolean isExtracted) {
        log.info(aMarker, "Applying decrypted values to records (isExtracted={}): {}", isExtracted, records.size());
        for (ControlDataComparisonQueryInputTable r : records) {
            List<EncryptionRequestClass> respList = groupedResponses.get(String.valueOf(r.getId()));
            if (respList == null) continue;
            // preserve order for multi-value
            respList.sort(Comparator.comparingInt(resp -> {
                if (!resp.getKey().contains("_")) return 0;
                return Integer.parseInt(resp.getKey().substring(resp.getKey().indexOf("_") + 1));
            }));
            String finalValue = respList.stream()
                    .map(EncryptionRequestClass::getValue)
                    .collect(Collectors.joining(","));
            if (isExtracted) {
                r.setExtractedValue(finalValue);
            } else {
                r.setActualValue(finalValue);
            }
        }
    }

    private void encryptAndApply(
            List<ControlDataComparisonQueryInputTable> records, InticsIntegrity encryptionHandler, boolean isExtracted
    ) {
        log.info(aMarker, "Total records to process for encryption (isExtracted={}): {}", isExtracted, records.size());
        List<EncryptionRequestClass> requests = buildEncryptionRequests(records, isExtracted);
        if (requests.isEmpty()) {
            log.info(aMarker, "No records found for encryption [{}]", isExtracted ? "ExtractedValue" : "ActualValue");
            return;
        }
        List<EncryptionRequestClass> responses = encryptionHandler.encrypt(requests);
        Map<String, List<EncryptionRequestClass>> groupedResponses = groupResponsesByRecordId(responses);
        applyEncryptedValues(records, groupedResponses, isExtracted);
    }

    /**
     * Builds encryption requests (single + multi expanded).
     */
    private List<EncryptionRequestClass> buildEncryptionRequests(
            List<ControlDataComparisonQueryInputTable> records, boolean isExtracted
    ) {
        log.info("Building encryption requests (isExtracted={}): {}", isExtracted, records.size());
        List<EncryptionRequestClass> requests = new ArrayList<>();
        for (ControlDataComparisonQueryInputTable r : records) {
            if (!"t".equalsIgnoreCase(r.getIsEncrypted())) continue;
            String rawVal = isExtracted ? r.getExtractedValue() : r.getActualValue();
            if (rawVal == null || rawVal.isEmpty()) continue;
            if ("multi_value".equalsIgnoreCase(r.getLineItemType()) && rawVal.contains(",")) {
                addMultiValueRequests(requests, r, rawVal);
            } else {
                requests.add(newRequest(String.valueOf(r.getId()), rawVal, r.getEncryptionPolicy()));
            }
        }
        return requests;
    }

    /**
     * Applies encrypted values back to the original records.
     */
    private void applyEncryptedValues(List<ControlDataComparisonQueryInputTable> records, Map<String, List<EncryptionRequestClass>> groupedResponses, boolean isExtracted) {
        log.info(aMarker, "Applying encrypted values to records (isExtracted={}): {}", isExtracted, records.size());
        for (ControlDataComparisonQueryInputTable r : records) {
            List<EncryptionRequestClass> respList = groupedResponses.get(String.valueOf(r.getId()));
            if (respList == null) continue;
            // Preserve order for multi-value
            respList.sort(Comparator.comparingInt(resp -> {
                if (!resp.getKey().contains("_")) return 0;
                return Integer.parseInt(resp.getKey().substring(resp.getKey().indexOf("_") + 1));
            }));
            String finalValue = respList.stream()
                    .map(EncryptionRequestClass::getValue)
                    .collect(Collectors.joining(","));
            if (isExtracted) {
                r.setExtractedValue(finalValue);
            } else {
                r.setActualValue(finalValue);
            }
        }
    }

    @NotNull
    public List<ControlDataComparisonQueryInputTable> getControlDataComparisonQueryInputTables(Jdbi jdbi, String querySet) {
        log.info(aMarker,"{}: Executing query set to fetch input data",querySet);
        final List<ControlDataComparisonQueryInputTable> controlDataComparisonQueryInputTables = new ArrayList<>();
        jdbi.useTransaction(handle -> {
            final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(querySet);
            AtomicInteger i = new AtomicInteger(0);
            formattedQuery.forEach(sqlToExecute -> {
                log.info(aMarker, "Executing query {} from index {}", sqlToExecute, i.getAndIncrement());
                Query query = handle.createQuery(sqlToExecute);
                List<ControlDataComparisonQueryInputTable> results = query.mapToBean(ControlDataComparisonQueryInputTable.class).list();
                controlDataComparisonQueryInputTables.addAll(results);
                log.info(aMarker, "Executed query from index {}", i.get());
            });
        });
        log.info(aMarker, "Total records fetched: {}", controlDataComparisonQueryInputTables.size());
        return controlDataComparisonQueryInputTables;
    }

    /**
     * MULTI-THREADED orchestration:
     * Partition the original records and process partitions in parallel.
     * Each partition will:
     *  - validate records using adapters
     *  - perform encryption on validated partition (with its own encryption handler)
     *  - insert results (batch) using Jdbi
     */
    public void invokeValidationPerRecordMultiThreaded(
            List<ControlDataComparisonQueryInputTable> originalRecords, Jdbi jdbi, String outputTable
    ) throws Exception {
        log.info(aMarker, "Starting multi-threaded validation for {} records", originalRecords.size());
        if (originalRecords == null || originalRecords.isEmpty()) {
            return;
        }

        // determine thread pool size from context or default to available processors
        int threadPoolSize = Runtime.getRuntime().availableProcessors();
        try {
            String configured = action.getContext().get("control.data.thread.pool.size");
            if (configured != null) {
                threadPoolSize = Integer.parseInt(configured);
            }
        } catch (Exception ex) {
            log.warn(aMarker, "Failed to parse control.data.thread.pool.size, using default: {}", threadPoolSize, ex);
        }

        // Partition the list into nearly-equal chunks
        List<List<ControlDataComparisonQueryInputTable>> partitions = partitionList(originalRecords, threadPoolSize);
        log.info(aMarker, "Partitioned records into {} chunks (threadPoolSize={})", partitions.size(), threadPoolSize);

        ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, threadPoolSize));
        List<Future<Integer>> futures = new ArrayList<>();

        for (List<ControlDataComparisonQueryInputTable> partition : partitions) {
            Callable<Integer> worker = () -> {
                // Each thread creates its own encryption handler instance to avoid sharing state
                InticsIntegrity threadEncryptionHandler = SecurityEngine.getInticsIntegrityMethod(action, log);

                // 1) Validate each record in the partition
                List<ControlDataComparisonQueryInputTable> processedRecords = partition.stream()
                        .map(this::doControlDataValidationByAdapters)
                        .collect(Collectors.toList());
                log.info(aMarker, "Thread {} completed validation for {} records", Thread.currentThread().getName(), processedRecords.size());

                // 2) Perform encryption on this chunk (local encryption handler)
                performEncryption(processedRecords, threadEncryptionHandler);
                log.info(aMarker, "Thread {} completed encryption for {} records", Thread.currentThread().getName(), processedRecords.size());

                // 3) Insert results for this chunk
                insertExecutionInfo(jdbi, outputTable, processedRecords);
                log.info(aMarker, "Thread {} inserted {} records into {}", Thread.currentThread().getName(), processedRecords.size(), outputTable);

                return processedRecords.size();
            };

            futures.add(executor.submit(worker));
        }

        // Wait for completion, collect results and detect exceptions
        Exception taskException = null;
        int totalProcessed = 0;
        for (Future<Integer> f : futures) {
            try {
                totalProcessed += f.get();
            } catch (ExecutionException ee) {
                log.error(aMarker, "Error while processing partition", ee.getCause());
                taskException = ee;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error(aMarker, "Thread interrupted while waiting for partition completion", ie);
                taskException = ie;
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn(aMarker, "Executor did not terminate in 60 seconds, forcing shutdown");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (taskException != null) {
            HandymanException handymanException = new HandymanException(taskException);
            HandymanException.insertException("Control data comparison failed during parallel processing", handymanException, action);
            throw handymanException;
        }

        log.info(aMarker, "Completed multi-threaded processing. Total processed records: {}", totalProcessed);

    }

    /**
     * Partition a list into at most 'parts' sublists with near-equal size.
     */
    private <T> List<List<T>> partitionList(List<T> source, int parts) {
        List<List<T>> lists = new ArrayList<>();
        if (source == null || source.isEmpty()) {
            return lists;
        }
        int actualParts = Math.min(parts, source.size());
        int baseSize = source.size() / actualParts;
        int remainder = source.size() % actualParts;
        int currentIndex = 0;
        for (int i = 0; i < actualParts; i++) {
            int chunkSize = baseSize + (i < remainder ? 1 : 0);
            List<T> sub = source.subList(currentIndex, currentIndex + chunkSize);
            lists.add(new ArrayList<>(sub));
            currentIndex += chunkSize;
        }
        return lists;
    }

    private ControlDataComparisonQueryInputTable doControlDataValidationByAdapters(ControlDataComparisonQueryInputTable comparisonInputLineItem) {
        log.info(aMarker, "Validating record ID: {}, Origin ID: {}, Paper No: {}, Sor Item Name: {}",
                comparisonInputLineItem.getId(), comparisonInputLineItem.getOriginId(), comparisonInputLineItem.getPaperNo(), comparisonInputLineItem.getSorItemName());
        String lowTouch = action.getContext().get("control.data.low.touch.threshold");
        String oneTouch = action.getContext().get("control.data.one.touch.threshold");
        String adapterKey = comparisonInputLineItem.getAllowedAdapter() != null ? comparisonInputLineItem.getAllowedAdapter() : "string";
        ComparisonAdapter adapter = ComparisonAdapterFactory.getAdapter(adapterKey);
        Long mismatchCount = adapter.validate(comparisonInputLineItem, action, log);
        String matchStatus = calculateValidationScores(mismatchCount, oneTouch, lowTouch);
        comparisonInputLineItem.setMatchStatus(matchStatus);
        comparisonInputLineItem.setMismatchCount(mismatchCount);
        log.info("Record ID: {}, Validation result - Match Status: {}, Mismatch Count: {}", comparisonInputLineItem.getId(), matchStatus, mismatchCount);
        return comparisonInputLineItem;
    }

    private void insertExecutionInfo(Jdbi jdbi, String outputTable, List<ControlDataComparisonQueryInputTable> controlDataInputLineItems) {
        if (controlDataInputLineItems == null || controlDataInputLineItems.isEmpty()) {
            return;
        }
        log.info(aMarker, "Inserting {} records into output table: {}", controlDataInputLineItems.size(), outputTable);
        jdbi.useHandle(handle -> {
            String sql = "INSERT INTO " + outputTable + " (" +
                    "root_pipeline_id, created_on, group_id, file_name, origin_id, batch_id, " +
                    "paper_no, actual_value, extracted_value, match_status, mismatch_count, " +
                    "tenant_id, classification, sor_container_id, sor_item_name, sor_item_id" +
                    ") VALUES (" +
                    ":rootPipelineId, :createdOn, :groupId, :fileName, :originId, :batchId, :paperNo, " +
                    ":actualValue, :extractedValue, :matchStatus, :mismatchCount, :tenantId, " +
                    ":classification, :sorContainerId, :sorItemName, :sorItemId" +
                    ")";
            PreparedBatch batch = handle.prepareBatch(sql);
            for (ControlDataComparisonQueryInputTable item : controlDataInputLineItems) {
                log.info("Queueing data validation result for origin id: {} and paper no: {} and sor item name: {} " +
                                "with match status: {} and mismatch count: {}",
                        item.getOriginId(), item.getPaperNo(), item.getSorItemName(), item.getMatchStatus(), item.getMismatchCount());
                String classification = determineClassification(item.getActualValue(), item.getExtractedValue(), item.getMatchStatus());
                batch.bind("rootPipelineId", item.getRootPipelineId())
                        .bind("createdOn", LocalDate.now())
                        .bind("groupId", item.getGroupId())
                        .bind("fileName", item.getFileName())
                        .bind("originId", item.getOriginId())
                        .bind("batchId", item.getBatchId())
                        .bind("paperNo", item.getPaperNo())
                        .bind("actualValue", item.getActualValue())
                        .bind("extractedValue", item.getExtractedValue())
                        .bind("matchStatus", item.getMatchStatus())
                        .bind("mismatchCount", item.getMismatchCount())
                        .bind("tenantId", item.getTenantId())
                        .bind("classification", classification)
                        .bind("sorContainerId", item.getSorContainerId())
                        .bind("sorItemName", item.getSorItemName())
                        .bind("sorItemId", item.getSorItemId())
                        .add();
            }
            int[] counts = batch.execute();
            log.info("Batch insert completed. Inserted {} rows into {}", counts.length, outputTable);
        });
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

}
