package in.handyman.raven.lib.model.agenticPaperFilter;

import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;


public class CoproRetryService {
    private static final Logger log = LoggerFactory.getLogger(CoproRetryService.class);
    private static final OkHttpClient httpclient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.MINUTES).writeTimeout(10, TimeUnit.MINUTES).readTimeout(10, TimeUnit.MINUTES).build();
    private static final String errorAudictTable = "copro_retry_error_audit";
    private static final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn("intics_zio_db_conn");
    private static ActionExecutionAudit action;
    private static int maxRetries ;

    public static Response callCoproApiWithRetry(Request request, CoproRetryErrorAudictTable retryAudict, ActionExecutionAudit actionAudit) {
        int attempt = 0;
        action = actionAudit;
        boolean isRetryActive = Boolean.parseBoolean(action.getContext().getOrDefault("copro.isretry.enabled","false"));
        maxRetries = isRetryActive ? action.getContext().get("copro.retry.attempt") != null ? Integer.parseInt(action.getContext().get("retry.attempt")):1:1;
        Response response = null;
        while (attempt < maxRetries) {
            int nextAttempt = attempt+1;
            try  {
                if (response != null) {
                    response.close();
                }
                response = httpclient.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response;
                } else if(isRetryActive && (nextAttempt < maxRetries)){
                    log.error("Attempt {}: Unsuccessful response {} - {}", attempt + 1, response.code(), response.message());
                    insertCoproRetryErrorAudict(retryAudict, request, response,null);
                }
            } catch (IOException e) {
                log.error("Attempt {}: IOException - {}", attempt + 1, ExceptionUtil.toString(e));
                if(isRetryActive && (nextAttempt < maxRetries)){
                    insertCoproRetryErrorAudict(retryAudict, request, null,e);
                }
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("Error inserting into copro retry audit", handymanException, action);
            }
            attempt++;
        }
        return response;
    }

    private static void insertCoproRetryErrorAudict(CoproRetryErrorAudictTable retryAudict, Request request, Response response,Exception e) {
        try {
            retryAudict.setRequest(encryptRequestResponse(String.valueOf(request)));
            if(response != null){
                retryAudict.setMessage(response.message());
                retryAudict.setResponse(encryptRequestResponse(response.toString()));
            }else{
                retryAudict.setMessage(e.getMessage());
                retryAudict.setResponse(null);
            }

            jdbi.useTransaction(handle -> {
                Update update = handle.createUpdate("  INSERT INTO macro." + errorAudictTable +
                        "( origin_id, group_id, tenant_id,process_id, file_path,paper_no,status,created_on,root_pipeline_id,batch_id,last_updated_on,request,response,endpoint) " +
                        " VALUES(:originId, :groupId, :tenantId, :processId, :filePath,:paperNo,:status,:createdOn,:rootPipelineId,:batchId,NOW(),:request,:response,:endpoint);");
                Update bindBean = update.bindBean(retryAudict);
                bindBean.execute();
            });
        } catch (Exception exception) {
            log.error("Error inserting into retry audit  {}", ExceptionUtil.toString(exception));
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Error inserting into copro retry audit", handymanException, action);
        }
    }

    public static String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        String requestStr;
        if ("true".equals(encryptReqRes)) {
            String encryptedRequest = SecurityEngine.getInticsIntegrityMethod(action).encrypt(request, "AES256", "COPRO_REQUEST");
            requestStr = encryptedRequest;
        } else {
            requestStr = request;
        }
        return requestStr;
    }
}

