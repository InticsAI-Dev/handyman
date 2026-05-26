package in.handyman.raven.core.azure.adapters;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import in.handyman.raven.core.utils.ConfigEncryptionUtils;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.config.SpwResourceConfig;
import in.handyman.raven.util.PropertyHandler;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HikariJdbiProvider {

    private static final Logger log = LoggerFactory.getLogger(HikariJdbiProvider.class);

    public static final int MIN_IDLE = Integer.parseInt(PropertyHandler.getOrDefault("azure.identity.hcp.minimum.idle", "100"));
    public static final int MAX_POOL_SIZE = Integer.parseInt(PropertyHandler.getOrDefault("azure.identity.hcp.max.pool.size", "300"));
    public static final int CONNECTION_TIMEOUT_MS = Integer.parseInt(PropertyHandler.getOrDefault("azure.identity.hcp.conn.timeout", "30000"));
    public static final int IDLE_TIMEOUT_MS = Integer.parseInt(PropertyHandler.getOrDefault("azure.identity.hcp.idle.timeout", "35000"));
    public static final int MAX_LIFETIME_MS = Integer.parseInt(PropertyHandler.getOrDefault("azure.identity.hcp.max.lifetime", "45000"));
    public static final String APPLICATION_NAME = "applicationName";
    public static final String HANDYMAN_RAVEN_APP = PropertyHandler.getOrDefault("azure.identity.hcp.application.name", "HandymanRavenApp");

    private static final long METRICS_LOG_INTERVAL_SECONDS = Long.parseLong(PropertyHandler.getOrDefault("hikari.metrics.log.interval.seconds", "60"));
    private static final boolean METRICS_LOG_ENABLED = Boolean.parseBoolean(PropertyHandler.getOrDefault("hikari.metrics.log.enabled", "true"));

    private static final String LEGACY_CONNECTION_TYPE_KEY = "legacy.resource.connection.type";
    private static final String AZURE = "AZURE";
    private static final String LEGACY = "LEGACY";
    private static final String SYSTEM_POOL_KEY = "__system__";

    private static final ConcurrentHashMap<String, HikariDataSource> POOLS = new ConcurrentHashMap<>();
    private static volatile String systemPoolName = null;
    private static volatile boolean initialized = false;
    private static ScheduledExecutorService metricsScheduler;

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        String mode = PropertyHandler.get(LEGACY_CONNECTION_TYPE_KEY);
        if (AZURE.equalsIgnoreCase(mode)) {
            initAzureSystemPool();
        } else if (LEGACY.equalsIgnoreCase(mode)) {
            initLegacySystemPool();
        } else {
            throw new HandymanException("Invalid " + LEGACY_CONNECTION_TYPE_KEY + ". Must be AZURE or LEGACY");
        }
        startMetricsScheduler();
        initialized = true;
    }

    private static void initAzureSystemPool() {
        String azureDatabaseUrl = PropertyHandler.get("raven.db.url");
        log.info("Initializing Azure system pool for {}", azureDatabaseUrl);
        HikariDataSource ds = buildAzurePool(azureDatabaseUrl);
        systemPoolName = SYSTEM_POOL_KEY;
        POOLS.put(systemPoolName, ds);
    }

    private static void initLegacySystemPool() {
        String bootstrapUrl = PropertyHandler.get("raven.db.url");
        log.info("Initializing legacy system pool. Bootstrap url: {}", bootstrapUrl);
        SpwResourceConfig systemResource = bootstrapResolveSystemResource(bootstrapUrl);
        if (systemResource == null) {
            log.warn("No matching active row in spw_resource_config for {}. Falling back to PropertyHandler credentials.", bootstrapUrl);
            HikariDataSource ds = buildLegacyPoolFromProperties();
            systemPoolName = SYSTEM_POOL_KEY;
            POOLS.put(systemPoolName, ds);
            return;
        }
        log.info("Using config_name={} as system pool", systemResource.getConfigName());
        HikariDataSource ds = buildLegacyPool(systemResource);
        systemPoolName = systemResource.getConfigName();
        POOLS.put(systemPoolName, ds);
    }

    private static SpwResourceConfig bootstrapResolveSystemResource(String bootstrapUrl) {
        String url = PropertyHandler.get("raven.db.url");
        String user = PropertyHandler.get("raven.db.user");
        String password = PropertyHandler.get("raven.db.password");
        String sql = "SELECT config_name, resource_url, user_name, password, driver_class, host, port, database_name " +
                "FROM config.spw_resource_config WHERE active = true";

        List<SpwResourceConfig> rows = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ConfigEncryptionUtils enc = ConfigEncryptionUtils.fromEnv();
            while (rs.next()) {
                SpwResourceConfig r = SpwResourceConfig.builder()
                        .configName(rs.getString("config_name"))
                        .resourceUrl(enc.decryptProperty(rs.getString("resource_url")))
                        .userName(enc.decryptProperty(rs.getString("user_name")))
                        .password(enc.decryptProperty(rs.getString("password")))
                        .driverClass(rs.getString("driver_class"))
                        .host(rs.getString("host"))
                        .port(enc.decryptProperty(rs.getString("port")))
                        .databaseName(enc.decryptProperty(rs.getString("database_name")))
                        .build();
                rows.add(r);
            }
        } catch (Exception e) {
            log.warn("Bootstrap query of spw_resource_config failed: {}. Will fall back to PropertyHandler credentials.", e.getMessage());
            return null;
        }
        return rows.stream()
                .filter(r -> Objects.equals(bootstrapUrl, r.getResourceUrl()))
                .findFirst()
                .orElse(null);
    }

    public static Jdbi getJdbi() {
        if (!initialized) {
            init();
        }
        return Jdbi.create(requirePool(systemPoolName));
    }

    public static Jdbi getJdbi(String resourceName) {
        if (resourceName == null || resourceName.isEmpty()) {
            return getJdbi();
        }
        return Jdbi.create(getDataSource(resourceName));
    }

    public static HikariDataSource getDataSource() {
        if (!initialized) {
            init();
        }
        return requirePool(systemPoolName);
    }

    public static HikariDataSource getDataSource(String resourceName) {
        if (resourceName == null || resourceName.isEmpty()) {
            return getDataSource();
        }
        if (!initialized) {
            init();
        }
        return POOLS.computeIfAbsent(resourceName, HikariJdbiProvider::buildPoolForResource);
    }

    private static HikariDataSource buildPoolForResource(String resourceName) {
        String mode = PropertyHandler.get(LEGACY_CONNECTION_TYPE_KEY);
        SpwResourceConfig resource = lookupResourceConfig(resourceName);
        if (resource == null) {
            throw new HandymanException("Resource '" + resourceName + "' not found in spw_resource_config");
        }
        log.info("Building {} pool for resource '{}' url={}", mode, resourceName, resource.getResourceUrl());
        if (AZURE.equalsIgnoreCase(mode)) {
            return buildAzurePool(resource.getResourceUrl());
        }
        return buildLegacyPool(resource);
    }

    private static SpwResourceConfig lookupResourceConfig(String name) {
        try {
            return in.handyman.raven.lambda.access.ConfigAccess.getResourceConfig(name);
        } catch (Exception e) {
            log.warn("Failed to look up resource '{}' via ConfigAccess: {}", name, e.getMessage());
            return null;
        }
    }

    private static HikariDataSource requirePool(String name) {
        HikariDataSource ds = POOLS.get(name);
        if (ds == null) {
            throw new HandymanException("Pool '" + name + "' not initialized");
        }
        return ds;
    }

    private static HikariDataSource buildAzurePool(String jdbcUrl) {
        AzureTokenHikariDataSource ds = new AzureTokenHikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        ds.setMinimumIdle(MIN_IDLE);
        ds.setMaximumPoolSize(MAX_POOL_SIZE);
        ds.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        ds.setIdleTimeout(IDLE_TIMEOUT_MS);
        ds.setMaxLifetime(MAX_LIFETIME_MS);
        ds.addDataSourceProperty(APPLICATION_NAME, HANDYMAN_RAVEN_APP);
        return ds;
    }

    private static HikariDataSource buildLegacyPool(SpwResourceConfig resource) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(resource.getResourceUrl());
        config.setUsername(resource.getUserName());
        config.setPassword(resource.getPassword());
        if (resource.getDriverClass() != null && !resource.getDriverClass().isEmpty()) {
            config.setDriverClassName(resource.getDriverClass());
        }
        config.setMinimumIdle(0);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setIdleTimeout(IDLE_TIMEOUT_MS);
        config.setMaxLifetime(MAX_LIFETIME_MS);
        config.setMaximumPoolSize(MAX_POOL_SIZE);
        config.addDataSourceProperty(APPLICATION_NAME, HANDYMAN_RAVEN_APP);
        return new HikariDataSource(config);
    }

    private static HikariDataSource buildLegacyPoolFromProperties() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(PropertyHandler.get("raven.db.url"));
        config.setUsername(PropertyHandler.get("raven.db.user"));
        config.setPassword(PropertyHandler.get("raven.db.password"));
        int max = Integer.parseInt(PropertyHandler.getOrDefault("raven.max.connection", String.valueOf(MAX_POOL_SIZE)));
        config.setMinimumIdle(0);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setIdleTimeout(IDLE_TIMEOUT_MS);
        config.setMaxLifetime(MAX_LIFETIME_MS);
        config.setMaximumPoolSize(max);
        config.addDataSourceProperty(APPLICATION_NAME, HANDYMAN_RAVEN_APP);
        return new HikariDataSource(config);
    }

    private static void startMetricsScheduler() {
        if (!METRICS_LOG_ENABLED) {
            log.info("HikariCP metrics logging is disabled via configuration");
            return;
        }
        if (metricsScheduler != null) {
            return;
        }
        metricsScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HikariCP-Metrics-Logger");
            t.setDaemon(true);
            return t;
        });
        metricsScheduler.scheduleAtFixedRate(
                HikariJdbiProvider::logHikariMetrics,
                METRICS_LOG_INTERVAL_SECONDS,
                METRICS_LOG_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
        log.info("HikariCP metrics logging scheduler started. Interval: {} seconds", METRICS_LOG_INTERVAL_SECONDS);
    }

    public static void logHikariMetrics() {
        POOLS.forEach((name, ds) -> {
            HikariPoolMXBean mx = ds.getHikariPoolMXBean();
            if (mx != null) {
                log.info("HikariCP[{}] active={} idle={} total={} awaiting={} max={}",
                        name,
                        mx.getActiveConnections(),
                        mx.getIdleConnections(),
                        mx.getTotalConnections(),
                        mx.getThreadsAwaitingConnection(),
                        MAX_POOL_SIZE);
            }
        });
    }

    public static void shutdown() {
        if (metricsScheduler != null && !metricsScheduler.isShutdown()) {
            log.info("Shutting down HikariCP metrics scheduler...");
            metricsScheduler.shutdown();
            try {
                if (!metricsScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    metricsScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                metricsScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        POOLS.forEach((name, ds) -> {
            if (!ds.isClosed()) {
                log.info("Closing HikariDataSource '{}'", name);
                ds.close();
            }
        });
        POOLS.clear();
        initialized = false;
        systemPoolName = null;
    }
}
