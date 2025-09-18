package in.handyman.raven.util;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.ILoggerFactory;

/**
 * Utility class to initialize logging early in the application lifecycle
 * to prevent SubstituteLogger buffer overflow issues.
 */
public class LoggingInitializer {
    
    private static volatile boolean initialized = false;
    
    /**
     * Initialize logging system early to prevent SubstituteLogger buffer overflow.
     * This should be called as early as possible in the application startup.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // Force initialization of the logger context by creating a logger
            Logger logger = LoggerFactory.getLogger(LoggingInitializer.class);
            
            // Test logging to ensure the system is working
            logger.info("Logging system initialized successfully");
            
            // Force initialization of logger factory to prevent SubstituteLogger
            ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
            if (loggerFactory != null) {
                System.out.println("Logger factory initialized: " + loggerFactory.getClass().getName());
            }
            
            initialized = true;
            
        } catch (Exception e) {
            System.err.println("Failed to initialize logging system: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Check if logging has been initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Force flush any pending log events to prevent buffer overflow
     */
    public static void flushPendingLogs() {
        try {
            // Create a logger to trigger any pending events
            Logger logger = LoggerFactory.getLogger(LoggingInitializer.class);
            logger.debug("Flushing pending log events");
        } catch (Exception e) {
            // Ignore exceptions during flush
        }
    }
}
