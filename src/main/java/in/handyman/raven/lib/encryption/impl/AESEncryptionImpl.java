package in.handyman.raven.lib.encryption.impl;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lib.encryption.InticsDataEncryptionApi;
import in.handyman.raven.util.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESEncryptionImpl implements InticsDataEncryptionApi {

    private final SecretKey secretKey;
    private static final Logger LOGGER = LoggerFactory.getLogger(AESEncryptionImpl.class);

    public AESEncryptionImpl() throws HandymanException {
        try {
            LOGGER.info("Initializing AES Encryption Implementation...");
            // Get the secret key from the properties file
            secretKey = getSecretKeyFromConfig();
            LOGGER.info("AES Encryption initialized successfully.");
        } catch (Exception e) {
            LOGGER.error("Error initializing AES algorithm: {}", e.getMessage(), e);
            throw new HandymanException("Error initializing AES algorithm.", e);
        }
    }

    private SecretKey getSecretKeyFromConfig() throws HandymanException {
        try {
            LOGGER.info("Fetching AES secret key from properties...");
            String secretKeyString = PropertyHandler.get("aes.secretKey");

            if (secretKeyString == null || secretKeyString.isEmpty()) {
                LOGGER.error("AES secret key is missing in properties file.");
                throw new HandymanException("AES secret key is not configured in the properties file.");
            }

            byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);
            LOGGER.info("AES secret key successfully retrieved and decoded.");

            if (decodedKey == null || decodedKey.length == 0) {
                LOGGER.warn("Decoded AES key is empty after Base64 decoding. Returning an empty key.");
                return new SecretKeySpec(new byte[32], "AES"); // Return an empty 256-bit key
            }

            if (decodedKey.length != 32) {
                LOGGER.error("Invalid AES-256 key length: {} bytes (expected: 32 bytes)", decodedKey.length);
                throw new HandymanException("Invalid AES-256 key length. Expected 32 bytes (256 bits).");
            }

            return new SecretKeySpec(decodedKey, "AES");

        } catch (Exception e) {
            LOGGER.error("Error decoding AES secret key: {}", e.getMessage(), e);
            throw new HandymanException("Error decoding AES secret key from config.", e);
        }
    }

    @Override
    public String encrypt(String inputToken, String encryptionPolicy, String sorItem) throws HandymanException {
        try {
            if (inputToken.isBlank()) {
                LOGGER.warn("Encryption skipped: Input token is blank.");
                return inputToken;
            }

            LOGGER.info("Encrypting data for sorItem: {}, encryptionPolicy: {}", sorItem, encryptionPolicy);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encrypted = cipher.doFinal(inputToken.getBytes());
            String encryptedString = Base64.getEncoder().encodeToString(encrypted);
            LOGGER.info("Encryption successful for sorItem: {}", sorItem);
            return encryptedString;

        } catch (Exception e) {
            LOGGER.error("Error during AES-256 encryption for sorItem: {} - {}", sorItem, e.getMessage(), e);
            throw new HandymanException("Error during AES-256 encryption.", e);
        }
    }

    @Override
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException {
        try {
            if (encryptedToken.isBlank()) {
                LOGGER.warn("Decryption skipped: Input token is blank.");
                return encryptedToken;
            }

            LOGGER.info("Decrypting data for sorItem: {}, encryptionPolicy: {}", sorItem, encryptionPolicy);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedToken));
            String decryptedString = new String(decrypted);
            LOGGER.info("Decryption successful for sorItem: {}", sorItem);
            return decryptedString;

        } catch (Exception e) {
            LOGGER.error("Error during AES-256 decryption for sorItem: {} - {}", sorItem, e.getMessage(), e);
            throw new HandymanException("Error during AES-256 decryption.", e);
        }
    }

    @Override
    public String getEncryptionMethod() {
        LOGGER.info("Returning encryption method: AES-256");
        return "AES-256";
    }
}
