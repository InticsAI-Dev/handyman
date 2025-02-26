package in.handyman.raven.access;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import in.handyman.raven.lambda.access.ConfigAccess;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
class ConfigAccessDTest {

    @Test
    void getResourceConfig() {
        final Map<String, String> commonConfig = ConfigAccess.getCommonConfig();
        assert commonConfig != null;
        log.info(commonConfig.toString());
    }


    @Test
    void getAzureConnection(){
        String tenantId="";
        String clientId="";
        String clientSecret="";

        ClientSecretCredential clientSecretCredential=new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
        TokenRequestContext tokenRequestContext=new TokenRequestContext()
                .addScopes("");

        AccessToken accessToken=clientSecretCredential.getToken(tokenRequestContext).block();

        try (Connection connection= DriverManager.getConnection("","", accessToken.getToken())){
            if(connection != null){
                log.info("successfully connected.");
            }else{
                log.error("Failed to connect.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
}
