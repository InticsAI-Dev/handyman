package in.handyman.raven.lib.model.documentEyeCue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class StoreContentUploadExecutor {

    /** Thread pool size (tune based on expected concurrency) */
    private static final int THREAD_POOL_SIZE = 30;

    /** Singleton ExecutorService created once when the class is loaded */
    private static final ExecutorService EXECUTOR;

    static {
        EXECUTOR = Executors.newFixedThreadPool(THREAD_POOL_SIZE, new StoreUploadThreadFactory());
    }

    /** Private constructor to prevent instantiation */
    private StoreContentUploadExecutor() {
        // Prevent instantiation
    }

    /** ✅ Accessor method to get the shared executor */
    public static ExecutorService getInstance() {
        return EXECUTOR;
    }

    /** ✅ Graceful shutdown method (should be called during application shutdown) */
    public static void shutdown() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(30, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /** ✅ Custom thread factory for consistent naming */
    private static class StoreUploadThreadFactory implements ThreadFactory {
        private int count = 1;

        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("store-upload-thread-" + count++);
            t.setDaemon(true); // Optional: JVM can exit without waiting
            return t;
        }
    }
}
