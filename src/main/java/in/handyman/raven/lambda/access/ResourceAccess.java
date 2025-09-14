package in.handyman.raven.lambda.access;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import in.handyman.raven.core.azure.adapters.AzureConnectionException;
import in.handyman.raven.core.azure.adapters.AzureJdbiConnection;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.config.SpwResourceConfig;
import in.handyman.raven.util.PropertyHandler;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import java.util.Objects;
import java.util.Optional;

@Slf4j
public class ResourceAccess {

    public static HikariDataSource rdbmsConn(final String resourceName) {
        final SpwResourceConfig resource = ConfigAccess.getResourceConfig(resourceName);
        if (Objects.isNull(resource)) {
            log.warn("{} not found in Resource connections", resourceName);
        }
        return getHikariDataSource(resource);
    }

    private static HikariDataSource getHikariDataSource(final SpwResourceConfig resource) {
        if (Objects.isNull(resource)) {
            log.warn("not found in Resource connections");
            throw new HandymanException("Resource not found");
        }
        try {
            return createHP(resource.getResourceUrl(), resource.getDriverClass(),
                    resource.getUserName(), resource.getPassword());
        } catch (ClassNotFoundException e) {
            throw new HandymanException("Resource " + Optional.of(resource).map(SpwResourceConfig::getConfigName).orElse("not found") + " failed to connect", e);
        }
    }

    private static HikariDataSource createHP(final String url, final String driver, final String user, final String password)
            throws ClassNotFoundException {
        Class.forName(driver);
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(35000);
        config.setMaxLifetime(45000);
        config.setMaximumPoolSize(2);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return new HikariDataSource(config);
    }

    public static Jdbi rdbmsJDBIConn(final String resourceName) throws AzureConnectionException {

        final SpwResourceConfig resource = ConfigAccess.getResourceConfig(resourceName);

        String legacyResourceConnection = PropertyHandler.get("legacy.resource.connection.type");
        final int maxConnection = Integer.parseInt(PropertyHandler.get("raven.max.connection"));


        if (legacyResourceConnection.equals("AZURE")) {
            String azureDatabaseUrl = PropertyHandler.get("azure.database.url");
            String azureUserName = PropertyHandler.get("raven.db.user");

            AzureJdbiConnection connection = new AzureJdbiConnection(azureDatabaseUrl, azureUserName, maxConnection);
            return connection.getAzureJdbiConnection();
        } else if (legacyResourceConnection.equals("LEGACY")) {
            if (Objects.isNull(resource)) {
                log.warn("Resource connection not found in Resource connections {}", resourceName);
                throw new HandymanException("Resource connection name is null");
            }
            log.info("Resource connection found in Resource connections {}", resourceName);
            return resource.get();
        } else {
            throw new HandymanException("Resource connection is null check the legacy.resource.connection.type ");
        }
    }

}
