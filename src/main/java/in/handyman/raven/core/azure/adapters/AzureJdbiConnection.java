package in.handyman.raven.core.azure.adapters;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import in.handyman.raven.util.PropertyHandler;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

@Slf4j
public class AzureJdbiConnection {

    private final String databaseUrl;
    private final String azureUserName;
    final int maxConnection ;


    public AzureJdbiConnection(String databaseUrl, String azureUserName, int maxConnection) {
        this.databaseUrl = databaseUrl;
        this.azureUserName = azureUserName;
        this.maxConnection = maxConnection;
    }

    public Jdbi getAzureJdbiConnection() {


        Optional<String> token = AzureAuthTokenSession.getInstance().getToken();
        if (token.isPresent()) {
            log.info("Token acquired successfully.");
        } else {
            log.error("Failed to acquire token.");
            throw new AzureConnectionException("Failed to acquire Azure AD token for database connection.");

        }
        log.info("Acquired Azure AD token for database connection.");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        config.setUsername(azureUserName);
        config.setPassword(token.get());
        config.setMinimumIdle(0);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(35000);
        config.setMaxLifetime(45000);
        config.setMaximumPoolSize(maxConnection);
        HikariDataSource hikariDataSource = new HikariDataSource(config);

         return Jdbi.create(hikariDataSource);
    }
}
