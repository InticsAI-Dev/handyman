package in.handyman.raven.lib.model.documentEyeCue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.utils.ConfigEncryptionUtils;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BearerTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(BearerTokenProvider.class);
    private static final OkHttpClient client = new OkHttpClient();

    public static String fetchBearerToken(ActionExecutionAudit action) {
        try {
            if (action == null || action.getContext() == null) {
                log.error("ActionExecutionAudit or its context is null – cannot fetch bearer token");
                return "";
            }

            String tokenUrl = action.getContext().get("apigee.token.url");
            String clientIdEnc = action.getContext().get("apigee.client.id");
            String clientSecretEnc = action.getContext().get("apigee.client.secret");

            if (tokenUrl == null || clientIdEnc == null || clientSecretEnc == null) {
                log.error("Missing Apigee token configuration in ActionExecutionAudit context");
                return "";
            }

            String clientId = ConfigEncryptionUtils.fromEnv().decryptProperty(clientIdEnc);
            String clientSecret = ConfigEncryptionUtils.fromEnv().decryptProperty(clientSecretEnc);

            String credentials = Credentials.basic(clientId, clientSecret);

            Request request = new Request.Builder()
                    .url(tokenUrl)
                    .post(RequestBody.create(
                            "grant_type=client_credentials",
                            MediaType.parse("application/x-www-form-urlencoded")))
                    .header("Authorization", credentials)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Failed to fetch bearer token: HTTP {} - {}", response.code(), response.message());
                    return "";
                }

                String body = response.body() != null ? response.body().string() : "";
                JsonNode json = new ObjectMapper().readTree(body);
                return json.has("access_token") ? json.get("access_token").asText() : "";
            }
        } catch (Exception e) {
            log.error("Error fetching bearer token", e);
            return "";
        }
    }
}
