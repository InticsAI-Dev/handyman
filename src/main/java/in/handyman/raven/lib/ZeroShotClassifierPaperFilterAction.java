package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ZeroShotClassifierPaperFilter;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.lib.model.zeroShotClassifier.*;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "ZeroShotClassifierPaperFilter"
)
public class ZeroShotClassifierPaperFilterAction implements IActionExecution {
    public final ActionExecutionAudit action;

    public final Logger log;
    public final ZeroShotClassifierPaperFilter zeroShotClassifierPaperFilter;

    public final Marker aMarker;

    public ZeroShotClassifierPaperFilterAction(final ActionExecutionAudit action, final Logger log,
                                               final Object zeroShotClassifierPaperFilter) {
        this.zeroShotClassifierPaperFilter = (ZeroShotClassifierPaperFilter) zeroShotClassifierPaperFilter;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" ZeroShotClassifierPaperFilter:" + this.zeroShotClassifierPaperFilter.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(zeroShotClassifierPaperFilter.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "Phrase match paper filter Action for {} has been started", zeroShotClassifierPaperFilter.getName());
            final String processId = Optional.ofNullable(zeroShotClassifierPaperFilter.getProcessID()).map(String::valueOf).orElse(null);
            final String insertQuery = "INSERT INTO paper.zero_shot_classifier_filtering_result_" + processId + "(origin_id,group_id,paper_no,synonym,confidence_score,truth_entity,status,stage,message, created_on, root_pipeline_id) " +
                    " VALUES(?,?,?,?,?,?,?,?,?,now() ,?)";
            final List<URL> urls = Optional.ofNullable(action.getContext().get("copro.paper-filtering-zero-shot-classifier.url")).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL {}", s1, e);
                    throw new HandymanException("Error in processing the URL", e, action);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            final CoproProcessor<PaperFilteringZeroShotClassifierInputTable, PaperFilteringZeroShotClassifierOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            PaperFilteringZeroShotClassifierOutputTable.class,
                            PaperFilteringZeroShotClassifierInputTable.class,
                            jdbi, log,
                            new PaperFilteringZeroShotClassifierInputTable(), urls, action);
            coproProcessor.startProducer(zeroShotClassifierPaperFilter.getQuerySet(), Integer.parseInt(zeroShotClassifierPaperFilter.getReadBatchSize()));
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, Integer.parseInt(zeroShotClassifierPaperFilter.getThreadCount()),
                    Integer.parseInt(zeroShotClassifierPaperFilter.getWriteBatchSize()),
                    new ZeroShotConsumerProcess(log, aMarker, action));
            log.info(aMarker, " Zero shot classifier has been completed {}  ", zeroShotClassifierPaperFilter.getName());

        } catch (Exception e) {
            log.error(aMarker, "Error in zero shot paper filter action", e);
            throw new HandymanException("Error in zero shot paper filter action", e, action);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return zeroShotClassifierPaperFilter.getCondition();
    }

    public static class ZeroShotConsumerProcess implements CoproProcessor.ConsumerProcess<PaperFilteringZeroShotClassifierInputTable, PaperFilteringZeroShotClassifierOutputTable> {
        private final Logger log;
        private final Marker aMarker;
        private final ObjectMapper mapper = new ObjectMapper();
        private static final MediaType MediaTypeJSON = MediaType
                .parse("application/json; charset=utf-8");

        private static final String actionName = "ZERO_SHOT_CLASSIFIER";

        public final ActionExecutionAudit action;
        final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        public ZeroShotConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) throws JsonMappingException, JsonProcessingException {
            this.log = log;
            this.aMarker = aMarker;
            this.action = action;
        }

        @Override
        public List<PaperFilteringZeroShotClassifierOutputTable> process(URL endpoint, PaperFilteringZeroShotClassifierInputTable entity) throws JsonProcessingException {
            List<PaperFilteringZeroShotClassifierOutputTable> parentObj = new ArrayList<>();

            String originId = entity.getOriginId();
            String groupId = entity.getGroupId();
            String pipelineId = String.valueOf(entity.pipelineId);
            String processId = String.valueOf(entity.getProcessId());
            String paperNo = String.valueOf(entity.getPaperNo());
            Long actionId = action.getActionId();
            ObjectMapper objectMapper = new ObjectMapper();


            //payload

            ZeroShotClassifierData data = new ZeroShotClassifierData();
            data.setRootPipelineId("1");
            data.setActionId(actionId);
            data.setProcess(entity.getProcessId());
            data.setOriginId(originId);
            data.setPaperNo(paperNo);
            data.setGroupId(groupId);
            String jsonInputRequest = objectMapper.writeValueAsString(data);

            ZeroShotClassifierRequest requests = new ZeroShotClassifierRequest();
            TritonRequest requestBody = new TritonRequest();
            requestBody.setName("ZSC START");
            requestBody.setShape(List.of(1, 1));
            requestBody.setDatatype("BYTES");
            requestBody.setData(Collections.singletonList(jsonInputRequest));

            // requestBody.setData(Collections.singletonList(jsonNodeRequest));

            //   requestBody.setData(Collections.singletonList(data));

            TritonInputRequest tritonInputRequest=new TritonInputRequest();
            tritonInputRequest.setInputs(Collections.singletonList(requestBody));


            String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

            try {
                String truthPlaceholder = entity.getTruthPlaceholder();

                Request request = new Request.Builder().url(endpoint)
                        .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
                if (log.isInfoEnabled()) {
                    log.info(aMarker, "Input variables id : {}", actionId);
                    log.info(aMarker, "Request has been built with the parameters\nURI: {}, originId {}, truthPlaceHolder {}, groupId {}, paperNo {}", endpoint,originId, truthPlaceholder, groupId, paperNo);
                }
                coproAPIProcessor(entity, parentObj, request);
            } catch (Exception e) {
                log.error("Error in the zero-shot classifier paper filter copro api call {}", e.toString());
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("Exception occurred in urgency triage model action for group id - " + groupId + " and originId - " + originId, handymanException, this.action);
            }
            return parentObj;
        }

        private <EntityConfidenceScore> void coproAPIProcessor(PaperFilteringZeroShotClassifierInputTable entity, List<PaperFilteringZeroShotClassifierOutputTable> parentObj, Request request) throws IOException {
            String originId = entity.getOriginId();
            String groupId = entity.getGroupId();

            final Integer paperNo = Optional.ofNullable(entity.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null);
            Long rootPipelineId = entity.getRootPipelineId();

            try (Response response = httpclient.newCall(request).execute()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ZeroShotClassifierModelResponse modelResponse = objectMapper.readValue(responseBody, ZeroShotClassifierModelResponse.class);
                    if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                        modelResponse.getOutputs().forEach(o -> {
                            o.getData().forEach(ZeroShotClassifierDataItem -> {
                                 ZeroShotClassifierDataItem.getEntity_confidence_score().forEach(ZeroShotClassifierDataEntityConfidenceScore -> {
                                     in.handyman.raven.lib.model.zeroShotClassifier.ZeroShotClassifierDataEntityConfidenceScore score = new ZeroShotClassifierDataEntityConfidenceScore();
                                     String truthEntity = score.getTruthEntity();
                                     String key = score.getKey();
                                     double scoreValue = score.getScore();

                                     parentObj.add(PaperFilteringZeroShotClassifierOutputTable
                                             .builder()
                                             .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                             .groupId(Optional.ofNullable(groupId).map(String::valueOf).orElse(null))
                                             .truthEntity(Optional.ofNullable(truthEntity).map(String::valueOf).orElse(null))
                                             .entity(Optional.ofNullable(key).map(String::valueOf).orElse(null))
                                             .confidenceScore(Optional.of(scoreValue).map(String::valueOf).orElse(null))
                                             .paperNo(paperNo)
                                             .status("COMPLETED")
                                             .stage(actionName)
                                             .message("Completed API call zero shot classifier")
                                             .rootPipelineId(rootPipelineId)
                                             .build());
                                 });
                            });
                        });
                    }
                } else {
                    parentObj.add(
                            PaperFilteringZeroShotClassifierOutputTable
                                    .builder()
                                    .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                    .groupId(Optional.ofNullable(groupId).map(String::valueOf).orElse(null))
                                    .status("FAILED")
                                    .paperNo(paperNo)
                                    .stage(actionName)
                                    .message(Optional.of(responseBody).map(String::valueOf).orElse(null))
                                    .rootPipelineId(rootPipelineId)
                                    .build());
                    log.error(aMarker, "Exception occurred in zero shot classifier API call");
                }
            } catch (Exception exception) {
                parentObj.add(
                        PaperFilteringZeroShotClassifierOutputTable
                                .builder()
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .groupId(Optional.ofNullable(groupId).map(String::valueOf).orElse(null))
                                .status("FAILED")
                                .paperNo(paperNo)
                                .stage(actionName)
                                .message(exception.getMessage())
                                .rootPipelineId(rootPipelineId)
                                .build());
                log.error(aMarker, "Exception occurred in the zero shot classifier paper filter action {}", ExceptionUtil.toString(exception));
                HandymanException handymanException = new HandymanException(exception);
                HandymanException.insertException("Zero shot classifier paper filter action failed for groupId - " + groupId + "and originId - " + originId + "and paperNo -" + paperNo, handymanException, action);
            }
        }
    }
}
