package in.handyman.raven.core.utils;

import org.jasypt.util.text.AES256TextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class ConfigEncryptionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigEncryptionUtils.class);

    private final String encryptionPassword;

    // Constructor to initialize encryption password
    public ConfigEncryptionUtils(String encryptionPassword) {
        if (encryptionPassword == null || encryptionPassword.isEmpty()) {
            LOGGER.error("Encryption password is not set");
            throw new IllegalArgumentException("Encryption password cannot be null or empty");
        }
        this.encryptionPassword = encryptionPassword;
    }

    // Method to decrypt an encrypted property
    public String decryptProperty(String encryptedValue) {
        final AES256TextEncryptor aesEncryptor = new AES256TextEncryptor();
        aesEncryptor.setPassword(encryptionPassword);

        try {
            // Check if the value starts with ENC() and decrypt
            if (encryptedValue != null && encryptedValue.startsWith("ENC(") && encryptedValue.endsWith(")")) {
                String encryptedText = encryptedValue.substring(4, encryptedValue.length() - 1);
                LOGGER.debug("Decrypting value: {}", encryptedValue);
                return aesEncryptor.decrypt(encryptedText);
            }
            return encryptedValue; // Return the original value if it's not encrypted
        } catch (Exception e) {
            LOGGER.error("Error decrypting property: {}", encryptedValue, e);
            return encryptedValue;  // Return the original value in case of failure
        }
    }

    // Method to encrypt a value and return it in ENC() format
    public String encryptProperty(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Input value cannot be null");
        }

        final AES256TextEncryptor aesEncryptor = new AES256TextEncryptor();
        aesEncryptor.setPassword(encryptionPassword);

        try {
            String encryptedText = aesEncryptor.encrypt(value);
            return "ENC(" + encryptedText + ")"; // Wrap the encrypted value with ENC()
        } catch (Exception e) {
            LOGGER.error("Error encrypting value: {}", value, e);
            return value; // Return original value if encryption fails
        }
    }

    // Static utility method for easy usage when the password is set through an environment variable
    public static ConfigEncryptionUtils fromEnv() {
        String encryptionPassword = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        if (encryptionPassword == null || encryptionPassword.isEmpty()) {
            LOGGER.error("JASYPT_ENCRYPTOR_PASSWORD environment variable is not set");
            throw new IllegalArgumentException("Encryption password is not available");
        }
        return new ConfigEncryptionUtils(encryptionPassword);
    }

    // Static helper method to wrap a string with ENC() for encryption
    public String encryptString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Input value cannot be null");
        }
        return "ENC(" + value + ")";
    }
}