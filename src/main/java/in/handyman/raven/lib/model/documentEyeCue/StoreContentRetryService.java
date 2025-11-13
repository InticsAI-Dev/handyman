package in.handyman.raven.lib.model.documentEyeCue;

import com.anthem.acma.commonclient.storecontent.dto.StoreContentRequestDto;
import com.anthem.acma.commonclient.storecontent.dto.StoreContentResponseDto;
import com.anthem.acma.commonclient.storecontent.logic.Acmastorecontentclient;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.enums.EncryptionConstants;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.retry.CoproRetryErrorAuditTable;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import in.handyman.raven.util.ExceptionUtil;
import org.slf4j.Logger;
import static in.handyman.raven.exception.HandymanException.handymanRepo;

public class StoreContentRetryService {

    private final HandymanRepo handymanRepo;
    private final Logger log;

    private static final List<String> RETRYABLE_MESSAGES = Arrays.asList(
            "connection reset", "broken pipe", "connection timed out",
            "read timed out", "unexpected end of stream", "socket closed",
            "REFUSED_STREAM", "PROTOCOL_ERROR", "stream was reset",
            "INTERNAL_ERROR", "CANCEL", "ENHANCE_YOUR_CALM"
    );

    public StoreContentRetryService(Logger log) {
        this.handymanRepo=HandymanException.handymanRepo;
        this.log = log;
    }

    /**
     * Executes StoreContent upload with retry and audit.
     */
    public StoreContentResponseDto uploadWithRetry(StoreContentRequestDto requestDto,
                                                   Acmastorecontentclient client,
                                                   ActionExecutionAudit actionAudit,
                                                   CoproRetryErrorAuditTable retryAudit) throws IOException {

        int maxRetries = Integer.parseInt(actionAudit.getContext().getOrDefault("storecontent.retry.attempt", "1"));
        IOException lastException = null;
        retryAudit.setCoproServiceId(UUID.randomUUID().toString());
        retryAudit.setStage("STORE_CONTENT_UPLOAD");
        retryAudit.setCreatedOn(new Timestamp(System.currentTimeMillis()));

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Attempt {} - Uploading to StoreContent service [id={}]", attempt, retryAudit.getCoproServiceId());
                StoreContentResponseDto responseDto = client.storeContent(requestDto);

                if (responseDto != null && responseDto.getStatus()==200) {
                    log.info("StoreContent upload SUCCESS on attempt {} | contentId={} | message={}",
                            attempt, responseDto.getContentID(), responseDto.getStatus());
                    retryAudit.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());
                    retryAudit.setMessage(String.valueOf(responseDto.getStatus()));
                    retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
                    insertAudit(attempt, retryAudit, requestDto, responseDto, null, actionAudit);
                    return responseDto;
                }

                // Non-retryable conditions
                if (!isRetryRequired(responseDto)) {
                    log.warn("Non-retryable response on attempt {} - message: {}", attempt,
                            responseDto != null ? String.valueOf(responseDto.getStatus()) : "null");
                    retryAudit.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());
                    retryAudit.setMessage(responseDto != null ? String.valueOf(responseDto.getStatus()) : "null");
                    retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
                    insertAudit(attempt, retryAudit, requestDto, responseDto, null, actionAudit);
                    return responseDto;
                }

                // Retryable case
                log.warn("Attempt {} failed (retryable). Status: {} Message: {}",
                        attempt, responseDto != null ? responseDto.getStatus() : "null",
                        responseDto != null ? responseDto.getMessage() : "null");
                retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
                insertAudit(attempt, retryAudit, requestDto, responseDto, null, actionAudit);

            } catch (IOException e) {
                lastException = e;
                if (!isRetryableException(e)) {
                    log.error("Non-retryable exception on attempt {}: {}", attempt, ExceptionUtil.toString(e));
                    handleIOException(attempt, retryAudit, requestDto, e, actionAudit);
                    throw e;
                }

                log.warn("Retryable IOException on attempt {}: {}", attempt, e.getMessage());
                handleIOException(attempt, retryAudit, requestDto, e, actionAudit);

                if (attempt == maxRetries) {
                    throw lastException;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (attempt < maxRetries) {
                sleepBackoff(actionAudit, attempt);
            }
        }

        throw lastException != null
                ? lastException
                : new IOException("StoreContent upload failed after " + maxRetries + " attempts.");
    }

    private boolean isRetryRequired(StoreContentResponseDto responseDto) {
        if (responseDto == null) return true;

        Integer status = responseDto.getStatus();
        if (status == null) return true;

        // Retry only if status is not 200 (HTTP OK)
        return status != 200;
    }


    private boolean isRetryableException(IOException e) {
        String message = e.getMessage();
        if (message == null) return true;

        for (String pattern : RETRYABLE_MESSAGES) {
            if (message.contains(pattern)) return true;
        }

        return e instanceof SocketException || e instanceof ProtocolException;
    }

    private void handleIOException(int attempt,
                                   CoproRetryErrorAuditTable retryAudit,
                                   StoreContentRequestDto requestDto,
                                   IOException e,
                                   ActionExecutionAudit action) {
        log.error("Attempt {}: IOException - {}", attempt, ExceptionUtil.toString(e));
        retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
        insertAudit(attempt, retryAudit, requestDto, null, e, action);
        HandymanException.insertException("Error during StoreContent upload",
                new HandymanException(e), action);
    }

    private void insertAudit(int attempt,
                             CoproRetryErrorAuditTable retryAudit,
                             StoreContentRequestDto requestDto,
                             StoreContentResponseDto response,
                             Exception e,
                             ActionExecutionAudit action) {
        try {
            populateAudit(attempt, retryAudit, requestDto, response, e, action);
            retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
            handymanRepo.insertAuditToDb(retryAudit, action);
        } catch (Exception ex) {
            log.error("Error inserting retry audit: {}", ExceptionUtil.toString(ex));
            HandymanException.insertException("Error inserting StoreContent retry audit",
                    new HandymanException(ex), action);
        }
    }

    private void populateAudit(int attempt,
                               CoproRetryErrorAuditTable retryAudit,
                               StoreContentRequestDto requestDto,
                               StoreContentResponseDto response,
                               Exception e,
                               ActionExecutionAudit action) {
        retryAudit.setAttempt(attempt);
        retryAudit.setStage("STORE_CONTENT_UPLOAD");
//        retryAudit.setRequest(encryptRequestResponse(requestDto.toString(), action));

        if (response != null) {
            retryAudit.setMessage(String.valueOf(response.getStatus()));
//            retryAudit.setResponse(encryptRequestResponse(response.toString(), action));
        } else if (e != null) {
            String message = e.getMessage() != null ? e.getMessage() : ExceptionUtil.toString(e);
            retryAudit.setMessage(message);
//            retryAudit.setResponse(encryptRequestResponse(ExceptionUtil.toString(e), action));
        }
    }

    private String encryptRequestResponse(String data, ActionExecutionAudit action) {
        String encryptReqRes = action.getContext().get(EncryptionConstants.ENCRYPT_REQUEST_RESPONSE);
        if ("true".equalsIgnoreCase(encryptReqRes)) {
            return SecurityEngine.getInticsIntegrityMethod(action, log)
                    .encrypt(data, "AES256", "STORECONTENT_REQUEST");
        }
        return data;
    }

    private void sleepBackoff(ActionExecutionAudit actionAudit, int attempt) {
        String delayStr = actionAudit.getContext().getOrDefault("storecontent.retry.delay.inSeconds", "3");
        long baseBackoffMillis = TimeUnit.SECONDS.toMillis(5);

        try {
            baseBackoffMillis = TimeUnit.SECONDS.toMillis(Long.parseLong(delayStr));
        } catch (NumberFormatException e) {
            log.error("Invalid retry delay, defaulting to 5s. {}", e.getMessage());
        }

        long backoffMillis = baseBackoffMillis * (long) Math.pow(2, attempt - 1);
        backoffMillis = Math.min(backoffMillis, TimeUnit.SECONDS.toMillis(60));

        log.info("Sleeping {} ms before retry attempt {}", backoffMillis, attempt + 1);

        try {
            Thread.sleep(backoffMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted during sleep: {}", ex.getMessage());
        }
    }
}