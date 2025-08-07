package in.handyman.raven.core.encryption.impl;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.core.encryption.InticsDataEncryptionApi;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

public class SHAEncryptionImpl implements InticsDataEncryptionApi {

    @Override
    public String encrypt(String inputToken, String encryptionPolicy, String sorItem) throws HandymanException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(inputToken.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new HandymanException("Error initializing SHA-256 algorithm.", e);
        }
    }

    @Override
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException {
        throw new HandymanException("SHA-256 is a one-way hash and cannot be decrypted.");
    }

    @Override
    public List<EncryptionRequestClass> encrypt(List<EncryptionRequestClass> requestList) throws HandymanException {
        return List.of();
    }

    @Override
    public List<EncryptionRequestClass> decrypt(List<EncryptionRequestClass> requestList) throws HandymanException {
        return List.of();
    }

    @Override
    public String getEncryptionMethod() {
        return "SHA-256";
    }
}
