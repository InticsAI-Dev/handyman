package in.handyman.raven.lib.azure.adapters;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import in.handyman.raven.lib.encryption.SecurityEngine;
import in.handyman.raven.lib.encryption.impl.AESEncryptionImpl;
import in.handyman.raven.lib.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.util.PropertyHandler;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
public class AzureAuthTokenSession {

    private static final AzureAuthTokenSession INSTANCE;

    private static final Duration TOKEN_EXPIRY_THRESHOLD = Duration.ofMinutes(50);
    private static final Duration TOKEN_EXPIRY_LIMIT = Duration.ofMinutes(60);

    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final String databaseUrl;
    private final String tokenScope;
    private final String azureUserName;
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;

    private String sessionToken;
    private Instant tokenCreatedDate;
    private String tokenCreatedBy;

    // **Singleton Initialization Using Static Block**
    static {
        INSTANCE = new AzureAuthTokenSession(
                PropertyHandler.get("azure.identity.tenantId"),
                PropertyHandler.get("azure.identity.clientId"),
                PropertyHandler.get("azure.identity.clientSecret"),
                PropertyHandler.get("azure.database.url"),
                PropertyHandler.get("azure.token.scope"),
                PropertyHandler.get("raven.db.user"),
                PropertyHandler.get("raven.db.url"),
                PropertyHandler.get("raven.db.user"),
                PropertyHandler.get("DB_PASSWORD")
        );
    }

    private AzureAuthTokenSession(String tenantId, String clientId, String clientSecret, String databaseUrl,
                                  String tokenScope, String azureUserName, String jdbcUrl, String dbUser, String dbPassword) {
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.databaseUrl = databaseUrl;
        this.tokenScope = tokenScope;
        this.azureUserName = azureUserName;
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public static AzureAuthTokenSession getInstance() {
        return INSTANCE;
    }

    /**
     * Retrieves the current session token. If the token is missing or expired, triggers an update.
     */
    public synchronized Optional<String> getToken() {
        log.info("Entering getToken() method");

        if (sessionToken == null || isTokenExpired(tokenCreatedDate)) {
            log.info("Token is missing or expired. Triggering token update...");
            updateToken();
        }

        log.info("Exiting getToken() method with token status: {}", sessionToken != null ? "Valid" : "Expired");
        return Optional.ofNullable(sessionToken);
    }

    /**
     * Updates the session token.
     *
     * **Execution Sequence:**
     * 1. Acquire a new Azure token.
     * 2. Open a database connection.
     * 3. Move existing token from `azure_auth_token_session` to `azure_auth_token_session_audit`.
     * 4. Store or update the new token in `azure_auth_token_session`.
     */
    private synchronized void updateToken() {
        log.info("Entering updateToken() method");

        // **Step 1: Acquire a New Token**
        String newToken = acquireNewToken().getToken();
        if (newToken == null || newToken.isEmpty()) {
            log.error("Failed to acquire a valid Azure token");
            return;
        }
        log.info("New token acquired successfully.");

        // **Step 2: Open database connection for token update**
        try (Connection dbConnection = DriverManager.getConnection(jdbcUrl, dbUser, newToken)) {

            // **Step 3: Move existing token to audit table before updating**
            moveToAuditTable(dbConnection);

            // **Step 4: Store or update the new token in the active session table**
            sessionToken = newToken;
            tokenCreatedDate = Instant.now();
            tokenCreatedBy = getCallerMethod();
            storeNewToken(dbConnection, sessionToken, tokenCreatedBy);

            log.info("Token successfully updated by: {}", tokenCreatedBy);

        } catch (SQLException e) {
            log.error("Database connection error while updating Azure token.", e);
            throw new RuntimeException("Failed to manage Azure token storage", e);
        }

        log.info("Exiting updateToken() method");
    }


    /**
     * Moves the old token from `azure_auth_token_session` to `azure_auth_token_session_audit`
     * before acquiring a new one.
     */
    private void moveToAuditTable(Connection dbConnection) {
        log.info("Entering moveToAuditTable() method");

        String moveQuery = "INSERT INTO audit.azure_auth_token_session_audit (session_token, token_created_date, expiration_date, token_created_by) " +
                "SELECT session_token, token_created_date, token_created_date + INTERVAL '60 minutes', token_created_by FROM audit.azure_auth_token_session";

        try (PreparedStatement ps = dbConnection.prepareStatement(moveQuery)) {
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                log.info("Old token moved to audit table.");
            }
        } catch (SQLException e) {
            log.error("Failed to move token to audit table.", e);
        }

        log.info("Exiting moveToAuditTable() method");
    }

    /**
     * Stores a new token in `azure_auth_token_session`. Ensures only one row exists.
     */
    private void storeNewToken(Connection dbConnection, String token, String createdBy) {
        log.info("Entering storeNewToken() method");

        String updateQuery = "UPDATE audit.azure_auth_token_session SET session_token = ?, token_created_date = ?, token_created_by = ?";

        try (PreparedStatement updatePs = dbConnection.prepareStatement(updateQuery)) {
            updatePs.setString(1, "");
            updatePs.setTimestamp(2, Timestamp.from(Instant.now()));
            updatePs.setString(3, createdBy);

            int rowsUpdated = updatePs.executeUpdate();
            if (rowsUpdated == 0) {
                String insertQuery = "INSERT INTO audit.azure_auth_token_session (session_token, token_created_date, token_created_by) VALUES (?, ?, ?)";
                try (PreparedStatement insertPs = dbConnection.prepareStatement(insertQuery)) {
                    insertPs.setString(1, "");
                    insertPs.setTimestamp(2, Timestamp.from(Instant.now()));
                    insertPs.setString(3, createdBy);
                    insertPs.executeUpdate();
                }
            }

            log.info("New token stored in database.");

        } catch (SQLException e) {
            log.error("Failed to store new Azure token.", e);
        }

        log.info("Exiting storeNewToken() method");
    }

    private boolean isTokenExpired(Instant createdTime) {
        return createdTime == null || Instant.now().isAfter(createdTime.plus(TOKEN_EXPIRY_LIMIT));
    }
    private AccessToken acquireNewToken() {
        log.info("Entering acquireNewToken() method");

        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();

        TokenRequestContext tokenRequestContext = new TokenRequestContext().addScopes(tokenScope);
        AccessToken token = clientSecretCredential.getToken(tokenRequestContext).block();

        log.info("Exiting acquireNewToken() method");
        return token;
    }

    private String getCallerMethod() {
        return StackWalker.getInstance().walk(frames -> frames.skip(2).findFirst()
                .map(frame -> frame.getClassName() + "." + frame.getMethodName())
                .orElse("Unknown"));
    }
}
