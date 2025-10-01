package in.handyman.raven.core.azure.adapters;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ConfigAccess;
import in.handyman.raven.lambda.doa.config.SpwResourceConfig;
import in.handyman.raven.util.PropertyHandler;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class HikariJdbiProvider {

    private static final Logger log = LoggerFactory.getLogger(HikariJdbiProvider.class);
    public static final int MIN_IDLE = Integer.parseInt(PropertyHandler.getOrDefault("azure.identity.hcp.minimum.idle","100"));
    public static final int MAX_POOL_SIZE = Integer.parseInt(PropertyHandler.getOrDefault("azure.identity.hcp.max.pool.size","300"));;
    public static final int CONNECTION_TIMEOUT_MS = Integer.parseInt(PropertyHandler.getOrDefault("azure.identity.hcp.conn.timeout","30000"));
    public static final int IDLE_TIMEOUT_MS = Integer.parseInt(PropertyHandler.getOrDefault("azure.identity.hcp.idle.timeout","35000"));;
    public static final int MAX_LIFETIME_MS = Integer.parseInt(PropertyHandler.getOrDefault("azure.identity.hcp.max.lifetime","45000"));;
    public static final String APPLICATION_NAME = "applicationName";
    public static final String HANDYMAN_RAVEN_APP = PropertyHandler.getOrDefault("azure.identity.hcp.application.name", "HandymanRavenApp");

    // Singleton HikariDataSource
    private static HikariDataSource hikariDataSource;

    // Initialize once at application startup
    public static void init() {
        String legacyResourceConnection = PropertyHandler.get("legacy.resource.connection.type");

        if ("AZURE".equalsIgnoreCase(legacyResourceConnection)) {
            log.info("Initializing Azure token-based HikariDataSource...");


            String azureDatabaseUrl = PropertyHandler.get("raven.db.url");

            AzureTokenHikariDataSource dataSource = new AzureTokenHikariDataSource();
            dataSource.setJdbcUrl(azureDatabaseUrl);
            dataSource.setMinimumIdle(MIN_IDLE);
            dataSource.setMaximumPoolSize(MAX_POOL_SIZE);
            dataSource.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
            dataSource.setIdleTimeout(IDLE_TIMEOUT_MS);
            dataSource.setMaxLifetime(MAX_LIFETIME_MS);
            dataSource.addDataSourceProperty(APPLICATION_NAME, HANDYMAN_RAVEN_APP);


            hikariDataSource = dataSource;
        } else {
            throw new HandymanException("Invalid legacy.resource.connection.type. Must be AZURE or LEGACY");
        }
    }

    // Returns Jdbi instance using already-initialized HikariDataSource
    public static Jdbi getJdbi() {
        if (hikariDataSource == null) {
            throw new HandymanException("HikariDataSource not initialized. Call HikariJdbiProvider.init() at startup.");
        }
        return Jdbi.create(hikariDataSource);
    }

    // Optional: provide HikariDataSource directly if needed
    public static HikariDataSource getDataSource() {
        if (hikariDataSource == null) {
            throw new HandymanException("HikariDataSource not initialized. Call HikariJdbiProvider.init() at startup.");
        }
        return hikariDataSource;
    }

    public static void logHikariMetrics() {
        if (hikariDataSource != null) {
            HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
            if (poolMXBean != null) {
                log.debug("HikariCP Metrics - Active Connections: {}, Idle Connections: {}, Total Connections: {}, Threads Awaiting Connection: {}",
                        poolMXBean.getActiveConnections(),
                        poolMXBean.getIdleConnections(),
                        poolMXBean.getTotalConnections(),
                        poolMXBean.getThreadsAwaitingConnection());
            } else {
                log.warn("HikariPoolMXBean is null. Cannot fetch runtime metrics.");
            }
        }
    }
}
