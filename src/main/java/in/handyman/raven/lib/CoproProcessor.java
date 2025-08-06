package in.handyman.raven.lib;

import in.handyman.raven.actor.HandymanActorSystemAccess;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.audit.StatementExecutionAudit;
import in.handyman.raven.lib.interfaces.coproprocessor.InboundBatchDataConsumer;
import in.handyman.raven.util.CommonQueryUtil;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

public class CoproProcessor<I, O extends CoproProcessor.Entity> {

    private final BlockingQueue<I> queue;
    private ExecutorService executorService;

    private final Class<O> outputTargetClass;
    private final Class<I> inputTargetClass;
    private final String jdbiResourceName;

    private final Logger logger;

    private final I stoppingSeed;

    private final List<URL> nodes;
    private final int nodeSize;

    private final AtomicInteger nodeCount = new AtomicInteger();

    private final ActionExecutionAudit actionExecutionAudit;

    public CoproProcessor(final BlockingQueue<I> queue, final Class<O> outputTargetClass,
                          final Class<I> inputTargetClass, final String jdbiResourceName, final Logger logger,
                          final I stoppingSeed, final List<URL> coproNodes,
                          final ActionExecutionAudit actionExecutionAudit) {
        this.queue = queue;
        this.inputTargetClass = inputTargetClass;
        this.stoppingSeed = stoppingSeed;
        this.nodes = coproNodes;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.outputTargetClass = outputTargetClass;
        this.jdbiResourceName = jdbiResourceName;
        this.logger = logger;
        this.actionExecutionAudit = actionExecutionAudit;
        this.nodeSize = coproNodes.size();
        final LocalDateTime startTime = LocalDateTime.now();
        if (nodeSize > 0) {
            this.logger.info("Copro processor created for copro coproNodes {}", nodeSize);
        } else {
            this.logger.info("Failed to create Copro processor due to empty copro coproNodes");
            HandymanException handymanException=new HandymanException("Failed to create Copro processor due to empty copro coproNodes");
            HandymanException.insertException("Failed to create Copro processor due to empty copro coproNodes",handymanException,actionExecutionAudit);
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
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
        final LocalDateTime startTime = LocalDateTime.now();
        final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(sqlQuery);
        formattedQuery.forEach(sql -> jdbi.useTransaction(handle -> handle.createQuery(sql).mapToBean(inputTargetClass).useStream(stream -> {
            final AtomicInteger counter = new AtomicInteger();
            final Map<Integer, List<I>> partitions = stream.collect(Collectors.groupingBy(it -> (Integer) (counter.getAndIncrement() / readBatchSize)));
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
                            HandymanException handymanException=new HandymanException(e);
                            HandymanException.insertException("Error at Producer sleep", handymanException, actionExecutionAudit);
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
        if (actionExecutionAudit.getContext().getOrDefault("copro.processor.thread.creator", "WORK_STEALING").equalsIgnoreCase("FIXED_THREAD")) {
            executorService = Executors.newFixedThreadPool(consumerCount);
            logger.info("Copro processor created with fixed thread pool of size {}", consumerCount);
        } else {
            executorService = Executors.newWorkStealingPool();
            logger.info("Copro processor created with work stealing pool");
        }
        for (int consumer = 0; consumer < consumerCount; consumer++) {
            executorService.submit(new InboundBatchDataConsumer<>(insertSql, writeBatchSize, callable, tPredicate,
                    startTime, countDownLatch, queue, nodeCount, nodeSize, actionExecutionAudit, nodes, jdbiResourceName, logger));

            logger.info("Consumer {} submitted the process", consumer);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("Consumer completed the process and persisted {} rows", nodeCount.get(), e);
        }
    }

    public interface ConsumerProcess<I, O extends Entity> {
        List<O> process(final URL endpoint, final I entity) throws Exception;
    }

    public interface Entity {
        List<Object> getRowData();

        String getStatus();
    }
}