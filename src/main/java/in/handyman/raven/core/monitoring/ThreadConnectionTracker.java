package in.handyman.raven.core.monitoring;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

@Component
public class ThreadConnectionTracker {

    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    @Scheduled(fixedDelay = 60000) // every 60 seconds
    public void trackThreads() {
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        for (ThreadInfo threadInfo : threadInfos) {
            if (isThreadOfInterest(threadInfo)) {
                System.out.println("Thread of interest detected: " + threadInfo.getThreadName());
                System.out.println("State: " + threadInfo.getThreadState());
                System.out.println("Stack Trace:");
                for (StackTraceElement element : threadInfo.getStackTrace()) {
                    System.out.println("\t" + element);
                }
            }
        }
    }

    private boolean isThreadOfInterest(ThreadInfo threadInfo) {
        // Example: Look for threads in WAITING, TIMED_WAITING or BLOCKED state
        if (threadInfo.getThreadState() == Thread.State.WAITING ||
                threadInfo.getThreadState() == Thread.State.TIMED_WAITING ||
                threadInfo.getThreadState() == Thread.State.BLOCKED) {
            // Additional filter: check for JDBC or HTTP keywords in stack trace
            for (StackTraceElement element : threadInfo.getStackTrace()) {
                if (element.getClassName().contains("jdbc") ||
                        element.getClassName().contains("http") ||
                        element.getClassName().contains("okhttp") ||
                        element.getClassName().contains("apache")) {
                    return true;
                }
            }
        }
        return false;
    }
}

