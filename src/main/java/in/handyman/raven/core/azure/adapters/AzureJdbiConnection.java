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
        return HikariJdbiProvider.getJdbi();
    }
}
