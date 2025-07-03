package in.handyman.raven.lib.model.errorResponseJsonTrigger;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.util.CommonQueryUtil;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FetchingDbValues<I> {

    private final Logger logger;
    private final Jdbi jdbi;
    private final ExecutorService executorService;
    private final Class<I> inputTargetClass;
    private final LinkedBlockingQueue<I> queue;
    private final I stoppingSeed;
    private final Object actionExecutionAudit; // Replace with actual type if known

    public FetchingDbValues(Logger logger,
                            Jdbi jdbi,
                            ExecutorService executorService,
                            Class<I> inputTargetClass,
                            LinkedBlockingQueue<I> queue,
                            I stoppingSeed,
                            Object actionExecutionAudit) {
        this.logger = logger;
        this.jdbi = jdbi;
        this.executorService = executorService;
        this.inputTargetClass = inputTargetClass;
        this.queue = queue;
        this.stoppingSeed = stoppingSeed;
        this.actionExecutionAudit = actionExecutionAudit;
    }

    public List<I> fetch(String sqlQuery, Integer readBatchSize, Logger log) {
        log.info("Executing select query to fetch the rows from DB has been started");
        final LocalDateTime startTime = LocalDateTime.now();
        final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(sqlQuery);
        List<I> allFetchedRecords = new ArrayList<>();

        formattedQuery.forEach(sql -> {
            jdbi.useTransaction(handle ->
                    handle.createQuery(sql)
                            .mapToBean(inputTargetClass)
                            .useStream(stream -> {
                                final AtomicInteger counter = new AtomicInteger();
                                final List<I> records = stream.collect(Collectors.toList());

                                allFetchedRecords.addAll(records); // store for return

                                final Map<Integer, List<I>> partitions = records.stream()
                                        .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / readBatchSize));

                                logger.info("Total rows created {}", counter.get());

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
                                                throw new HandymanException("Error at Producer sleep", e);
                                            }
                                        });
                                        insertCompletionIntoStatementAudit(startTime);
                                    } finally {
                                        queue.add(stoppingSeed);
                                        logger.info("Added stopping seed to the queue");
                                    }
                                });

                            })
                    );
        });

        return allFetchedRecords;
    }

    // Stub methods to simulate your existing logic – replace with real implementations
    private void insertRowsReadIntoStatementAudit(List<I> items, LocalDateTime time) {
        // Audit logic here
    }

    private void insertCompletionIntoStatementAudit(LocalDateTime time) {
        // Completion logic here
    }
}
