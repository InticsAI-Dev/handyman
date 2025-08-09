package in.handyman.raven.lib.model.common.copro;

import in.handyman.raven.exception.HandymanException;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class CoproExecutorFactory {

    public enum ThreadType {
        VIRTUAL_THREAD,
        FIXED_THREAD,
        WORK_STEALING_POOL
    }

    public static class CoproExecutor {

        @Getter
        private final ExecutorService executorService;
        private final Semaphore concurrencyLimiter;

        public CoproExecutor(ExecutorService executorService, Semaphore concurrencyLimiter) {
            this.executorService = executorService;
            this.concurrencyLimiter = concurrencyLimiter;
        }

        /**
         * Submit a Callable task. This will acquire a permit (if concurrencyLimiter != null),
         * run the callable.call(), and release the permit in finally.
         *
         * Note: we run the callable inside a Runnable wrapper so we can control acquiring/releasing.
         */
        public void submit(Callable<Void> taskCallable, Logger logger, int consumerIndex) {
            Runnable wrappedTask = () -> {
                if (concurrencyLimiter != null) {
                    try {
                        concurrencyLimiter.acquire();
                        logger.debug("Consumer {} acquired permit", consumerIndex);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.error("Consumer {} interrupted during semaphore acquire", consumerIndex, e);
                        return;
                    }
                }

                try {
                    try {
                        taskCallable.call();
                    } catch (Exception ex) {
                        logger.error("Consumer {} callable threw exception", consumerIndex, ex);
                        throw new HandymanException(ex);
                    }
                } finally {
                    if (concurrencyLimiter != null) {
                        concurrencyLimiter.release();
                        logger.debug("Consumer {} released permit", consumerIndex);
                    }
                }
            };

            executorService.submit(wrappedTask);
            logger.info("Consumer {} submitted to executor", consumerIndex);
        }
    }

    public static CoproExecutor createExecutor(
            ThreadType threadType,
            int consumerCount,
            boolean virtualThreadConcurrencyLimit,
            Logger logger
    ) {
        ExecutorService executorService;
        Semaphore concurrencyLimiter = null;

        switch (threadType) {
            case VIRTUAL_THREAD -> {
                executorService = Executors.newVirtualThreadPerTaskExecutor();
                if (virtualThreadConcurrencyLimit) {
                    concurrencyLimiter = new Semaphore(consumerCount);
                    logger.info("Virtual thread pool created with concurrency limit {}", consumerCount);
                } else {
                    logger.info("Virtual thread pool created with NO concurrency limit");
                }
            }
            case FIXED_THREAD -> {
                executorService = Executors.newFixedThreadPool(consumerCount);
                logger.info("Fixed thread pool created with size {}", consumerCount);
            }
            case WORK_STEALING_POOL -> {
                executorService = Executors.newWorkStealingPool();
                logger.info("Work stealing pool created");
            }
            default -> {
                executorService = Executors.newWorkStealingPool();
                logger.info("Work stealing pool created as default option");
            }
        }

        return new CoproExecutor(executorService, concurrencyLimiter);
    }
}