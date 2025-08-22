package in.handyman.raven.core.encryption;

import in.handyman.raven.core.encryption.impl.EncryptionRequest;
import in.handyman.raven.exception.HandymanException;

import java.util.List;

public interface InticsDataEncryptionApi {
    public String encrypt(String inputToken, String encryptionPolicy, String sorItem) throws HandymanException;
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException;
    List<EncryptionRequest> encrypt(List<EncryptionRequest> requestList) throws HandymanException;
    List<EncryptionRequest> decrypt(List<EncryptionRequest> requestList) throws HandymanException;

    public String getEncryptionMethod();
}
