package in.handyman.raven.lib.encryption.impl;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lib.encryption.InticsDataEncryptionApi;

public class ProtegrityApiEncryptionImpl implements InticsDataEncryptionApi {

    @Override
    public String encrypt(String inputToken, String encryptionPolicy, String sorItem) throws HandymanException {
        //TODO CALL PROTEGRITY ENCRYPTION METHOD

        return new String();
    }

    @Override
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException {
        //TODO CALL PROTEGRITY DECRYPTION METHOD
        return new String();
    }

    @Override
    public String getEncryptionMethod() {
        return "PROTEGRITY_API_ENC";
    }
}
