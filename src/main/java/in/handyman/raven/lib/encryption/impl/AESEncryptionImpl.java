package in.handyman.raven.lib.encryption.impl;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lib.encryption.InticsDataEncryptionApi;
import in.handyman.raven.util.PropertyHandler;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESEncryptionImpl implements InticsDataEncryptionApi {

    private final SecretKey secretKey;

    public AESEncryptionImpl() throws HandymanException {
        try {
            // Get the secret key from the properties file
            secretKey = getSecretKeyFromConfig();
        } catch (Exception e) {
            throw new HandymanException("Error initializing AES algorithm.", e);
        }
    }

    private SecretKey getSecretKeyFromConfig() throws HandymanException {
        try {
            // Get the Base64 encoded secret key from the properties file
            String secretKeyString = PropertyHandler.get("aes.secretKey");

            if (secretKeyString == null || secretKeyString.isEmpty()) {
                throw new HandymanException("AES secret key is not configured in the properties file.");
            }

            // Decode the Base64 encoded key
            byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);

            // Ensure the key is 32 bytes (256 bits)
            if (decodedKey.length != 32) {
                throw new HandymanException("Invalid AES-256 key length. Expected 32 bytes (256 bits).");
            }

            return new SecretKeySpec(decodedKey, "AES");

        } catch (Exception e) {
            throw new HandymanException("Error decoding AES secret key from config.", e);
        }
    }

    @Override
    public String encrypt(String inputToken, String encryptionPolicy, String sorItem) throws HandymanException {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // ECB mode, consider CBC for more security
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encrypted = cipher.doFinal(inputToken.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new HandymanException("Error during AES-256 encryption.", e);
        }
    }

    @Override
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedToken));
            return new String(decrypted);
        } catch (Exception e) {
            throw new HandymanException("Error during AES-256 decryption.", e);
        }
    }

    @Override
    public String getEncryptionMethod() {
        return "AES-256";
    }
}
