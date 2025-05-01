package in.handyman.raven.lib.encryption;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.encryption.impl.AESEncryptionImpl;
import in.handyman.raven.lib.encryption.impl.ProtegrityApiEncryptionImpl;
import in.handyman.raven.lib.encryption.impl.ProtegrityEncryptionImpl;
import in.handyman.raven.lib.encryption.inticsgrity.InticsIntegrity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static in.handyman.raven.lib.encryption.EncryptionConstants.*;

public class SecurityEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityEngine.class);

    private SecurityEngine() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    public static InticsIntegrity getInticsIntegrityMethod(final ActionExecutionAudit action) {
        String methodName = action.getContext().getOrDefault(DEFAULT_ENCRYPTION_ALGORITHM, "INBUILT_AES");
        String encryptionUrl = action.getContext().get(PROTEGRITY_ENCRYPT_URL);
        String decryptionUrl = action.getContext().get(PROTEGRITY_DECRYPT_URL);

        LOGGER.info("Initializing encryption method: {}", methodName);

        if (EncryptionHandlers.PROTEGRITY_ENC.name().equals(methodName)) {
            LOGGER.info("Selected encryption handler: PROTEGRITY_ENC");
            return new InticsIntegrity(new ProtegrityEncryptionImpl());
        } else if (EncryptionHandlers.PROTEGRITY_API_ENC.name().equals(methodName)) {
            LOGGER.info("Selected encryption handler: PROTEGRITY_API_ENC");
            return new InticsIntegrity(new ProtegrityApiEncryptionImpl(encryptionUrl, decryptionUrl, action));
        } else {
            LOGGER.info("Selected encryption handler: INBUILT_AES (default)");
            return new InticsIntegrity(new AESEncryptionImpl());
        }
    }
}
