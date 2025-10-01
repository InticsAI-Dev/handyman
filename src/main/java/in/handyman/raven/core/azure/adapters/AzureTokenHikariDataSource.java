package in.handyman.raven.core.azure.adapters;

import com.zaxxer.hikari.HikariDataSource;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import in.handyman.raven.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;

public class AzureTokenHikariDataSource extends HikariDataSource {

    private static AccessToken cachedToken;
    private static final Logger log = LoggerFactory.getLogger(AzureTokenHikariDataSource.class);
    private static final long EXPIRATION_BUFFER_SECONDS = Integer.parseInt(PropertyHandler.getOrDefault("azure.identity.refresh.minutes","3300"));

    // Load Azure config from PropertyHandler
    private final String azureTenantId = PropertyHandler.get("azure.identity.tenantId");
    private final String azureClientId = PropertyHandler.get("azure.identity.clientId");
    private final String azureClientSecret = PropertyHandler.get("azure.identity.clientSecret");
    private final String azureTokenScope = PropertyHandler.get("azure.token.scope");
    private final String azureUserName = PropertyHandler.get("raven.db.user");

    @Override
    public String getUsername() {
        return azureUserName;
    }

    @Override
    public String getPassword() {
        if (cachedToken == null) {
            // First time initialization
            log.info("Azure access token not initialized. Fetching token for the first time...");
            cachedToken = fetchNewToken();
        } else if (isTokenExpired(cachedToken)) {
            // Token expired or about to expire
            long remaining = getTokenRemainingMinutes(cachedToken);
            log.info("Azure access token expired ({} minutes remaining). Refreshing token...", remaining);
            cachedToken = fetchNewToken();
        } else {
            // Token still valid
            long remaining = getTokenRemainingMinutes(cachedToken);
            log.info("Azure access token is valid. Reusing cached token. Remaining minutes: {}", remaining);
        }

        return cachedToken.getToken();
    }

    private boolean isTokenExpired(AccessToken token) {
        long remainingMinutes = getTokenRemainingMinutes(token);
        log.info("Azure token expires in {} minutes", remainingMinutes);
        return remainingMinutes <= (EXPIRATION_BUFFER_SECONDS / 60);
    }

    public long getTokenRemainingMinutes(AccessToken token) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiry = token.getExpiresAt();

        Duration remaining = Duration.between(now, expiry);
        return remaining.toMinutes(); // returns remaining minutes
    }
    private AccessToken fetchNewToken() {
        try {
            ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                    .tenantId(azureTenantId)
                    .clientId(azureClientId)
                    .clientSecret(azureClientSecret)
                    .build();

            return credential.getTokenSync(new TokenRequestContext().addScopes(azureTokenScope));
        } catch (Exception e) {
            log.error("Failed to retrieve Azure access token: {}" , e.getMessage());
            throw new RuntimeException("Could not retrieve Azure access token", e);
        }
    }
}
