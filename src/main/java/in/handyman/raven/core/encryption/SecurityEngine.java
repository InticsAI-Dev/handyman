package in.handyman.raven.core.encryption;

import in.handyman.raven.core.encryption.impl.AESEncryptionImpl;
import in.handyman.raven.core.encryption.impl.ProtegrityApiEncryptionImpl;
import in.handyman.raven.core.encryption.impl.ProtegrityEncryptionImpl;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.util.LoggingInitializer;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;

import static in.handyman.raven.core.encryption.EncryptionConstants.*;

public class SecurityEngine {

    // Static initialization to ensure logging is set up before any operations
    static {
        LoggingInitializer.initialize();
    }

    private SecurityEngine() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    public static InticsIntegrity getInticsIntegrityMethod(final ActionExecutionAudit action, final Logger logger) {
        String methodName = action.getContext().getOrDefault(DEFAULT_ENCRYPTION_ALGORITHM, "INBUILT_AES");
        String encryptionUrl = action.getContext().get(PROTEGRITY_ENCRYPT_URL);
        String decryptionUrl = action.getContext().get(PROTEGRITY_DECRYPT_URL);

        logger.info("Initializing encryption method: {}", methodName);

        if (EncryptionHandlers.PROTEGRITY_ENC.name().equals(methodName)) {
            logger.info("Selected encryption handler: PROTEGRITY_ENC");
            return new InticsIntegrity(new ProtegrityEncryptionImpl());
        } else if (EncryptionHandlers.PROTEGRITY_API_ENC.name().equals(methodName)) {
            logger.info("Selected encryption handler: PROTEGRITY_API_ENC");
            return new InticsIntegrity(new ProtegrityApiEncryptionImpl(encryptionUrl, decryptionUrl, action, logger));
        } else {
            logger.info("Selected encryption handler: INBUILT_AES (default)");
            return new InticsIntegrity(new AESEncryptionImpl());
        }
    }
}
