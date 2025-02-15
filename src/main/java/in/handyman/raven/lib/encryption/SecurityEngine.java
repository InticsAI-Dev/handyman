package in.handyman.raven.lib.encryption;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.encryption.impl.AESEncryptionImpl;
import in.handyman.raven.lib.encryption.impl.ProtegrityApiEncryptionImpl;
import in.handyman.raven.lib.encryption.impl.ProtegrityEncryptionImpl;
import in.handyman.raven.lib.encryption.inticsgrity.InticsIntegrity;

public class SecurityEngine {


    public static InticsIntegrity getInticsIntegrityMethod(final ActionExecutionAudit action) {
        String methodName = action.getContext().getOrDefault("pipeline.encryption.default.holder","");
        if (methodName.equals(EncryptionHandlers.PROTEGRITY_ENC.name())) {
            InticsIntegrity encryption = new InticsIntegrity(new ProtegrityEncryptionImpl());
            return encryption;
        } else if (methodName.equals(EncryptionHandlers.PROTEGRITY_API_ENC.name())) {
            InticsIntegrity encryption = new InticsIntegrity(new ProtegrityApiEncryptionImpl());
            return encryption;
        } else {
            InticsIntegrity encryption = new InticsIntegrity(new AESEncryptionImpl());
            return encryption;
        }
    }



}
