package in.handyman.raven.core.encryption.impl;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.core.encryption.InticsDataEncryptionApi;

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
    public String getEncryptionMethod() {
        return "PROTEGRITY_ENC";
    }
}
