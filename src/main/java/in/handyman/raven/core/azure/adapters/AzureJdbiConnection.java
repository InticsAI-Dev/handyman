package in.handyman.raven.core.azure.adapters;

import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class AzureJdbiConnection {

    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final String databaseUrl;
    private final String tokenScope;
    private final String azureUserName;



    public AzureJdbiConnection(String tenantId, String clientId, String clientSecret, String databaseUrl,String tokenScope, String azureUserName) {
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.databaseUrl = databaseUrl;
        this.tokenScope=tokenScope;
        this.azureUserName=azureUserName;
    }

    public Jdbi getAzureJdbiConnection() {

        String token= AzureAuthTokenSession.getInstance().getToken().get();
        // Construct connection properties
        Properties connectionProps = new Properties();
        connectionProps.put("user", azureUserName);  // Can also use a specific user if applicable
        connectionProps.put("password",token );

        try {
            // Establish JDBC connection using the token
            Connection connection = DriverManager.getConnection(databaseUrl, connectionProps);

            if (connection != null) {
                log.info("Successfully connected to the database using Azure Authentication.");
            } else {
                log.error("Failed to connect to the database connection was null.");
            }

            // Returning JDBI instance configured with the connection
            return Jdbi.create(connection);
        } catch (SQLException e) {
            log.error("Failed to connect to the database error in the config.");
            throw new RuntimeException("Failed to connect to the database", e);
        }
    }
}
