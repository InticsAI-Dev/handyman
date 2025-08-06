package in.handyman.raven.lib.model.documentEyeCue;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.core.encryption.SecurityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiKeyProvider {
    private static final Logger log = LoggerFactory.getLogger(ApiKeyProvider.class);

    public static String getDecryptedApiKey(ActionExecutionAudit action) {
        if (action == null) {
            log.warn("ActionExecutionAudit is null – cannot fetch API key");
            return "";
        }
        String encryptedApiKey = action.getContext().get("storecontent.api.key");
        if (encryptedApiKey == null || encryptedApiKey.isBlank()) {
            log.warn("No API key found in action context");
            return "";
        }
        try {
            return SecurityEngine.getInticsIntegrityMethod(action, log)
                    .decrypt(encryptedApiKey, "AES256", "STORECONTENT_API_KEY");
        } catch (Exception e) {
            log.error("Error decrypting API key", e);
            return "";
        }
    }
}