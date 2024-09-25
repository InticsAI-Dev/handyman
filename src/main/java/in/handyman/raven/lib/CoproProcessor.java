package in.handyman.raven.lib;

import in.handyman.raven.actor.HandymanActorSystemAccess;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.audit.StatementExecutionAudit;
import in.handyman.raven.lib.model.currency.detection.CurrencyDetectionInputQuerySet;
import in.handyman.raven.lib.model.currency.detection.CurrencyDetectionOutputQuerySet;
import in.handyman.raven.util.CommonQueryUtil;
import in.handyman.raven.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class CoproProcessor<I, O extends CoproProcessor.Entity> {

    private final BlockingQueue<I> queue;
    private final ExecutorService executorService;

    private final Class<O> outputTargetClass;
    private final Class<I> inputTargetClass;
    private final Jdbi jdbi;

    private final Logger logger;

    private final I stoppingSeed;

    private final List<URL> nodes;
    private final Integer nodeSize;

    private final AtomicInteger nodeCount = new AtomicInteger();

    private final ActionExecutionAudit actionExecutionAudit;

    public CoproProcessor(final BlockingQueue<I> queue, final Class<O> outputTargetClass,
                          final Class<I> inputTargetClass, final Jdbi jdbi, final Logger logger,
                          final I stoppingSeed, final List<URL> coproNodes,
                          final ActionExecutionAudit actionExecutionAudit) {
        this.queue = queue;
        this.inputTargetClass = inputTargetClass;
        this.stoppingSeed = stoppingSeed;
        this.nodes = coproNodes;
        this.executorService = Executors.newWorkStealingPool();
        this.outputTargetClass = outputTargetClass;
        this.jdbi = jdbi;
        this.logger = logger;
        this.actionExecutionAudit = actionExecutionAudit;
        this.nodeSize = coproNodes.size();
        final LocalDateTime startTime = LocalDateTime.now();
        if (nodeSize > 0) {
            this.logger.info("Copro processor created for copro coproNodes {}", nodeSize);
        } else {
            this.logger.info("Failed to create Copro processor due to empty copro coproNodes");
            throw new HandymanException("Failed to create Copro processor due to empty copro coproNodes");
        }
        final StatementExecutionAudit audit = StatementExecutionAudit.builder()
                .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                .actionId(actionExecutionAudit.getActionId())
                .statementContent("CoproProcessor created for " + actionExecutionAudit.getActionName())
                .build();
        addAudit(audit, startTime);
    }

    private void addAudit(final StatementExecutionAudit audit, final LocalDateTime startTime) {
        audit.setCreatedBy(actionExecutionAudit.getCreatedBy());
        audit.setLastModifiedBy(actionExecutionAudit.getLastModifiedBy());
        audit.setCreatedDate(startTime);
        audit.setLastModifiedDate(LocalDateTime.now());
        HandymanActorSystemAccess.insert(audit);
    }

    public void startProducer(final String sqlQuery, final Integer readBatchSize) {

        final LocalDateTime startTime = LocalDateTime.now();
        final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(sqlQuery);
        formattedQuery.forEach(sql -> jdbi.useTransaction(handle -> handle.createQuery(sql).mapToBean(inputTargetClass).useStream(stream -> {
            final AtomicInteger counter = new AtomicInteger();
            final Map<Integer, List<I>> partitions = stream.collect(Collectors.groupingBy(it -> counter.getAndIncrement() / readBatchSize));
            logger.info("Total no of rows created {}", counter.get());
            executorService.submit(() -> {
                try {
                    partitions.forEach((integer, ts) -> {
                        queue.addAll(ts);
                        insertRowsReadIntoStatementAudit(ts, startTime);
                        logger.info("Partition {} added to the queue", integer);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            logger.error("Error at Producer sleep", e);
                            throw new HandymanException("Error at Producer sleep", e, actionExecutionAudit);
                        }
                    });
                    logger.info("Total Partition added to the queue: {} ", partitions.size());
                    insertCompletionIntoStatementAudit(startTime);
                } finally {
                    queue.add(stoppingSeed);
                    logger.info("Added stopping seed to the queue");
                }
            });

        })));
    }

    private void insertRowsReadIntoStatementAudit(List<I> ts, LocalDateTime startTime) {
        final StatementExecutionAudit audit = StatementExecutionAudit.builder()
                .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                .actionId(actionExecutionAudit.getActionId())
                .statementContent("CoproProcessor producer for " + actionExecutionAudit.getActionName())
                .timeTaken((double) ChronoUnit.SECONDS.between(startTime, LocalDateTime.now()))
                .rowsRead(ts.size())
                .build();
        addAudit(audit, startTime);
    }

    private void insertCompletionIntoStatementAudit(LocalDateTime startTime) {
        final StatementExecutionAudit audit = StatementExecutionAudit.builder()
                .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                .actionId(actionExecutionAudit.getActionId())
                .statementContent("CoproProcessor producer completed " + actionExecutionAudit.getActionName())
                .timeTaken((double) ChronoUnit.SECONDS.between(startTime, LocalDateTime.now()))
                .build();
        addAudit(audit, startTime);
    }

    public void startConsumer(final String insertSql, final Integer consumerCount, final Integer writeBatchSize,
                              final ConsumerProcess<I, O> callable) {
        final LocalDateTime startTime = LocalDateTime.now();
        final Predicate<I> tPredicate = t -> !Objects.equals(t, stoppingSeed);
        final CountDownLatch countDownLatch = new CountDownLatch(consumerCount);
        for (int consumer = 0; consumer < consumerCount; consumer++) {
            executorService.submit(() -> {
                final List<O> processedEntity = new ArrayList<>();
                try {
                    while (true) {
                        try {
                            final I take = queue.take();
                            if (tPredicate.test(take)) {
                                final int index = nodeCount.incrementAndGet() % nodeSize;//Round robin
                                final List<O> results = new ArrayList<>();
                                try {
                                    int nodesSize = nodes.size();
                                    logger.info("Nodes size {} and index value {}", nodesSize, index);
                                    if (nodesSize != index) {
                                        final List<O> list = callable.process(nodes.get(index), take);
                                        results.addAll(list);
                                    }
                                } catch (Exception e) {
                                    logger.error("Error in callable process in consumer", e);
                                }
                                processedEntity.addAll(results);
                                if (nodeCount.get() % writeBatchSize == 0) {
                                    jdbi.useTransaction(handle -> {
                                        final PreparedBatch preparedBatch = handle.prepareBatch(insertSql);
                                        for (final O output : processedEntity) {
                                            final List<Object> rowData = output.getRowData();
                                            for (int i = 0; i < rowData.size(); i++) {
                                                preparedBatch.bind(i, rowData.get(i));

                                            }
                                            preparedBatch.add();
                                            logger.info("prepared batch insert query input entity \n {}  and \n {} ",take, processedEntity.toString());
                                        }

                                        try {
                                            int[] execute = preparedBatch.execute();
                                            logger.info("Consumer persisted {}", execute);
                                        }catch(Exception e){
                                            logger.error("exception in prepared batch {}", e);
                                        }

                                    });
                                    insertRowsProcessedIntoStatementAudit(startTime, processedEntity);
                                    processedEntity.clear();
                                }
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
            });

            logger.info("Consumer {} submitted the process", consumer);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("Consumer completed the process and persisted {} rows", nodeCount.get(), e);
        }
    }

    private void checkProcessedEntitySizeForPendingQueue(String insertSql, List<O> processedEntity, LocalDateTime startTime) {
        if (!processedEntity.isEmpty()) {
            jdbi.useTransaction(handle -> {
                int rowCount = 0;
                final Connection connection = handle.getConnection();
//                            connection.setAutoCommit(false);
                try {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                        for (final O output : processedEntity) {
                            final List<Object> rowData = output.getRowData();
                            for (int i = 1; i <= rowData.size(); i++) {
                                preparedStatement.setObject(i, rowData.get(i - 1));
                            }
                            preparedStatement.addBatch();
                        }
                        rowCount = (int) Arrays.stream(preparedStatement.executeBatch()).count();
                        insertRowsProcessedIntoStatementAudit(startTime, processedEntity);
                    }
                } catch (Exception e) {
                    logger.error("Error in executing prepared statement {}", ExceptionUtil.toString(e));
                    handle.rollback();
                    throw new HandymanException("Error in executing prepared statement ", e, actionExecutionAudit);
                } finally {
                    if (handle.isInTransaction()) {
//                                    handle.commit();
                    }
//                                handle.commit();
                }
                logger.info("Consumer persisted {}", rowCount);
            });

        }
    }

    private void insertRowsProcessedIntoStatementAudit(LocalDateTime startTime, List<O> processedEntity) {
        final StatementExecutionAudit audit = StatementExecutionAudit.builder()
                .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                .actionId(actionExecutionAudit.getActionId())
                .timeTaken((double) ChronoUnit.SECONDS.between(startTime, LocalDateTime.now()))
                .rowsProcessed(processedEntity.size())
                .statementContent("CoproProcessor consumer for " + actionExecutionAudit.getActionName())
                .build();
        addAudit(audit, startTime);
    }


    public interface ConsumerProcess<I, O extends Entity> {

        List<O> process(final URL endpoint, final I entity) throws Exception;

    }

    public interface Entity {

        List<Object> getRowData();

    }

}
