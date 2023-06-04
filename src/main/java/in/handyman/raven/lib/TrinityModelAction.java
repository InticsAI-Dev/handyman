package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TrinityModel;

import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import in.handyman.raven.util.CommonQueryUtil;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "TrinityModel"
)
public class TrinityModelAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final TrinityModel trinityModel;

    private final Marker aMarker;
    public static final String ATTRIBUTION_URL = "copro.trinity-attribution.url";
    private final AtomicInteger counter = new AtomicInteger();
    private static String httpClientTimeout = new String();
    private final List<String> nodes;

    public TrinityModelAction(final ActionExecutionAudit action, final Logger log,
                              final Object trinityModel) {
        this.trinityModel = (TrinityModel) trinityModel;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" TrinityModel:" + this.trinityModel.getName());
        this.nodes = Optional.ofNullable(action.getContext().get(ATTRIBUTION_URL)).map(s -> Arrays.asList(s.split(","))).orElse(Collections.emptyList());
        httpClientTimeout = action.getContext().get("okhttp.client.timeout");
    }

    @Override
    public void execute() throws Exception {


        try {
            log.info(aMarker, "Trinity Model Attribution Action for {} has been started", trinityModel.getName());
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(trinityModel.getResourceConn());
            final List<TrinityModelAction.TrinityModelQueryResult> trinityModelQueryResults = new ArrayList<>();
            jdbi.useTransaction(handle -> {
                final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(trinityModel.getQuestionSql());
                formattedQuery.forEach(sqlToExecute -> trinityModelQueryResults.addAll(handle.createQuery(sqlToExecute)
                        .mapToBean(TrinityModelAction.TrinityModelQueryResult.class)
                        .stream().collect(Collectors.toList())));
            });

            // Create DDL

            jdbi.useTransaction(handle -> handle.execute("create table if not exists macro." + trinityModel.getResponseAs() + " ( id bigserial not null, file_path text,question text, predicted_attribution_value text , score float8 NULL, b_box json null, image_dpi int8 null, image_width int8 null, image_height int8 null, extracted_image_unit varchar null, action_id bigint, root_pipeline_id bigint,process_id bigint, created_on timestamp not null default now(),status varchar NULL,stage varchar NULL ,paper_type varchar NULL);"));
            jdbi.useTransaction(handle -> handle.execute("create table if not exists macro." + trinityModel.getResponseAs() + "_error ( id bigserial not null, file_path text,error_message text,  action_id bigint, root_pipeline_id bigint,process_id bigint, created_on timestamp not null default now() );"));
            final List<TrinityModelAction.TrinityModelLineItem> trinityModelLineItems = new ArrayList<>();

            Map<String, Map<String, List<TrinityModelAction.TrinityModelQueryResult>>> listTrinityModelQueryResult = trinityModelQueryResults.stream().collect(Collectors.groupingBy(TrinityModelAction.TrinityModelQueryResult::getFilePath, Collectors.groupingBy(TrinityModelAction.TrinityModelQueryResult::getPaperType)));

            listTrinityModelQueryResult.forEach((s, stringListMap) -> {
                stringListMap.forEach((s1, trinityModelQueryResults1) -> trinityModelLineItems.add(TrinityModelAction.TrinityModelLineItem.builder()
                        .filePath(s).paperType(s1).questions(trinityModelQueryResults1.stream().map(TrinityModelAction.TrinityModelQueryResult::getQuestion).collect(Collectors.toList()))
                        .build()));
            });

            doProcess(trinityModelLineItems);
        } catch (Exception e) {
            log.error(aMarker, "Error in trinity model attribution action", e);
            throw new HandymanException("Error in trinity model attribution action", e, action);
        }

    }

    private void doProcess(final List<TrinityModelAction.TrinityModelLineItem> donutLineItems) {
        final int parallelism;
        if (trinityModel.getForkBatchSize() != null) {
            parallelism = Integer.parseInt(trinityModel.getForkBatchSize());
        } else {
            parallelism = 1;
        }
        final int size = nodes.size();
        try {
            if (size > 0) {
                final int batchSize = donutLineItems.size() / parallelism;
                if (parallelism > 1 && size > 1 && batchSize > 0) {
                    final List<List<TrinityModelAction.TrinityModelLineItem>> donutLineItemPartitions = Lists.partition(donutLineItems, batchSize);
                    final CountDownLatch countDownLatch = new CountDownLatch(donutLineItemPartitions.size());
                    final ExecutorService executorService = Executors.newFixedThreadPool(parallelism);
                    donutLineItemPartitions.forEach(items -> executorService.submit(() -> {

                        try {
                            computeProcess(size, items);
                        } finally {
                            countDownLatch.countDown();
                        }

                    }));
                    countDownLatch.await();

                } else {
                    computeProcess(size, donutLineItems);
                }

            }
            action.getContext().put(trinityModel.getResponseAs().concat(".error"), "false");
        } catch (Exception e) {
            log.error(aMarker, "The Failure Response {} --> {}", trinityModel.getResponseAs(), e.getMessage(), e);
            action.getContext().put(trinityModel.getResponseAs().concat(".error"), "true");
            throw new HandymanException("Failure in trinity model response", e, action);
        }
    }

    private void computeProcess(final int nodeSize, final List<TrinityModelAction.TrinityModelLineItem> donutLineItems) {
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(trinityModel.getResourceConn());

        donutLineItems.forEach(donutLineItem -> {
            final String filePath = donutLineItem.getFilePath();
            final String paperType = donutLineItem.getPaperType();

            try {
                final List<String> questions = donutLineItem.getQuestions();
                final String node = nodes.get(counter.incrementAndGet() % nodeSize);

                if(log.isInfoEnabled()){
                    log.info(aMarker, "1. preparing {} for rest api call", questions.size());
                    log.info(aMarker, "2. info's are {}, {}, {}", filePath, paperType, questions);
                }

                final TrinityModelAction.TrinityModelResultLineItem trinityModelResultLineItem = new TrinityModelAction.TrinityModelApiCaller(node).compute(filePath, paperType, questions);

                log.info(aMarker, "completed {}", trinityModelResultLineItem.attributes.size());

                jdbi.useTransaction(handle -> {
                    String COLUMN_LIST = "process_id,file_path,question, predicted_attribution_value,b_box, image_dpi , image_width , image_height , extracted_image_unit , action_id, root_pipeline_id,status,stage,paper_type, score";
                    final PreparedBatch batch = handle.prepareBatch("INSERT INTO macro." + trinityModel.getResponseAs() + " (" + COLUMN_LIST + ") VALUES(" + action.getPipelineId() + ",:filePath,:question,:predictedAttributionValue, :bBoxes::json, :imageDpi, :imageWidth, :imageHeight , :extractedImageUnit, " + action.getActionId() + "," + action.getRootPipelineId() + ",:status,:stage,:paperType, :scores);");

                    Lists.partition(trinityModelResultLineItem.attributes, 100).forEach(resultLineItems -> {
                        log.info(aMarker, "inserting into trinity model_action {}", resultLineItems.size());
                        resultLineItems.forEach(resultLineItem -> {
                            batch.bind("filePath", filePath)
                                    .bind("question", resultLineItem.question)
                                    .bind("predictedAttributionValue", resultLineItem.predictedAttributionValue)
                                    .bind("scores", resultLineItem.scores)
                                    .bind("paperType", paperType)
                                    .bind("bBoxes", String.valueOf(resultLineItem.bboxes))
                                    .bind("imageDpi", trinityModelResultLineItem.imageDPI)
                                    .bind("imageWidth", trinityModelResultLineItem.imageWidth)
                                    .bind("imageHeight", trinityModelResultLineItem.imageHeight)
                                    .bind("extractedImageUnit", trinityModelResultLineItem.extractedImageUnit)
                                    .bind("status", "COMPLETED")
                                    .bind("stage", "VQA_TRANSACTION")
                                    .add();

                        });
                        int[] counts = batch.execute();
                        log.info(aMarker, " persisted {} in trinity model_action", counts);
                    });
                });
            } catch (Exception e) {
                jdbi.useTransaction(handle -> handle.createUpdate("INSERT INTO macro." + trinityModel.getResponseAs() + "_error (file_path,error_message, action_id, root_pipeline_id,process_id) VALUES(:filePath,:errorMessage, " + action.getActionId() + ", " + action.getRootPipelineId() + "," + action.getPipelineId() + ");")
                        .bind("filePath", filePath)
                        .bind("errorMessage", e.getMessage())
                        .execute());
                log.error(aMarker, "Error in inserting into trinity model action {}", ExceptionUtil.toString(e));
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("Error in trinity model action", handymanException, action);
            }

        });
    }

    @Override
    public boolean executeIf() throws Exception {
        return trinityModel.getCondition();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrinityModelLineItem {

        private String filePath;
        private List<String> questions;
        private String paperType;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrinityModelQueryResult {

        private String filePath;
        private String question;
        private String paperType;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrinityModelResultLineItem {
        //private String question;
        //private String predictedAttributionValue;
        //private JsonNode bBoxes;
        private List<TrinityModelAction.TrinityModelResult> attributes;
        private double imageDPI;
        private double imageWidth;
        private double imageHeight;
        private String extractedImageUnit;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrinityModelResult {
        private String question;
        private String predictedAttributionValue;
        private JsonNode bboxes;
        private float scores;
    }


    @Slf4j
    public static class TrinityModelApiCaller {

        private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
        private static final ObjectMapper MAPPER = new ObjectMapper();
        private final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
                .writeTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
                .readTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
                .build();
        private final String node;

        public TrinityModelApiCaller(final String node) {
            this.node = node;
        }

        protected TrinityModelAction.TrinityModelResultLineItem compute(final String inputPath, final String paperType, final List<String> questions) {
            final ObjectNode objectNode = MAPPER.createObjectNode();
            objectNode.put("inputFilePath", inputPath);
            objectNode.putPOJO("attributes", questions);
            objectNode.put("paperType", paperType);
//            objectNode.put("outputDir", outputDir);
            final Request request = new Request.Builder().url(node)
                    .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();
            log.info("Request URL : {} Question List size {}", node, questions.size());
            try (Response response = httpclient.newCall(request).execute()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {

                    TrinityModelAction.TrinityModelResultLineItem trinityModelResultLineItem = MAPPER.readValue(responseBody, new TypeReference<>() {
                    });
                    log.info("TrinityModelLineItem size {}", trinityModelResultLineItem.attributes.size());
                    return trinityModelResultLineItem;
                } else {
                    log.error("Error in the trinity model response {}", responseBody);
                    throw new HandymanException(responseBody);
                }
            } catch (Exception e) {
                log.error("Failed to execute the rest api call");
                throw new HandymanException("Failed to execute the rest api call " + node, e);
            }
        }
    }
}