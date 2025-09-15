package in.handyman.raven.lib.model.documentEyeCue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

public class BearerTokenProvider {

    private static final OkHttpClient client = new OkHttpClient();

    private static final String ACCESS_TOKEN_FIELD = "access_token";
    private static final String KEY_STORECONTENT_API_KEY = "storecontent.api.key";
    private static final String KEY_APIGEE_TOKEN_URL = "apigee.token.url";
    private static final String KEY_AUTHORIZATION_HEADER = "storecontent.authorization.header";

    public static String fetchBearerToken(ActionExecutionAudit action, Logger log, Marker MARKER) {
        String token = "";
        try {
            String storeContentApiKey = action.getContext().get(KEY_STORECONTENT_API_KEY);
            String apigeeTokenUrl = action.getContext().get(KEY_APIGEE_TOKEN_URL);
            String authorizationHeader = action.getContext().get(KEY_AUTHORIZATION_HEADER);

            RequestBody body = new FormBody.Builder()
                    .add("grant_type", "client_credentials")
                    .add("scope", "public")
                    .build();

            Request request = new Request.Builder()
                    .url(apigeeTokenUrl)
                    .post(body)
                    .header("Authorization", "Basic " + authorizationHeader)
                    .header("apikey", storeContentApiKey)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorMessage = "Failed to fetch bearer token: HTTP "
                            + response.code() + " - " + response.message();

                    try (ResponseBody errorBody = response.body()) {
                        if (errorBody != null) {
                            String errorBodyStr = errorBody.string();
                            log.error(MARKER, "Error response body");
                        }
                    } catch (Exception e) {
                        String catchMessage = "Could not read error response body";
                        log.warn(MARKER, catchMessage, e);

                        HandymanException handymanException = new HandymanException(catchMessage, e);
                        HandymanException.insertException(catchMessage, handymanException, action);
                    }

                    HandymanException handymanException = new HandymanException(errorMessage);
                    HandymanException.insertException(errorMessage, handymanException, action);
                    log.error(MARKER, errorMessage);
                    return token;
                }

                try (ResponseBody responseBody = response.body()) {
                    String bodyStr = (responseBody != null) ? responseBody.string() : "";
                    log.info(MARKER, "Success response body");

                    JsonNode json = new ObjectMapper().readTree(bodyStr);
                    if (json.has(ACCESS_TOKEN_FIELD)) {
                        token = json.get(ACCESS_TOKEN_FIELD).asText();
                        log.info(MARKER, "Successfully extracted bearer token");
                    } else {
                        String errorMessage = "Bearer token not found in response";
                        HandymanException handymanException = new HandymanException(errorMessage);
                        HandymanException.insertException(errorMessage, handymanException, action);
                        log.error(MARKER, errorMessage);
                    }
                }
            } catch (Exception e) {
                String errorMessage = "Unexpected error while fetching bearer token";
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException(errorMessage, handymanException, action);
                log.error(MARKER, errorMessage, e);
            }
        } catch (Exception e) {
            String errorMessage = "Error fetching bearer token from Anthem OAuth";
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(errorMessage, handymanException, action);
            log.error(MARKER, errorMessage, e);
        } finally {
            log.info(MARKER, "Bearer token fetch attempt completed. Success = {}", !token.isEmpty());
        }
        return token;
    }
}
