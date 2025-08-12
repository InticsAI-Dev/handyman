package in.handyman.raven.lib.model.asset.info;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Optimized InboundBatchDataConsumer with async processing and improved performance
 */
public class OptimizedInboundBatchDataConsumer<I, O extends CoproProcessor.Entity> implements Callable<Void> {

    public static final String COPRO_PROCESSOR_RETRY_FAILED_FILES = "copro.processor.retry.failed.files";
    public static final String COPRO_PROCESSOR_RETRY_FAILED_FILES_MAX_ATTEMPTS = "copro.processor.retry.failed.files.max.attempts";
    public static final String ENABLE_ASYNC_PROCESSING = "enable.async.processing";
    public static final String ASYNC_BATCH_SIZE = "async.batch.size";

    private final String insertSql;
    private final Integer writeBatchSize;
    private final CoproProcessor.ConsumerProcess<I, O> callable;
    private final Predicate<I> tPredicate;
    private final LocalDateTime startTime;
    private final CountDownLatch countDownLatch;
    private final BlockingQueue<I> queue;
    private final AtomicInteger nodeCount;
    private final int nodeSize;
    private final ActionExecutionAudit actionExecutionAudit;
    private final List<URL> nodes;
    private final String jdbiResourceName;
    private final Logger logger;

    // Async processing components
    private final ExecutorService processingExecutor;
    private final ExecutorService databaseExecutor;
    private final boolean asyncEnabled;
    private final int asyncBatchSize;

    public OptimizedInboundBatchDataConsumer(String insertSql, Integer writeBatchSize,
                                             CoproProcessor.ConsumerProcess<I, O> callable, Predicate<I> tPredicate,
                                             LocalDateTime startTime, CountDownLatch countDownLatch, BlockingQueue<I> queue,
                                             AtomicInteger nodeCount, int nodeSize, ActionExecutionAudit actionExecutionAudit,
                                             List<URL> nodes, String jdbiResourceName, Logger logger) {
        this.insertSql = insertSql;
        this.writeBatchSize = writeBatchSize;
        this.callable = callable;
        this.tPredicate = tPredicate;
        this.startTime = startTime;
        this.countDownLatch = countDownLatch;
        this.queue = queue;
        this.nodeCount = nodeCount;
        this.nodeSize = nodeSize;
        this.actionExecutionAudit = actionExecutionAudit;
        this.nodes = nodes;
        this.jdbiResourceName = jdbiResourceName;
        this.logger = logger;

        // Initialize async processing
        this.asyncEnabled = Boolean.parseBoolean(
                actionExecutionAudit.getContext().getOrDefault(ENABLE_ASYNC_PROCESSING, "true")
        );
        this.asyncBatchSize = Integer.parseInt(
                actionExecutionAudit.getContext().getOrDefault(ASYNC_BATCH_SIZE, "10")
        );

        int corePoolSize = Runtime.getRuntime().availableProcessors();

        this.processingExecutor = new ThreadPoolExecutor(
                corePoolSize, corePoolSize * 2, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                r -> new Thread(r, "AsyncProcessor-" + System.currentTimeMillis()),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        this.databaseExecutor = new ThreadPoolExecutor(
                2, 4, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                r -> new Thread(r, "DBProcessor-" + System.currentTimeMillis()),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Override
    public Void call() throws Exception {
        Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
        logger.info("Optimized Consumer {} started with async enabled: {}, batch size: {}",
                countDownLatch.getCount(), asyncEnabled, asyncBatchSize);

        try {
            if (asyncEnabled) {
                return processWithAsync(jdbi);
            } else {
                return processWithoutAsync(jdbi);
            }
        } finally {
            shutdownExecutors();
            logger.info("Consumer {} completed processing with node count {}",
                    countDownLatch.getCount(), nodeCount.get());
            countDownLatch.countDown();
        }
    }

    /**
     * Process items with async processing enabled
     */
    private Void processWithAsync(Jdbi jdbi) throws Exception {
        final List<O> processedEntity = new ArrayList<>();
        final List<CompletableFuture<List<O>>> pendingFutures = new ArrayList<>();

        while (true) {
            try {
                final I take = queue.take();
                if (!tPredicate.test(take)) {
                    logger.info("Breaking the async consumer");
                    queue.add(take);
                    break;
                }

                // Process item asynchronously
                CompletableFuture<List<O>> future = processItemAsync(take);
                pendingFutures.add(future);

                // Check if we should wait for some futures to complete
                if (pendingFutures.size() >= asyncBatchSize) {
                    collectCompletedFutures(pendingFutures, processedEntity);
                    writeBatchToDBOnCondition(insertSql, writeBatchSize, processedEntity, startTime, jdbi);
                }

            } catch (InterruptedException e) {
                logger.error("Error at Async Consumer thread", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error at Async Consumer Process", e);
                insertException("Error at Async Consumer Process", e);
            }
        }

        // Wait for all remaining futures
        waitForAllFutures(pendingFutures, processedEntity);
        checkProcessedEntitySizeForPendingQueue(insertSql, processedEntity, startTime, jdbi);

        return null;
    }

    /**
     * Process items without async processing (fallback)
     */
    private Void processWithoutAsync(Jdbi jdbi) throws Exception {
        final List<O> processedEntity = new ArrayList<>();

        while (true) {
            try {
                final I take = queue.take();
                if (!tPredicate.test(take)) {
                    logger.info("Breaking the sync consumer");
                    queue.add(take);
                    break;
                }

                final int index = nodeCount.incrementAndGet() % nodeSize;
                takeAndAddRowsBeforeDbFlush(callable, index, take, processedEntity, jdbi);
                writeBatchToDBOnCondition(insertSql, writeBatchSize, processedEntity, startTime, jdbi);

            } catch (InterruptedException e) {
                logger.error("Error at Consumer thread", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error at Consumer Process", e);
                insertException("Error at Consumer Process", e);
            }
        }

        checkProcessedEntitySizeForPendingQueue(insertSql, processedEntity, startTime, jdbi);
        return null;
    }

    /**
     * Process a single item asynchronously
     */
    private CompletableFuture<List<O>> processItemAsync(I item) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final int index = nodeCount.incrementAndGet() % nodeSize;
                final List<O> results = new ArrayList<>();

                int nodesSize = nodes.size();
                if (nodesSize > index) {
                    URL endpoint = nodes.get(index);
                    final List<O> initialResults = callable.process(endpoint, item);

                    String retryFailedFilesActivator = actionExecutionAudit.getContext()
                            .getOrDefault(COPRO_PROCESSOR_RETRY_FAILED_FILES, "false");

                    if ("true".equals(retryFailedFilesActivator)) {
                        int maxAttempts = Integer.parseInt(actionExecutionAudit.getContext()
                                .getOrDefault(COPRO_PROCESSOR_RETRY_FAILED_FILES_MAX_ATTEMPTS, "3"));

                        // Process retries asynchronously as well
                        final List<O> finalResults = retryFailedFilesAsync(
                                initialResults, callable, endpoint, item, maxAttempts
                        );
                        results.addAll(finalResults);
                    } else {
                        results.addAll(initialResults);
                    }
                }

                return results;
            } catch (Exception e) {
                logger.error("Error in async callable process", e);
                insertException("Error in async callable process", e);
                return new ArrayList<>();
            }
        }, processingExecutor);
    }

    /**
     * Collect completed futures and add results to processed entities
     */
    private void collectCompletedFutures(List<CompletableFuture<List<O>>> futures, List<O> processedEntity) {
        List<CompletableFuture<List<O>>> completed = futures.stream()
                .filter(CompletableFuture::isDone)
                .collect(Collectors.toList());

        for (CompletableFuture<List<O>> future : completed) {
            try {
                List<O> results = future.get(100, TimeUnit.MILLISECONDS);
                processedEntity.addAll(results);
            } catch (Exception e) {
                logger.error("Error collecting future result", e);
            }
        }

        futures.removeAll(completed);
    }

    /**
     * Wait for all remaining futures to complete
     */
    private void waitForAllFutures(List<CompletableFuture<List<O>>> futures, List<O> processedEntity) {
        if (!futures.isEmpty()) {
            try {
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0])
                );
                allFutures.get(30, TimeUnit.SECONDS);

                // Collect all results
                for (CompletableFuture<List<O>> future : futures) {
                    try {
                        List<O> results = future.get();
                        processedEntity.addAll(results);
                    } catch (Exception e) {
                        logger.error("Error getting final future result", e);
                    }
                }
            } catch (Exception e) {
                logger.error("Error waiting for all futures", e);
                insertException("Error waiting for all futures", e);
            }
        }
    }

    /**
     * Async retry mechanism for failed files
     */
    private List<O> retryFailedFilesAsync(List<O> resultSet, CoproProcessor.ConsumerProcess<I, O> callable,
                                          URL endpoint, I take, int maxAttempts) {
        List<O> finalResults = new ArrayList<>(resultSet);
        List<O> failedResults = extractFailedResults(resultSet);
        finalResults.removeAll(failedResults);

        if (failedResults.isEmpty()) {
            return finalResults;
        }

        // Process retries asynchronously
        List<CompletableFuture<List<O>>> retryFutures = failedResults.stream()
                .map(failedItem -> retryItemAsync(callable, endpoint, take, maxAttempts))
                .collect(Collectors.toList());

        try {
            CompletableFuture<Void> allRetries = CompletableFuture.allOf(
                    retryFutures.toArray(new CompletableFuture[0])
            );
            allRetries.get(60, TimeUnit.SECONDS);

            // Collect retry results
            for (CompletableFuture<List<O>> retryFuture : retryFutures) {
                try {
                    List<O> retryResults = retryFuture.get();
                    if (retryResults != null && !retryResults.isEmpty()) {
                        processRetryResults(retryResults, finalResults, take);
                    }
                } catch (Exception e) {
                    logger.error("Error getting retry result", e);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing async retries", e);
        }

        return finalResults;
    }

    /**
     * Retry a single item asynchronously
     */
    private CompletableFuture<List<O>> retryItemAsync(CoproProcessor.ConsumerProcess<I, O> callable,
                                                      URL endpoint, I take, int maxAttempts) {
        return CompletableFuture.supplyAsync(() -> {
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    List<O> retryOutput = callable.process(endpoint, take);
                    if (hasSuccess(retryOutput)) {
                        return retryOutput;
                    }
                } catch (Exception e) {
                    logger.error("Retry attempt {} failed. Error: {}", attempt, e.getMessage());
                }
            }
            return null;
        }, processingExecutor);
    }

    /**
     * Async database writing
     */
    private void writeBatchToDBAsync(String insertSql, List<O> processedEntity, LocalDateTime startTime, Jdbi jdbi) {
        if (processedEntity.isEmpty()) return;

        CompletableFuture.runAsync(() -> {
            try {
                jdbi.useTransaction(handle -> {
                    final PreparedBatch preparedBatch = handle.prepareBatch(insertSql);
                    iterateAndAddToBatch(processedEntity, preparedBatch);

                    logger.debug("Async batch insert query input entity size: {}", processedEntity.size());
                    int[] executeResults = preparedBatch.execute();
                    logger.debug("Async batch insert executed successfully. Rows affected: {}", executeResults.length);
                });

                insertRowsProcessedIntoStatementAudit(startTime, processedEntity);
            } catch (Exception e) {
                logger.error("Error during async batch insert", e);
                insertException("Failed to write async batch to DB", e);
            }
        }, databaseExecutor);
    }

    // Original methods with minor optimizations
    private void takeAndAddRowsBeforeDbFlush(CoproProcessor.ConsumerProcess<I, O> callable, int index, I take,
                                             List<O> processedEntity, Jdbi jdbi) {
        final List<O> results = new ArrayList<>();
        try {
            int nodesSize = nodes.size();
            if (nodesSize > index) {
                URL endpoint = nodes.get(index);
                final List<O> initialResults = callable.process(endpoint, take);
                String retryFailedFilesActivator = actionExecutionAudit.getContext()
                        .getOrDefault(COPRO_PROCESSOR_RETRY_FAILED_FILES, "false");

                if ("true".equals(retryFailedFilesActivator)) {
                    int retryFailedFilesMaxAttempts = Integer.parseInt(actionExecutionAudit.getContext()
                            .getOrDefault(COPRO_PROCESSOR_RETRY_FAILED_FILES_MAX_ATTEMPTS, "3"));
                    final List<O> finalResults = retryFailedFiles(initialResults, callable, endpoint, take,
                            retryFailedFilesMaxAttempts, jdbi);
                    results.addAll(finalResults);
                } else {
                    results.addAll(initialResults);
                }
            }
        } catch (Exception e) {
            logger.error("Error in callable process in consumer", e);
            insertException("Error in callable process in consumer", e);
        }
        processedEntity.addAll(results);
    }

    private List<O> retryFailedFiles(List<O> resultSet, CoproProcessor.ConsumerProcess<I, O> callable, URL endpoint,
                                     I take, int maxAttempts, Jdbi jdbi) {
        List<O> finalResults = new ArrayList<>(resultSet);
        List<O> failedResults = extractFailedResults(resultSet);
        finalResults.removeAll(failedResults);

        insertFailedCoproProcess(take, failedResults, 0, jdbi);

        for (O failedItem : failedResults) {
            List<O> retryOutput = retryWithAttempts(callable, endpoint, take, maxAttempts, jdbi);
            processRetryResults(retryOutput, finalResults, take);
        }

        return finalResults;
    }

    private List<O> extractFailedResults(List<O> resultSet) {
        return resultSet.stream()
                .filter(o -> ConsumerProcessApiStatus.FAILED.getStatusDescription().equals(o.getStatus()))
                .collect(Collectors.toList());
    }

    private List<O> retryWithAttempts(CoproProcessor.ConsumerProcess<I, O> callable, URL endpoint, I take,
                                      int maxAttempts, Jdbi jdbi) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                List<O> retryOutput = callable.process(endpoint, take);
                if (hasSuccess(retryOutput)) return retryOutput;
                insertFailedCoproProcess(take, retryOutput, attempt, jdbi);
            } catch (Exception e) {
                logger.error("Retry attempt {} failed for item. Error: {}", attempt, e.getMessage(), e);
                insertException("Error in retry attempt", e);
            }
        }
        return null;
    }

    private boolean hasSuccess(List<O> retryOutput) {
        return retryOutput != null && retryOutput.stream()
                .anyMatch(o -> ConsumerProcessApiStatus.COMPLETED.getStatusDescription().equals(o.getStatus()));
    }

    private void processRetryResults(List<O> retryOutput, List<O> finalResults, I take) {
        if (retryOutput == null || retryOutput.isEmpty()) return;

        boolean success = false;
        for (O result : retryOutput) {
            if (ConsumerProcessApiStatus.COMPLETED.getStatusDescription().equals(result.getStatus())) {
                finalResults.add(result);
                success = true;
                break;
            }
        }

        if (!success) {
            retryOutput.stream()
                    .filter(o -> ConsumerProcessApiStatus.FAILED.getStatusDescription().equals(o.getStatus()))
                    .forEach(finalResults::add);
        }
    }

    private void insertFailedCoproProcess(I failedEntityRequest, List<O> retryOutputResponse, int attemptNumber, Jdbi jdbi) {
        retryOutputResponse.forEach(o -> {
            jdbi.useHandle(handle -> {
                handle.createUpdate(
                                "INSERT INTO failed_copro_process (" +
                                        "created_by, created_date, last_modified_by, last_modified_date, " +
                                        "action_id, rows_processed, rows_read, rows_written, " +
                                        "request, failed_response, attempts, time_taken, root_pipeline_id" +
                                        ") VALUES (:createdBy, :createdDate, :lastModifiedBy, :lastModifiedDate, " +
                                        ":actionId, :rowsProcessed, :rowsRead, :rowsWritten, " +
                                        ":request, :failedResponse, :attempts, :timeTaken, :rootPipelineId)"
                        )
                        .bind("createdBy", actionExecutionAudit.getCreatedBy())
                        .bind("createdDate", LocalDateTime.now())
                        .bind("lastModifiedBy", actionExecutionAudit.getLastModifiedBy())
                        .bind("lastModifiedDate", LocalDateTime.now())
                        .bind("actionId", actionExecutionAudit.getActionId())
                        .bind("rowsProcessed", 0)
                        .bind("rowsRead", 0)
                        .bind("rowsWritten", 0)
                        .bind("request", failedEntityRequest)
                        .bind("failedResponse", o)
                        .bind("attempts", attemptNumber)
                        .bind("timeTaken", 0.0)
                        .bind("rootPipelineId", actionExecutionAudit.getRootPipelineId())
                        .execute();
            });
        });
    }

    private void writeBatchToDBOnCondition(String insertSql, Integer writeBatchSize, List<O> processedEntity,
                                           LocalDateTime startTime, Jdbi jdbi) {
        if (nodeCount.get() % writeBatchSize == 0 && !processedEntity.isEmpty()) {
            if (asyncEnabled) {
                writeBatchToDBAsync(insertSql, new ArrayList<>(processedEntity), startTime, jdbi);
                processedEntity.clear();
            } else {
                writeBatchToDBSync(insertSql, processedEntity, startTime, jdbi);
            }
        }
    }

    private void writeBatchToDBSync(String insertSql, List<O> processedEntity, LocalDateTime startTime, Jdbi jdbi) {
        try {
            jdbi.useTransaction(handle -> {
                final PreparedBatch preparedBatch = handle.prepareBatch(insertSql);
                iterateAndAddToBatch(processedEntity, preparedBatch);

                logger.info("Prepared batch insert query input entity size: {}", processedEntity.size());
                int[] executeResults = preparedBatch.execute();
                logger.info("Batch insert executed successfully. Rows affected: {}", executeResults.length);
            });

            insertRowsProcessedIntoStatementAudit(startTime, processedEntity);
            processedEntity.clear();

        } catch (Exception e) {
            logger.error("Error during batch insert. Batch size: {}, Entities: {}",
                    writeBatchSize, processedEntity.size(), e);
            insertException("Failed to write batch to DB", e);
        }
    }

    private void iterateAndAddToBatch(List<O> processedEntity, PreparedBatch preparedBatch) {
        for (final O output : processedEntity) {
            addRowsToBatch(output, preparedBatch);
            preparedBatch.add();
        }
    }

    private static <O extends CoproProcessor.Entity> void addRowsToBatch(O output, PreparedBatch preparedBatch) {
        final List<Object> rowData = output.getRowData();
        for (int i = 0; i < rowData.size(); i++) {
            preparedBatch.bind(i, rowData.get(i));
        }
    }

    private void checkProcessedEntitySizeForPendingQueue(String insertSql, List<O> processedEntity,
                                                         LocalDateTime startTime, Jdbi jdbi) {
        if (!processedEntity.isEmpty()) {
            if (asyncEnabled) {
                writeBatchToDBAsync(insertSql, processedEntity, startTime, jdbi);
            } else {
                writeBatchToDBSync(insertSql, processedEntity, startTime, jdbi);
            }
        }
    }

    private void insertRowsProcessedIntoStatementAudit(LocalDateTime startTime, List<O> processedEntity) {
        final in.handyman.raven.lambda.doa.audit.StatementExecutionAudit audit =
                in.handyman.raven.lambda.doa.audit.StatementExecutionAudit.builder()
                        .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                        .actionId(actionExecutionAudit.getActionId())
                        .timeTaken((double) ChronoUnit.SECONDS.between(startTime, LocalDateTime.now()))
                        .rowsProcessed(processedEntity.size())
                        .statementContent("OptimizedCoproProcessor consumer for " + actionExecutionAudit.getActionName())
                        .build();
        addAudit(audit, startTime);
    }

    private void addAudit(final in.handyman.raven.lambda.doa.audit.StatementExecutionAudit audit,
                          final LocalDateTime startTime) {
        audit.setCreatedBy(actionExecutionAudit.getCreatedBy());
        audit.setLastModifiedBy(actionExecutionAudit.getLastModifiedBy());
        audit.setCreatedDate(startTime);
        audit.setLastModifiedDate(LocalDateTime.now());
        in.handyman.raven.actor.HandymanActorSystemAccess.insert(audit);
    }

    private void insertException(String message, Exception e) {
        HandymanException handymanException = new HandymanException(e);
        HandymanException.insertException(message, handymanException, actionExecutionAudit);
    }

    private void shutdownExecutors() {
        processingExecutor.shutdown();
        databaseExecutor.shutdown();
        try {
            if (!processingExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                processingExecutor.shutdownNow();
            }
            if (!databaseExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                databaseExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            processingExecutor.shutdownNow();
            databaseExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}