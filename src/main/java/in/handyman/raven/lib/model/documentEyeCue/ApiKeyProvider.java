package in.handyman.raven.lib.model.documentEyeCue;

import in.handyman.raven.core.utils.ConfigEncryptionUtils;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ApiKeyProvider {
    private static final Logger log = LoggerFactory.getLogger(ApiKeyProvider.class);
    private static final Marker MARKER = MarkerFactory.getMarker("DocumentEyeCue");

    private static final String KEY_STORECONTENT_API_KEY = "storecontent.api.key";

    public static String getDecryptedApiKey(ActionExecutionAudit action) {
        try {
            if (action == null || action.getContext() == null) {
                String errorMessage = "ActionExecutionAudit or its context is null – cannot fetch API key";
                HandymanException.insertException(errorMessage, new HandymanException(errorMessage), action);
                log.warn(MARKER, errorMessage);
            }

            String encryptedApiKey = action.getContext().get(KEY_STORECONTENT_API_KEY);
            if (encryptedApiKey == null || encryptedApiKey.isBlank()) {
                String warnMessage = "No StoreContent API key found in ActionExecutionAudit context";
                HandymanException.insertException(warnMessage, new HandymanException(warnMessage), action);
                log.warn(MARKER, warnMessage);
            }

            return ConfigEncryptionUtils.fromEnv().decryptProperty(encryptedApiKey);

        } catch (Exception e) {
            String errorMessage = "Error decrypting StoreContent API key";
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(errorMessage, handymanException, action);
            log.error(MARKER, errorMessage, e);
            return "";
        }
    }
}