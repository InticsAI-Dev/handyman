package in.handyman.raven.lib.encryption.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lib.encryption.InticsDataEncryptionApi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

public class ProtegrityApiEncryptionImpl implements InticsDataEncryptionApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtegrityApiEncryptionImpl.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient();
    private final String protegrityEncApiUrl;
    private final String protegrityDecApiUrl;

    public ProtegrityApiEncryptionImpl(String encryptionUrl, String decryptionUrl) {
        this.protegrityEncApiUrl = encryptionUrl;
        this.protegrityDecApiUrl = decryptionUrl;
    }

    @Override
    public String encrypt(String inputToken, String encryptionPolicy, String sorItem) throws HandymanException {
        if (inputToken == null || inputToken.isBlank()) {
            LOGGER.warn("Encryption skipped: inputToken is null or blank for sorItem: {}", sorItem);
            return inputToken;
        }

        LOGGER.info("Encrypting data for sorItem: {}, encryptionPolicy: {}", sorItem, encryptionPolicy);
        return callProtegrityApi(inputToken, encryptionPolicy, sorItem, protegrityEncApiUrl);
    }

    @Override
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException {
        if (encryptedToken == null || encryptedToken.isBlank()) {
            LOGGER.warn("Decryption skipped: encryptedToken is null or blank for sorItem: {}", sorItem);
            return encryptedToken;
        }

        LOGGER.info("Decrypting data for sorItem: {}, encryptionPolicy: {}", sorItem, encryptionPolicy);
        return callProtegrityApi(encryptedToken, encryptionPolicy, sorItem, protegrityDecApiUrl);
    }

    @Override
    public String getEncryptionMethod() {
        LOGGER.info("Returning encryption method: PROTEGRITY_API_ENC");
        return "PROTEGRITY_API_ENC";
    }

    private String callProtegrityApi(String value, String policy, String key, String endpoint) throws HandymanException {
        try {
            LOGGER.info("Calling Protegrity API at {} for key: {}", endpoint, key);

            String jsonPayload = objectMapper.writeValueAsString(Collections.singletonList(
                    new EncryptionRequest(policy, value, key)
            ));
            LOGGER.debug("Protegrity API request payload: {}", jsonPayload);

            RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    LOGGER.error("Protegrity API error for key {}: {} (Code: {})",
                            key, response.message(), response.code());
                    throw new HandymanException("Protegrity API error: " + response.message());
                }

                String responseBody = response.body().string();
                LOGGER.debug("Protegrity API response: {}", responseBody);

                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                String encryptedValue = jsonResponse.get(0).get("value").asText();
                LOGGER.info("Protegrity API call successful for key: {}", key);
                return encryptedValue;
            }

        } catch (IOException e) {
            LOGGER.error("Error calling Protegrity API for key {}: {}", key, e.getMessage(), e);
            throw new HandymanException("Error calling Protegrity API: " + e.getMessage(), e);
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @JsonInclude(JsonInclude.Include.NON_EMPTY)  // Ignore empty fields
    static class EncryptionRequest {
        public String policy;
        public String value;
        public String key;
    }
}
