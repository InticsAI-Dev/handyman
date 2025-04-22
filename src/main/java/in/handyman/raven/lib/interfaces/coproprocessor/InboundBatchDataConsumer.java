package in.handyman.raven.lib.interfaces.coproprocessor;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import lombok.extern.slf4j.Slf4j;
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

public class InboundBatchDataConsumer<I, O extends CoproProcessor.Entity> implements Callable<Void> {

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
    private final Jdbi jdbi;
    private final Logger logger;

    public InboundBatchDataConsumer(String insertSql, Integer writeBatchSize,
                                    CoproProcessor.ConsumerProcess<I, O> callable, Predicate<I> tPredicate,
                                    LocalDateTime startTime, CountDownLatch countDownLatch, BlockingQueue<I> queue,
                                    AtomicInteger nodeCount, int nodeSize, ActionExecutionAudit actionExecutionAudit,
                                    List<URL> nodes, Jdbi jdbi, Logger logger) {
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
        this.jdbi = jdbi;
        this.logger = logger;
    }

    @Override
    public Void call() throws Exception {
        final List<O> processedEntity = new ArrayList<>();
        try {
            while (true) {
                try {
                    final I take = queue.take();
                    if (tPredicate.test(take)) {
                        final int index = nodeCount.incrementAndGet() % nodeSize;//Round robin
                        takeAndAddRowsBeforeDbFlush(callable, index, take, processedEntity);
                        writeBatchToDBOnCondition(insertSql, writeBatchSize, processedEntity, startTime);
                    } else {
                        logger.info("Breaking the consumer");
                        queue.add(take);
                        break;
                    }
                } catch (InterruptedException e) {
                    logger.error("Error at Consumer thread", e);
                    throw new HandymanException("Error at Consumer thread", e, actionExecutionAudit);
                } catch (Exception e) {
                    logger.error("Error at Consumer Process", e);
                    throw new HandymanException("Error at Consumer Process", e, actionExecutionAudit);
                }
            }
            checkProcessedEntitySizeForPendingQueue(insertSql, processedEntity, startTime);
        } catch (Exception e) {
            logger.error("Final persistence failed", e);
        } finally {
            logger.info("Consumer {} completed the process and persisted {} rows", countDownLatch.getCount(), nodeCount.get());
            countDownLatch.countDown();
        }
        return null;
    }

    private void takeAndAddRowsBeforeDbFlush(CoproProcessor.ConsumerProcess<I, O> callable, int index, I take, List<O> processedEntity) {
        final List<O> results = new ArrayList<>();
        try {
            int nodesSize = nodes.size();
            logger.info("Nodes size {} and index value {}", nodesSize, index);
            if (nodesSize != index) {
                URL endpoint = nodes.get(index);
                final List<O> list = callable.process(endpoint, take);
                results.addAll(list);
            }
        } catch (Exception e) {
            logger.error("Error in callable process in consumer", e);
        }
        processedEntity.addAll(results);
    }

    private void writeBatchToDBOnCondition(String insertSql, Integer writeBatchSize, List<O> processedEntity, LocalDateTime startTime) {
        if (nodeCount.get() % writeBatchSize == 0) {
            jdbi.useTransaction(handle -> {
                usePreparedBatchToFlush(insertSql, handle, processedEntity);
            });
            insertRowsProcessedIntoStatementAudit(startTime, processedEntity);
            processedEntity.clear();
        }
    }

    private void usePreparedBatchToFlush(String insertSql, Handle handle, List<O> processedEntity) {
        final PreparedBatch preparedBatch = handle.prepareBatch(insertSql);
        iterateAndAddToBatch(processedEntity, preparedBatch);

        try {
            int[] execute = preparedBatch.execute();
            logger.info("Consumer persisted {}", execute);
        } catch (Exception e) {
            logger.error("exception in prepared batch {}", e);
        }
    }

    private void iterateAndAddToBatch(List<O> processedEntity, PreparedBatch preparedBatch) {
        for (final O output : processedEntity) {
            addRowsToBatch(output, preparedBatch);
            preparedBatch.add();
            logger.info("prepared batch insert query input entity size {} ", processedEntity.size());
        }
    }

    private static <O extends CoproProcessor.Entity> void addRowsToBatch(O output, PreparedBatch preparedBatch) {
        final List<Object> rowData = output.getRowData();
        for (int i = 0; i < rowData.size(); i++) {
            preparedBatch.bind(i, rowData.get(i));
        }
    }

    private void checkProcessedEntitySizeForPendingQueue(String insertSql, List<O> processedEntity, LocalDateTime startTime) {
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
                    logger.error("Error in executing prepared statement {}", e.getMessage());
                    throw new HandymanException("Error in executing prepared statement ", e, actionExecutionAudit);
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