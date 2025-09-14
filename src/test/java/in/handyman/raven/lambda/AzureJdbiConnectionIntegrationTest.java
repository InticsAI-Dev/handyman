package in.handyman.raven.lambda;

import in.handyman.raven.core.azure.adapters.AzureConnectionException;
import in.handyman.raven.core.azure.adapters.AzureJdbiConnection;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class AzureJdbiConnectionIntegrationTest {

    @Test
    void testAzureJdbiConnection_withAccessToken_shouldExecuteQuery() throws AzureConnectionException {
        String tenantId = "";
        String clientId = "";
        String clientSecret = "";
        String databaseUrl = ""; // e.g., jdbc:sqlserver://... or jdbc:postgresql://...
        String tokenScope = "";   // e.g., https://database.windows.net/.default
        String azureUserName = "";    // e.g., intics-psql
        int maxConnection = 0;
        AzureJdbiConnection connection = new AzureJdbiConnection(
                databaseUrl, azureUserName, maxConnection
        );

        Jdbi jdbi = connection.getAzureJdbiConnection();

        List<String> databases = jdbi.withHandle(handle ->
                handle.createQuery("SELECT current_database()")
                        .mapTo(String.class)
                        .list()
        );

        // Validate result
        assertFalse(databases.isEmpty());
        System.out.println("Connected to database: " + databases.get(0));
    }
}

