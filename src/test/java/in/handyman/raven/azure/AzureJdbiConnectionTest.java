package in.handyman.raven.azure;

import org.junit.jupiter.api.Test;

public class AzureJdbiConnectionTest {


    @Test
    void testConnectionAzure() {
        String mockDatabaseUrl = "jdbc:sqlserver://mock.database.windows.net;databaseName=mydb";
        String mockTenantId = "jdbc:sqlserver://mock.database.windows.net;databaseName=mydb";
        String mockClientId = "mockClientId";
        String mockClientSecret = "mockClientSecret";
        String mockTokenScope = "mockClientSecret";

//        AzureJdbiConnection connection = new AzureJdbiConnection(mockTenantId, mockClientId, mockClientSecret, mockDatabaseUrl, mockTokenScope);
//        Jdbi jdbi = connection.getAzureJdbiConnection();


    }
}
