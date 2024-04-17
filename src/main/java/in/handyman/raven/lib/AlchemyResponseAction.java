package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AlchemyResponse;
import in.handyman.raven.lib.model.paperitemizer.ProcessAuditOutputTable;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
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
        actionName = "AlchemyResponse"
)
public class AlchemyResponseAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final AlchemyResponse alchemyResponse;

    private final Marker aMarker;
    private static final List<ProcessAuditOutputTable> processOutputAudit = new ArrayList<>();


    public AlchemyResponseAction(final ActionExecutionAudit action, final Logger log,
                                 final Object alchemyResponse) {
        this.alchemyResponse = (AlchemyResponse) alchemyResponse;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" AlchemyResponse:" + this.alchemyResponse.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(alchemyResponse.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "Alchemy Response Action for {} has been started", alchemyResponse.getName());
            final String insertQuery = "";
            final List<URL> urls = Optional.ofNullable(action.getContext().get("alchemy.origin.valuation.url")).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            final CoproProcessor<AlchemyResponseAction.AlchemyResponseInputTable, AlchemyResponseAction.AlchemyResponseOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            AlchemyResponseAction.AlchemyResponseOutputTable.class,
                            AlchemyResponseAction.AlchemyResponseInputTable.class,
                            jdbi, log,
                            new AlchemyResponseAction.AlchemyResponseInputTable(), urls, action);
            coproProcessor.startProducer(alchemyResponse.getQuerySet(), Integer.valueOf(action.getContext().get("read.batch.size")));
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, 1, Integer.valueOf(action.getContext().get("write.batch.size")), new AlchemyResponseAction.AlchemyReponseConsumerProcess(log, aMarker, action));
            log.info(aMarker, "Alchemy Info has been completed {}  ", alchemyResponse.getName());
        } catch (Exception t) {
            action.getContext().put(alchemyResponse.getName() + ".isSuccessful", "false");
            log.error(aMarker, "Error at alchemy response execute method {}", ExceptionUtil.toString(t));
            throw new HandymanException("Error at alchemyResponse execute method ", t, action);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return alchemyResponse.getCondition();
    }

    public static class AlchemyReponseConsumerProcess implements CoproProcessor.ConsumerProcess<AlchemyResponseAction.AlchemyResponseInputTable, AlchemyResponseAction.AlchemyResponseOutputTable> {
        private final Logger log;
        private final Marker aMarker;
        private final ObjectMapper mapper = new ObjectMapper();

        public final ActionExecutionAudit action;

        private final Long tenantId;
        private final String authToken;
        private static final MediaType MediaTypeJSON = MediaType
                .parse("application/json; charset=utf-8");
        final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        public AlchemyReponseConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
            this.log = log;
            this.aMarker = aMarker;
            this.action = action;
            this.tenantId = Long.valueOf(action.getContext().get("alchemyAuth.tenantId"));
            this.authToken = action.getContext().get("alchemyAuth.token");
        }

        @Override
        public List<AlchemyResponseAction.AlchemyResponseOutputTable> process(URL endpoint, AlchemyResponseAction.AlchemyResponseInputTable entity) throws Exception {

            List<AlchemyResponseAction.AlchemyResponseOutputTable> parentObj = new ArrayList<>();
            String originId = entity.getOriginId();
            Integer paperNo = entity.getPaperNo();
            Long rootPipelineId = entity.getRootPipelineId();
            Integer confidenceScore = entity.getConfidenceScore();
            String extractedValue = entity.getExtractedValue();
            String sorItemName = entity.getSorItemName();
            Long synonymId = entity.getSynonymId();
            Long questionId = entity.getQuestionId();
            String bbox = entity.getBbox();
            String feature = entity.getFeature();

            AlchemyRequestTable alchemyRequestTable = AlchemyRequestTable
                    .builder()
                    .paperNo(paperNo)
                    .rootPipelineId(rootPipelineId)
                    .feature(feature)
                    .build();

            if(feature.equals("KIE")){
                alchemyRequestTable.setBbox(mapper.readTree(bbox));
                alchemyRequestTable.setConfidenceScore(confidenceScore);
                alchemyRequestTable.setExtractedValue(extractedValue);
                alchemyRequestTable.setSynonymId(synonymId);
                alchemyRequestTable.setQuestionId(questionId);
            }
            if(feature.equals("CHECKBOX_EXTRACTION")){
                alchemyRequestTable.setBbox(mapper.readTree(bbox));
                alchemyRequestTable.setConfidenceScore(confidenceScore);
                alchemyRequestTable.setExtractedValue(extractedValue);
                alchemyRequestTable.setState(entity.getState());
            }
            if(feature.equals("TABLE_EXTRACT")){
                JsonNode tableNode = mapper.readTree(entity.getTableData());
                alchemyRequestTable.setTableData(tableNode);
                alchemyRequestTable.setCsvFilePath(entity.getCsvFilePath());
                alchemyRequestTable.setTruthEntityId(entity.getTruthEntityId());
            }
            if(feature.equals("CURRENCY_DETECTION")){
                alchemyRequestTable.setDetectedValue(entity.getDetectedValue());
                alchemyRequestTable.setDetectedAsciiValue(entity.getDetectedAsciiValue());
                alchemyRequestTable.setConfidenceScore(entity.getConfidenceScore());
            }
            if(feature.equals("TABLE_EXTRACT_AGGREGATE")){
                JsonNode tableAggregateNode = mapper.readTree(entity.getTableAggregateNode());
                JsonNode tableNode = mapper.readTree(entity.getTableData());
                alchemyRequestTable.setTableData(tableNode);
                alchemyRequestTable.setAggregateJson(tableAggregateNode);
                alchemyRequestTable.setSorItemId(entity.sorItemId);
            }
            if(feature.equals("BULLETIN_EXTRACTION")){
                JsonNode bulletinOutput = mapper.readTree(entity.getBulletinPoints());
                alchemyRequestTable.setBulletinPoints(bulletinOutput);
                alchemyRequestTable.setBulletinSection(entity.getBulletinSection());
                alchemyRequestTable.setSynonymId(entity.getSynonymId());
            }
            if(feature.equals("PARAGRAPH_EXTRACTION")){
                JsonNode paragraphOutput = mapper.readTree(entity.getParagraphPoints());
                alchemyRequestTable.setParagraphPoints(paragraphOutput);
                alchemyRequestTable.setParagraphSection(entity.getParagraphSection());
                alchemyRequestTable.setSynonymId(entity.getSynonymId());
            }




            Request request = new Request.Builder().url(endpoint + "/" + originId + "/?tenantId=" + this.tenantId)
                    .addHeader("accept", "*/*")
                    .addHeader("Authorization", "Bearer " + authToken)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(mapper.writeValueAsString(alchemyRequestTable), MediaTypeJSON))
                    .build();

            if (log.isInfoEnabled()) {
                log.info(aMarker, "Request has been build with the parameters {}, alchemy originId : {}, sorItemName : {}", endpoint, originId, sorItemName);
            }

            try (Response response = httpclient.newCall(request).execute()) {
                processOutputAudit.add(
                        ProcessAuditOutputTable.builder()
                                .originId(originId)
                                .tenantId(tenantId)
                                .batchId("1")
                                .endpoint(String.valueOf(endpoint))
                                .rootPipelineId(entity.getRootPipelineId())
                                .request(String.valueOf(request))
                                .response(String.valueOf(response))
                                .stage("")
                                .message("")
                                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                                .build());
                if (response.isSuccessful()) {
                    log.info("Response Details: {}", response);
                    log.info(aMarker, "Execute for alchemy response {}", response);
                }
            } catch (Exception e) {
                log.error(aMarker, "The Exception occurred in alchemy response action", e);
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("Exception occurred in alchemy response action for alchemy originId - " + originId + " and sorItemName - " + sorItemName, handymanException, this.action);
            }
            return parentObj;
        }

        @Override
        public List<ProcessAuditOutputTable> processAudit() throws Exception {
            return processOutputAudit;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class AlchemyResponseInputTable implements CoproProcessor.Entity {

        private String originId;
        private Integer paperNo;
        private Long tenantId;
        private Long rootPipelineId;
        private Integer confidenceScore;
        private String extractedValue;
        private String sorItemName;
        private Long synonymId;
        private Long questionId;
        private String bbox;
        private String feature;
        private String state;
        private String tableData;
        private String detectedValue;
        private String detectedAsciiValue;
        private String csvFilePath;
        private Long truthEntityId;
        private String tableAggregateNode;
        private Long sorItemId;
        private String bulletinSection;
        private String bulletinPoints;
        private String paragraphSection;
        private String paragraphPoints;


        @Override
        public List<Object> getRowData() {
            return null;
        }
    }
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class AlchemyCurrencyResponse{
        private String detectedValue;
        private String detectedAsciiValue;
        private String confidenceScore;
    }
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class AlchemyRequestTable {
        private Integer paperNo;
        private Integer confidenceScore;
        private String extractedValue;
        private JsonNode bbox;
        private Long synonymId;
        private Long questionId;
        private Long rootPipelineId;
        private String feature;
        private String state;
        private String csvFilePath;
        private Long truthEntityId;
        private JsonNode tableData;
        private String detectedValue;
        private String detectedAsciiValue;
        private JsonNode aggregateJson;
        private Long sorItemId;
        private String bulletinSection;
        private JsonNode bulletinPoints;
        private String paragraphSection;
        private JsonNode paragraphPoints;
    }

    @AllArgsConstructor
    @Data
    @Builder
    public static class AlchemyResponseOutputTable implements CoproProcessor.Entity {

        @Override
        public List<Object> getRowData() {
            return null;
        }
    }

}
