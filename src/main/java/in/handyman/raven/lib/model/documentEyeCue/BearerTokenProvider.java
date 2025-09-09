package in.handyman.raven.lib.model.documentEyeCue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final String ACCESS_TOKEN_FIELD = "access_token";
    private static final MediaType FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded");
    public static String fetchBearerToken(ActionExecutionAudit action) {
        String token = "";
        try {
            String requestBodyStr = "grant_type=client_credentials&scope=public";
            RequestBody body = RequestBody.create(requestBodyStr, FORM_URLENCODED);

            String storeContentApiKey = action.getContext().get("storecontent.api.key");
            String apigeeTokenUrl = action.getContext().get("apigee.token.url");
            String authorizationHeader =action.getContext().get("storecontent.authorization.header");


            Request request = new Request.Builder()
                    .url(apigeeTokenUrl)
                    .post(body)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Basic " + authorizationHeader)
                    .header("apikey", storeContentApiKey)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new HandymanException("Failed to fetch bearer token: HTTP "
                            + response.code() + " - " + response.message());
                }

                try (ResponseBody responseBody = response.body()) {
                    String bodyStr = (responseBody != null) ? responseBody.string() : "";
                    JsonNode json = new ObjectMapper().readTree(bodyStr);

                    if (json.has(ACCESS_TOKEN_FIELD)) {
                        token = json.get(ACCESS_TOKEN_FIELD).asText();
                    } else {
                        throw new HandymanException("Bearer token not found in response: " + bodyStr);
                    }
                }
            }
        } catch (Exception e) {
            String errorMessage = "Error fetching bearer token from Anthem OAuth";
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(errorMessage, handymanException, action);
            log.error(MARKER, errorMessage, e);
            throw handymanException;
        } finally {
            log.info(MARKER, "Bearer token fetch attempt completed. Success = {}", !token.isEmpty());
        }
        return token;
    }
}
