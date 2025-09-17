package in.handyman.raven.lib.interfaces.coproprocessor;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InboundBatchDataConsumer<I, O extends CoproProcessor.Entity> implements Callable<Void> {

    public static final String COPRO_PROCESSOR_RETRY_FAILED_FILES = "copro.processor.retry.failed.files";
    public static final String COPRO_PROCESSOR_RETRY_FAILED_FILES_MAX_ATTEMPTS = "copro.processor.retry.failed.files.max.attempts";
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

    public InboundBatchDataConsumer(String insertSql, Integer writeBatchSize,
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
    }

    @Override
    public Void call() throws Exception {
        Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
        logger.info("Consumer {} started processing with node count {}", countDownLatch.getCount(), nodeCount.get());
        final List<O> processedEntity = new ArrayList<>();
        try {
            while (true) {
                try {
                    final I take = queue.take();
                    if (tPredicate.test(take)) {
                        final int index = nodeCount.incrementAndGet() % nodeSize;//Round robin
                        takeAndAddRowsBeforeDbFlush(callable, index, take, processedEntity, jdbi);
                        writeBatchToDBOnCondition(insertSql, writeBatchSize, processedEntity, startTime, jdbi);
                    } else {
                        logger.info("Breaking the consumer");
                        queue.add(take);
                        break;
                    }
                } catch (InterruptedException e) {

                    HandymanException handymanException=new HandymanException(e);
                    HandymanException.insertException("Error at Consumer thread", handymanException, actionExecutionAudit);
                } catch (Exception e) {

                    HandymanException handymanException=new HandymanException(e);
                    HandymanException.insertException("Error at Consumer Process", handymanException, actionExecutionAudit);
                }
            }
            checkProcessedEntitySizeForPendingQueue(insertSql, processedEntity, startTime, jdbi);
        } catch (Exception e) {

            HandymanException handymanException=new HandymanException(e);
            HandymanException.insertException("Final persistence failed ", handymanException, actionExecutionAudit);

        } finally {
            logger.info("Consumer {} completed the process and persisted {} rows", countDownLatch.getCount(), nodeCount.get());
            countDownLatch.countDown();
        }
        return null;
    }

    private void takeAndAddRowsBeforeDbFlush(CoproProcessor.ConsumerProcess<I, O> callable, int index, I take, List<O> processedEntity, Jdbi jdbi) {
        final List<O> results = new ArrayList<>();
        try {
            int nodesSize = nodes.size();
            logger.info("Nodes size {} and index value {}", nodesSize, index);
            if (nodesSize != index) {
                URL endpoint = nodes.get(index);
                final List<O> initialResults = callable.process(endpoint, take);
                String retryFailedFilesActivator = actionExecutionAudit.getContext().getOrDefault(COPRO_PROCESSOR_RETRY_FAILED_FILES, "false");
                if ("true".equals(retryFailedFilesActivator)) {
                    int retryFailedFilesMaxAttempts = Integer.parseInt(actionExecutionAudit.getContext().getOrDefault(COPRO_PROCESSOR_RETRY_FAILED_FILES_MAX_ATTEMPTS, "3"));
                    final List<O> finalResults = retryFailedFiles(initialResults, callable, endpoint, take, retryFailedFilesMaxAttempts, jdbi);
                    results.addAll(finalResults);
                } else {
                    results.addAll(initialResults);
                }

            }
        } catch (Exception e) {
            HandymanException handymanException=new HandymanException(e);
            HandymanException.insertException("Error in callable process in consumer ", handymanException, actionExecutionAudit);

        }
        processedEntity.addAll(results);
    }

    private List<O> retryFailedFiles(List<O> resultSet, CoproProcessor.ConsumerProcess<I, O> callable, URL endpoint, I take, int maxAttempts, Jdbi jdbi) {
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

    private List<O> retryWithAttempts(CoproProcessor.ConsumerProcess<I, O> callable, URL endpoint, I take, int maxAttempts, Jdbi jdbi) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                List<O> retryOutput = callable.process(endpoint, take);
                if (hasSuccess(retryOutput)) return retryOutput;
                insertFailedCoproProcess(take, retryOutput, attempt, jdbi);
            } catch (Exception e) {

                HandymanException handymanException=new HandymanException(e);
                HandymanException.insertException("Error in callable process in consumer ", handymanException, actionExecutionAudit);
            }
        }
        return null;
    }

    private boolean hasSuccess(List<O> retryOutput) {
        return retryOutput.stream()
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
            // Final failure, include in result
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


    private void writeBatchToDBOnCondition(String insertSql, Integer writeBatchSize, List<O> processedEntity, LocalDateTime startTime,Jdbi jdbi) {
        if (nodeCount.get() % writeBatchSize == 0) {
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
                HandymanException exception=new HandymanException(e);
                HandymanException.insertException("Error during batch insert or audit logging. Batch size: "+writeBatchSize+", Entities: "+processedEntity.size()+", StartTime: "+startTime, exception, actionExecutionAudit);
            }
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

    private void checkProcessedEntitySizeForPendingQueue(String insertSql, List<O> processedEntity, LocalDateTime startTime, Jdbi jdbi) {
        if (!processedEntity.isEmpty()) {
            jdbi.useTransaction(handle -> {
                int rowCount = 0;
                final Connection connection = handle.getConnection();
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                    for (final O output : processedEntity) {
                        final List<Object> rowData = output.getRowData();
                        for (int i = 1; i <= rowData.size(); i++) {
                            preparedStatement.setObject(i, rowData.get(i - 1));
                        }
                        preparedStatement.addBatch();
                    }
                    int[] array = preparedStatement.executeBatch();
                    rowCount = (int) Arrays.stream(array).count();
                    insertRowsProcessedIntoStatementAudit(startTime, processedEntity);
                } catch (Exception e) {
                    HandymanException handymanException=new HandymanException(e);
                    HandymanException.insertException("Error in executing prepared statement ", handymanException, actionExecutionAudit);
                }
                logger.info("Consumer persisted {}", rowCount);
            });
        }
    }

    private void insertRowsProcessedIntoStatementAudit(LocalDateTime startTime, List<O> processedEntity) {
        final in.handyman.raven.lambda.doa.audit.StatementExecutionAudit audit = in.handyman.raven.lambda.doa.audit.StatementExecutionAudit.builder()
                .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                .actionId(actionExecutionAudit.getActionId())
                .timeTaken((double) ChronoUnit.SECONDS.between(startTime, LocalDateTime.now()))
                .rowsProcessed(processedEntity.size())
                .statementContent("CoproProcessor consumer for " + actionExecutionAudit.getActionName())
                .build();
        addAudit(audit, startTime);
    }

    private void addAudit(final in.handyman.raven.lambda.doa.audit.StatementExecutionAudit audit, final LocalDateTime startTime) {
        audit.setCreatedBy(actionExecutionAudit.getCreatedBy());
        audit.setLastModifiedBy(actionExecutionAudit.getLastModifiedBy());
        audit.setCreatedDate(startTime);
        audit.setLastModifiedDate(LocalDateTime.now());
        in.handyman.raven.actor.HandymanActorSystemAccess.insert(audit);
    }
}