package in.handyman.raven.lib.encryption;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.encryption.impl.AESEncryptionImpl;
import in.handyman.raven.lib.encryption.impl.ProtegrityApiEncryptionImpl;
import in.handyman.raven.lib.encryption.impl.ProtegrityEncryptionImpl;
import in.handyman.raven.lib.encryption.inticsgrity.InticsIntegrity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityEngine.class);

    public static InticsIntegrity getInticsIntegrityMethod(final ActionExecutionAudit action) {
        String methodName = action.getContext().getOrDefault("pipeline.encryption.default.holder", "");

        LOGGER.info("Initializing encryption method: {}", methodName);

        if (methodName.equals(EncryptionHandlers.PROTEGRITY_ENC.name())) {
            LOGGER.info("Selected encryption handler: PROTEGRITY_ENC");
            return new InticsIntegrity(new ProtegrityEncryptionImpl());
        } else if (methodName.equals(EncryptionHandlers.PROTEGRITY_API_ENC.name())) {
            LOGGER.info("Selected encryption handler: PROTEGRITY_API_ENC");
            return new InticsIntegrity(new ProtegrityApiEncryptionImpl());
        } else {
            LOGGER.info("Selected encryption handler: INBUILD_AES (default)");
            return new InticsIntegrity(new AESEncryptionImpl());
        }
    }
}
