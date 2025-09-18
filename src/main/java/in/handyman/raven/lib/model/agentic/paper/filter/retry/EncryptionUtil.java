package in.handyman.raven.lib.model.agentic.paper.filter.retry;

import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public final class EncryptionUtil {
    private static final Logger log = LoggerFactory.getLogger(EncryptionUtil.class);
    private static final int ENCRYPTION_TIMEOUT_SECONDS = 5;
    private static final int MAX_SAFE_LENGTH = 2000;

    private EncryptionUtil() {}

    public static String encryptRequestResponse(String data, ActionExecutionAudit action) {
        if (data == null) return null;
        String encrypt = action != null && action.getContext() != null
                ? action.getContext().get(ENCRYPT_REQUEST_RESPONSE)
                : null;

        if ("true".equalsIgnoreCase(encrypt)) {
            return CompletableFuture.supplyAsync(() -> {
                        try {
                            return SecurityEngine
                                    .getInticsIntegrityMethod(action, log)
                                    .encrypt(data, "AES256", "COPRO_REQUEST");
                        } catch (Exception ex) {
                            throw new CompletionException(ex);
                        }
                    }).orTimeout(ENCRYPTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .exceptionally(ex -> handleEncryptionFailure(ex, data))
                    .join();
        }
        return data;
    }

    private static String handleEncryptionFailure(Throwable ex, String data) {
        if (ex instanceof TimeoutException || (ex.getCause() instanceof SocketTimeoutException)) {
            log.warn("Encryption timed out after {}s; storing plaintext.", ENCRYPTION_TIMEOUT_SECONDS);
            return "[ENCRYPTION_TIMEOUT] " + truncateSafe(data);
        } else {
            log.error("Encryption failed: {}; storing plaintext.", ex.getMessage());
            return "[ENCRYPTION_ERROR] " + truncateSafe(data);
        }
    }

    private static String truncateSafe(String input) {
        if (input == null) return null;
        return input.length() > MAX_SAFE_LENGTH ? input.substring(0, MAX_SAFE_LENGTH) + "...[TRUNCATED]" : input;
    }
}
