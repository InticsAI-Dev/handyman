package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.*;
import java.security.spec.MGF1ParameterSpec;
import java.net.URLEncoder;
import java.util.Objects;



public class CipherStreamUtil {
    private final Logger log;
    public static ActionExecutionAudit action;

    public CipherStreamUtil(Logger log, ActionExecutionAudit action) {
        this.log = log;
        CipherStreamUtil.action = action;
    }

    public static PublicKey getPublicKey(String base64PublicKey) throws Exception {
        String sanitizedKey = base64PublicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(sanitizedKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec oAepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oAepParams);

        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        int maxLength = cipher.getOutputSize(1) - 42; // 42 bytes is the overhead for OAEP with SHA-256

        if (dataBytes.length > maxLength) {
            throw new IllegalArgumentException("Data is too large for RSA encryption. Max length: " + maxLength + " bytes");
        }

        byte[] encryptedBytes = cipher.doFinal(dataBytes);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decryptDataLogic(String encryptedPrivateKey, String password, String salt, String cipherText) throws Exception {
        byte[] encryptedPrivateKeyBytes = Base64.getDecoder().decode(encryptedPrivateKey);
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        byte[] cipherTextBytes = Base64.getDecoder().decode(cipherText);

        // Step 1: Decrypt the RSA private key
        PrivateKey privateKey = decryptRsaKey(encryptedPrivateKeyBytes, password, saltBytes);

        // Step 2: Use the decrypted private key to decrypt the ciphertext
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(cipherTextBytes);

        return new String(decryptedBytes);
    }

    private static PrivateKey decryptRsaKey(byte[] encryptedPrivateKey, String password, byte[] salt) throws Exception {
        // Derive the key from password and salt
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100000, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        // Decrypt the private key
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16]; // Assuming IV is the first 16 bytes of encrypted key
        System.arraycopy(encryptedPrivateKey, 0, iv, 0, 16);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secret, ivParameterSpec);
        byte[] decryptedKeyBytes = cipher.doFinal(encryptedPrivateKey, 16, encryptedPrivateKey.length - 16);

        // Create PrivateKey object
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decryptedKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }



    public static String encryptionApi(Object jsonInput, ActionExecutionAudit action) throws Exception {
        // Step 1: Create OkHttpClient

        String encryption_activator = action.getContext().get( "encryption.activator");
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String apiUrl = action.getContext().get("apiUrl");

        OkHttpClient client = new OkHttpClient();

        String jsonString;
        if (jsonInput instanceof String) {
            jsonString = (String) jsonInput; // Directly use the string
        } else if (jsonInput instanceof File) {
            jsonString = convertFileToJson((File) jsonInput);
        }else {
            jsonString = convertObjectToJson(jsonInput); // Convert other objects to JSON
        }

        String encodedPlaintext = URLEncoder.encode(jsonString, "UTF-8");

        // Construct the URL
        String url = String.format("%s?plaintext=%s", apiUrl, encodedPlaintext);

        // when encryption is True
        if (Objects.equals("true", encryption_activator)) {
            // Step 2: Create the RequestBody from the JSON input
            RequestBody body = RequestBody.create("", JSON);
            // Step 3: Build the Request
            Request request = new Request.Builder()
                    .url(url)
                    .post(body) // Specify this as a POST request
                    .build();

            // Step 4: Send the request and get the response
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    // Return the response body in base64 format
                    return response.body().string();
                } else {
                    throw new RuntimeException("Failed to call encryption API: " + response.code());
                }
            }
        }else {
            return jsonInput.toString();
        }


    }

    // Method to convert file content to JSON string
    private static String convertFileToJson(File file) throws IOException {
            // Read the content of the file
            return new String(Files.readAllBytes(file.toPath()));
    }

    public static String convertObjectToJson(Object obj) throws Exception{

        JSONObject jsonObject = new JSONObject(obj);
            String cipherText = jsonObject.toString();
            return cipherText;

    }



    public static String decryptionApi(Object jsonInput, ActionExecutionAudit action) throws Exception {
        // Step 1: Create OkHttpClient

        String decryption_activator = action.getContext().get("decryption.activator");
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String decryptApiUrl = action.getContext().get("decryptApiUrl");

        String jsonString;
        OkHttpClient client = new OkHttpClient();
        if (jsonInput instanceof String) {
            jsonString = (String) jsonInput; // Directly use the string
        } else {
            jsonString = convertObjectToJson(jsonInput); // Convert other objects to JSON
        }



        String encodedPlaintext = URLEncoder.encode(jsonString, "UTF-8").replace("/", "%2F");

        // Construct the URL
        String url = String.format("%s?cipher_text=%s", decryptApiUrl, encodedPlaintext);

        // when encryption is True
        if (Objects.equals("true", decryption_activator)) {
            // Step 2: Create the RequestBody from the JSON input
            RequestBody body = RequestBody.create("", JSON);
            // Step 3: Build the Request
            Request request = new Request.Builder()
                    .url(url)
                    .post(body) // Specify this as a POST request
                    .build();

            // Step 4: Send the request and get the response
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    // Return the response body in base64 format
                    // Parse the response and extract 'cipherText'
                    String responseBody = response.body().string();
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonResponse = mapper.readTree(responseBody);

                    return jsonResponse.get("plainText").asText();
                } else {
                    throw new RuntimeException("Failed to call encryption API: " + response.code());
                }
            }
        } else {
            return jsonInput.toString();
        }

    }
    }