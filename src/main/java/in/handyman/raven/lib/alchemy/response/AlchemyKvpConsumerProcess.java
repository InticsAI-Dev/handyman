package in.handyman.raven.lib.alchemy.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.AlchemyKvpResponseAction;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.alchemy.common.AlchemyApiPayload;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.lib.model.outbound.AlchemyKvpInputEntity;
import in.handyman.raven.lib.model.outbound.AlchemyKvpOutputEntity;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class AlchemyKvpConsumerProcess implements CoproProcessor.ConsumerProcess<AlchemyKvpInputEntity, AlchemyKvpOutputEntity> {


    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final AlchemyKvpResponseAction aAction;
    private final String STAGE_NAME = "PRODUCT_OUTBOUND";

    private final int timeOut;
    private final String authToken;

    public AlchemyKvpConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, AlchemyKvpResponseAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.aAction = aAction;
        this.timeOut = aAction.getTimeOut();
        this.authToken = action.getContext().get("alchemyAuth.token");
        this.httpclient = new OkHttpClient.Builder()
                .connectTimeout(this.timeOut, TimeUnit.MINUTES)

                .writeTimeout(this.timeOut, TimeUnit.MINUTES)
                .readTimeout(this.timeOut, TimeUnit.MINUTES)
                .build();
    }


    @Override
    public List<AlchemyKvpOutputEntity> process(URL endpoint, AlchemyKvpInputEntity entity) throws Exception {


        log.info(aMarker, " Alchemy consumer process Started for origin id {}", entity.getAlchemyOriginId());

        List<AlchemyKvpOutputEntity> parentObj = new ArrayList<>();
        String originId = entity.getAlchemyOriginId();
        Long rootPipelineId = entity.getRootPipelineId();


        log.info(aMarker, "Request object  endpoint {} ", endpoint);

        Long tenantId = entity.getTenant_id();
        String endPointFinalUrl = endpoint + "/" + originId + "/?tenantId=" + tenantId;

        Request request = new Request.Builder()
                .url(endPointFinalUrl)
                .addHeader("accept", "*/*")
                .addHeader("Authorization", "Bearer " + authToken)
                .build();

        try (Response response = httpclient.newCall(request).execute()) {

            Timestamp createdOn = Timestamp.valueOf(LocalDateTime.now());
            if (response.isSuccessful()) {
                AlchemyApiPayload alchemyApiPayload = mapper.readValue(response.body().string(), AlchemyApiPayload.class);


                if (!alchemyApiPayload.getPayload().isEmpty() && !alchemyApiPayload.getPayload().isNull() && alchemyApiPayload.isSuccess()) {

                    parentObj.add(AlchemyKvpOutputEntity
                            .builder()
                            .processId(entity.getProcessId())
                            .tenantId(tenantId)
                            .groupId(entity.getGroupId())
                            .kvpResponse(encryptRequestResponse(String.valueOf(alchemyApiPayload.getPayload())))
                            .alchemyOriginId(entity.getAlchemyOriginId())
                            .pipelineOriginId(entity.getPipelineOriginId())
                            .rootPipelineId(rootPipelineId)
                            .fileName(entity.getFileName())
                            .stage("PRODUCT_OUTBOUND").status("COMPLETED").message("alchemy kvp response completed for origin_id - " + entity.getAlchemyOriginId())
                            .build());
                }
            } else {
                parentObj.add(AlchemyKvpOutputEntity
                        .builder()
                        .processId(entity.getProcessId())
                        .tenantId(tenantId)
                        .groupId(entity.getGroupId())
                        .alchemyOriginId(entity.getAlchemyOriginId())
                        .pipelineOriginId(entity.getPipelineOriginId())
                        .rootPipelineId(rootPipelineId)
                        .stage("PRODUCT_OUTBOUND").status("FAILED").message("alchemy kvp response failed for origin_id - " + entity.getAlchemyOriginId())
                        .build());
            }


        } catch (Exception e) {
            log.error(aMarker, "The Exception occurred in getting response {} for origin Id {}", ExceptionUtil.toString(e),originId);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Alchemy kvp api consumer failed for origin Id - " + originId,
                    handymanException,
                    this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }


        return parentObj;
    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        String requestStr;
        if ("true".equals(encryptReqRes)) {
            String encryptedRequest = SecurityEngine.getInticsIntegrityMethod(action.log).encrypt(request, "AES256", "COPRO_REQUEST");
            requestStr = encryptedRequest;
        } else {
            requestStr = request;
        }
        return requestStr;
    }
}
