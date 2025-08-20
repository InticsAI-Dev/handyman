package in.handyman.raven.core.encryption.impl;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.core.encryption.InticsDataEncryptionApi;

import java.util.List;

public class ProtegrityEncryptionImpl implements InticsDataEncryptionApi {

    @Override
    public String encrypt(String inputToken, String encryptionPolicy, String sorItem) throws HandymanException {
        //TODO CALL PROTEGRITY API ENCRYPTION

        return new String();
    }

    @Override
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException {
        //TODO CALL PROTEGRITY API DECRYPTION
        return new String();
    }
    @Override
    public List<EncryptionRequest> encrypt(List<EncryptionRequest> requestList) throws HandymanException {
        return List.of();
    }

    @Override
    public List<EncryptionRequest> decrypt(List<EncryptionRequest> requestList) throws HandymanException {
        return List.of();
    }
    @Override
    public String getEncryptionMethod() {
        return "PROTEGRITY_ENC";
    }
}
