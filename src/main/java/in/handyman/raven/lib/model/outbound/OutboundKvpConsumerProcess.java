package in.handyman.raven.lib.model.outbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.OutboundKvpResponseAction;
import in.handyman.raven.lib.alchemy.common.AlchemyApiPayload;
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

public class OutboundKvpConsumerProcess implements CoproProcessor.ConsumerProcess<AlchemyKvpInputEntity, AlchemyKvpOutputEntity> {

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private final OutboundKvpResponseAction aaction;
    private final OkHttpClient httpclient;
    private final String authToken;
    private final String STAGE_NAME = "PRODUCT_OUTBOUND";

    public OutboundKvpConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action, OutboundKvpResponseAction aaction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.aaction = aaction;
        this.authToken = action.getContext().get("alchemyAuth.token");
        this.httpclient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public List<AlchemyKvpOutputEntity> process(URL endpoint, AlchemyKvpInputEntity entity) throws Exception {

        log.info(aMarker, " Alchemy cosumer process Started for origin id {}", entity.getAlchemyOriginId());

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
                            .kvpResponse(String.valueOf(alchemyApiPayload.getPayload()))
                            .alchemyOriginId(entity.getAlchemyOriginId())
                            .pipelineOriginId(entity.getPipelineOriginId())
                            .rootPipelineId(rootPipelineId)
                            .fileName(entity.getFileName())
                            .stage("PRODUCT_OUBOUND").status("COMPLETED").message("alchemy kvp response completed for origin_id - " + entity.getAlchemyOriginId())
                            .batchId(entity.getBatchId())
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
                        .batchId(entity.getBatchId())
                        .stage("PRODUCT_OUBOUND").status("FAILED").message("alchemy kvp response failed for origin_id - " + entity.getAlchemyOriginId())
                        .build());
            }


        } catch (Exception e) {
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Alchemy kvp api consumer failed for origin_id - " + originId,
                    handymanException,
                    this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }


        return parentObj;
    }
}


