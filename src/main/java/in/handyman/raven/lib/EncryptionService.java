package in.handyman.raven.lib;

import okhttp3.*;

import java.io.IOException;
import java.util.Objects;

public class EncryptionService {

    private static final String encryptionServiceUrl = "http://192.168.10.240:10001/copro-utils/data-security/encrypt";
    private final OkHttpClient httpClient = new OkHttpClient();

    /**
     * This method calls the encryption API to encrypt the data using query parameters.
     *
     * @param plaintext The original data in JSON format that needs to be encrypted.
     * @return The encrypted data returned by the encryption service.
     * @throws IOException if there's an issue with the request or response.
     */
    public String encryptData(String plaintext) throws IOException {
        // Build the URL with the plaintext as a query parameter
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(encryptionServiceUrl)).newBuilder();
        urlBuilder.addQueryParameter("plaintext", plaintext);  // Add the plaintext as a query parameter

        // Since FastAPI expects POST, send a POST request with an empty body and query params
        RequestBody emptyBody = RequestBody.create(new byte[0], null);

        // Build the request to the encryption API
        Request request = new Request.Builder().url(urlBuilder.build()).post(emptyBody).build();  // Use POST

        // Execute the request and handle the response
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return Objects.requireNonNull(response.body()).string();  // Get the encrypted data
            } else {
                throw new IOException("Encryption service failed. Response code: " + response.code());
            }
        }
    }
}
