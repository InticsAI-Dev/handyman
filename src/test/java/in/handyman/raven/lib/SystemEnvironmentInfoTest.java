package in.handyman.raven.lib;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Properties;

public class SystemEnvironmentInfoTest {

    public static void main(String[] args) {
        printSystemInfo();
    }

    public static void printSystemInfo() {
        System.out.println("===== SYSTEM ENVIRONMENT INFO =====");

        // OS & CPU
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        System.out.println("OS Name: " + osBean.getName());
        System.out.println("OS Version: " + osBean.getVersion());
        System.out.println("OS Architecture: " + osBean.getArch());
        System.out.println("Available Processors (Cores): " + osBean.getAvailableProcessors());

        // Java
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("Java Home: " + System.getProperty("java.home"));

        // Memory Info (JVM Heap)
        Runtime runtime = Runtime.getRuntime();
        long mb = 1024 * 1024;
        System.out.println("JVM Heap Memory (MB):");
        System.out.println("  Max Memory: " + runtime.maxMemory() / mb);
        System.out.println("  Total Memory: " + runtime.totalMemory() / mb);
        System.out.println("  Free Memory: " + runtime.freeMemory() / mb);
        System.out.println("  Used Memory: " + (runtime.totalMemory() - runtime.freeMemory()) / mb);

        // User & Runtime Info
        Properties props = System.getProperties();
        System.out.println("User: " + props.getProperty("user.name"));
        System.out.println("User Home: " + props.getProperty("user.home"));
        System.out.println("Current Dir: " + props.getProperty("user.dir"));

        // Uptime
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        System.out.println("JVM Uptime (ms): " + uptime);

        System.out.println("===================================");
    }
}