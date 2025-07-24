package in.handyman.raven.core.azure.adapters;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import java.util.Properties;

@Slf4j
public class AzureJdbiConnection {

    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final String databaseUrl;
    private final String tokenScope;
    private final String azureUserName;

    private HikariDataSource dataSource;

    public AzureJdbiConnection(String tenantId, String clientId, String clientSecret, String databaseUrl,
                               String tokenScope, String azureUserName) {
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.databaseUrl = databaseUrl;
        this.tokenScope = tokenScope;
        this.azureUserName = azureUserName;
        initDataSource(); // Initialize the Hikari pool
    }

    private void initDataSource() {
        String accessToken = AzureAuthTokenSession.getInstance().getToken().get();

        Properties props = new Properties();
        props.setProperty("accessToken", accessToken);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        config.setUsername(azureUserName);       // required even for token auth
        config.setDataSourceProperties(props);

        // Pool tuning for token lifecycle (token is valid ~1 hour)
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setIdleTimeout(10 * 60 * 1000);     // 10 minutes
        config.setMaxLifetime(50 * 60 * 1000);     // 50 minutes (before token expires)
        config.setConnectionTimeout(30_000);       // 30 seconds
        config.setValidationTimeout(5_000);        // 5 seconds
        config.setLeakDetectionThreshold(60_000);  // 1 minute

        this.dataSource = new HikariDataSource(config);
        log.info("Initialized Azure HikariCP connection pool");
    }

    public Jdbi getAzureJdbiConnection() {
        if (dataSource == null) {
            throw new IllegalStateException("Hikari DataSource is not initialized");
        }
        return Jdbi.create(dataSource);
    }
}
