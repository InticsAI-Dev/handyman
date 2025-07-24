package in.handyman.raven.core.encryption;

import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.exception.HandymanException;

import java.util.List;

public interface InticsDataEncryptionApi {
    public String encrypt(String inputToken, String encryptionPolicy, String sorItem) throws HandymanException;
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException;
    List<EncryptionRequestClass> encrypt(List<EncryptionRequestClass> requestList) throws HandymanException;
    List<EncryptionRequestClass> decrypt(List<EncryptionRequestClass> requestList) throws HandymanException;
    public String getEncryptionMethod();
}
