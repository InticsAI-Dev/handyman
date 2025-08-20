package in.handyman.raven.core.encryption.inticsgrity;

import in.handyman.raven.core.encryption.impl.EncryptionRequest;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.core.encryption.InticsDataEncryptionApi;

import java.util.List;

public class InticsIntegrity implements InticsDataEncryptionApi {

    private final InticsDataEncryptionApi encryptionImpl;

    public InticsIntegrity(InticsDataEncryptionApi encryptionImpl) {
        this.encryptionImpl = encryptionImpl;
    }


    @Override
    public String encrypt(String inputToken, String encryptionPolicy, String sorItem) throws HandymanException {
        return encryptionImpl.encrypt(inputToken, encryptionPolicy, sorItem);
    }

    @Override
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException {
        return encryptionImpl.decrypt(encryptedToken, encryptionPolicy, sorItem);
    }


    @Override
    public List<EncryptionRequest> encrypt(List<EncryptionRequest> requestList) throws HandymanException {
        return encryptionImpl.encrypt(requestList);
    }

    @Override
    public List<EncryptionRequest> decrypt(List<EncryptionRequest> requestList) throws HandymanException {
        return encryptionImpl.decrypt(requestList);
    }

    @Override
    public String getEncryptionMethod() {
        return encryptionImpl.getEncryptionMethod();

    }

}
