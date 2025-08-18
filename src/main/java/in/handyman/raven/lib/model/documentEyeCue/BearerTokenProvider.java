package in.handyman.raven.lib.model.documentEyeCue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.utils.ConfigEncryptionUtils;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class BearerTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(BearerTokenProvider.class);
    private static final OkHttpClient client = new OkHttpClient();
    private static final Marker MARKER = MarkerFactory.getMarker("DocumentEyeCue");

    private static final String KEY_APIGEE_TOKEN_URL = "apigee.token.url";
    private static final String KEY_APIGEE_CLIENT_ID = "apigee.client.id";
    private static final String KEY_APIGEE_CLIENT_SECRET = "apigee.client.secret";

    private static final String GRANT_TYPE = "grant_type=client_credentials";
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String ACCESS_TOKEN_FIELD = "access_token";

    public static String fetchBearerToken(ActionExecutionAudit action) {
        try {
            if (action == null || action.getContext() == null) {
                log.error("ActionExecutionAudit or its context is null – cannot fetch bearer token");
                return "";
            }

            String tokenUrl = action.getContext().get(KEY_APIGEE_TOKEN_URL);
            String clientIdEnc = action.getContext().get(KEY_APIGEE_CLIENT_ID);
            String clientSecretEnc = action.getContext().get(KEY_APIGEE_CLIENT_SECRET);

            if (tokenUrl == null || clientIdEnc == null || clientSecretEnc == null) {
                log.error("Missing Apigee token configuration in ActionExecutionAudit context");
                return "";
            }

            String clientId = ConfigEncryptionUtils.fromEnv().decryptProperty(clientIdEnc);
            String clientSecret = ConfigEncryptionUtils.fromEnv().decryptProperty(clientSecretEnc);

            String credentials = Credentials.basic(clientId, clientSecret);

            Request request = new Request.Builder()
                    .url(tokenUrl)
                    .post(RequestBody.create(GRANT_TYPE, MediaType.parse(CONTENT_TYPE)))
                    .header("Authorization", credentials)
                    .header("Content-Type", CONTENT_TYPE)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Failed to fetch bearer token: HTTP {} - {}", response.code(), response.message());
                    return "";
                }

                try (ResponseBody responseBody = response.body()) {
                    String body = (responseBody != null) ? responseBody.string() : "";
                    JsonNode json = new ObjectMapper().readTree(body);
                    return json.has(ACCESS_TOKEN_FIELD) ? json.get(ACCESS_TOKEN_FIELD).asText() : "";
                }
            }
        } catch (Exception e) {
            String errorMessage = "Error fetching bearer token from Apigee";
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(errorMessage, handymanException, action);
            log.error(MARKER, errorMessage, e);
            return "";
        }
    }
}
