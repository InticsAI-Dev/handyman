package in.handyman.raven.lib;

import java.util.concurrent.*;

public class CoproProcessorConsumerTest {

    public static void main(String[] args) {
        new CoproProcessorConsumerTest().getConsumerModern();
    }

    void getConsumerModern() {
        Integer consumerCount = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(consumerCount);

        ThreadFactory namedThreadFactory = r -> {
            Thread t = new Thread(r);
            t.setName("copro-consumer-" + t.getId());
            t.setDaemon(false);
            return t;
        };

        ExecutorService executorService = new ThreadPoolExecutor(
                consumerCount,               // core pool size
                consumerCount,               // max pool size
                120L, TimeUnit.SECONDS,      // keep-alive time
                new LinkedBlockingQueue<>(), // no task backlog
                namedThreadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        for (int i = 0; i < consumerCount; i++) {
            int taskId = i + 1;
            executorService.submit(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + " is processing task ID " + taskId);
                    // Simulate work
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Task interrupted: " + e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Interrupted while waiting for consumers to finish: " + e);
        } finally {
            System.out.println("Shutting down consumer executor service");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(2, TimeUnit.MINUTES)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
