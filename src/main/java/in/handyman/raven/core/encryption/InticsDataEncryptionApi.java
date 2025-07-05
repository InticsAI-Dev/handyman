package in.handyman.raven.core.encryption;

import in.handyman.raven.exception.HandymanException;

public interface InticsDataEncryptionApi {
    public String encrypt(String inputToken, String encryptionPolicy, String sorItem) throws HandymanException;
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException;
    public String getEncryptionMethod();
}
