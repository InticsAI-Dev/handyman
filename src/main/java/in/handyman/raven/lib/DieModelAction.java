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
import in.handyman.raven.lib.model.DieModel;
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

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "DieModel"
)
public class DieModelAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final DieModel dieModel;

    private final Marker aMarker;

    public static final String ATTRIBUTION_URL = "copro.die-attribution.url";

    private final AtomicInteger counter = new AtomicInteger();
    private static String httpClientTimeout = new String();
  private final List<String> nodes;

    public DieModelAction(final ActionExecutionAudit action, final Logger log,
                          final Object dieModel) {
        this.dieModel = (DieModel) dieModel;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" DieModel:" + this.dieModel.getName());
      this.nodes = Optional.ofNullable(action.getContext().get(ATTRIBUTION_URL)).map(s -> Arrays.asList(s.split(","))).orElse(Collections.emptyList());

    }

    @Override
    public void execute() throws Exception {


        try {
            log.info(aMarker, "DieModel Attribution Action for {} has been started", dieModel.getName());
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(dieModel.getResourceConn());
            final List<DieModelQueryResult> donutQueryResults = new ArrayList<>();
            jdbi.useTransaction(handle -> {
                final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(dieModel.getQuestionSql());
                formattedQuery.forEach(sqlToExecute -> donutQueryResults.addAll(handle.createQuery(sqlToExecute)
                        .mapToBean(DieModelQueryResult.class)
                        .stream().collect(Collectors.toList())));
            });

            // Create DDL

            jdbi.useTransaction(handle -> handle.execute("create table if not exists macro." + dieModel.getResponseAs() + " ( id bigserial not null, file_path text,question text, predicted_attribution_value text,b_box json null, image_dpi int8 null, image_width int8 null, image_height int8 null, extracted_image_unit varchar null, action_id bigint, root_pipeline_id bigint,process_id bigint, created_on timestamp not null default now(),status varchar NULL,stage varchar NULL ,paper_type varchar NULL);"));
            jdbi.useTransaction(handle -> handle.execute("create table if not exists macro." + dieModel.getResponseAs() + "_error ( id bigserial not null, file_path text,error_message text,  action_id bigint, root_pipeline_id bigint,process_id bigint, created_on timestamp not null default now() );"));
            final List<DieModelLineItem> donutLineItems = new ArrayList<>();

          Map<String,Map<String, List<DieModelQueryResult>>> listDieModelQueryResult = donutQueryResults.stream().collect(Collectors.groupingBy(DieModelQueryResult::getFilePath,Collectors.groupingBy(DieModelQueryResult::getPaperType)));

            listDieModelQueryResult.forEach((s, stringListMap) -> {
                stringListMap.forEach((s1, donutQueryResults1) -> donutLineItems.add(DieModelLineItem.builder()
                        .filePath(s).paperType(s1).questions(donutQueryResults1.stream().map(DieModelQueryResult::getQuestion).collect(Collectors.toList()))
                        .build()));
            });

            doProcess(donutLineItems);
        } catch (Exception e) {
            log.error(aMarker, "Error in donut attribution action", e);
            throw new HandymanException("Error in donut attribution action", e, action);
        }

    }

    private void doProcess(final List<DieModelLineItem> donutLineItems) {
        final int parallelism;
        if (dieModel.getForkBatchSize() != null) {
            parallelism = Integer.parseInt(dieModel.getForkBatchSize());
        } else {
            parallelism = 1;
        }
        final int size = nodes.size();
        try {
            if (size > 0) {
                final int batchSize = donutLineItems.size() / parallelism;
                if (parallelism > 1 && size > 1 && batchSize > 0) {
                    final List<List<DieModelLineItem>> donutLineItemPartitions = Lists.partition(donutLineItems, batchSize);
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
            action.getContext().put(dieModel.getResponseAs().concat(".error"), "false");
        } catch (Exception e) {
            log.error(aMarker, "The Failure Response {} --> {}", dieModel.getResponseAs(), e.getMessage(), e);
            action.getContext().put(dieModel.getResponseAs().concat(".error"), "true");
            throw new HandymanException("Failure in donut response", e, action);
        }
    }

    private void computeProcess(final int nodeSize, final List<DieModelLineItem> donutLineItems) {
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(dieModel.getResourceConn());

        donutLineItems.forEach(donutLineItem -> {
            final String filePath = donutLineItem.getFilePath();
            final String paperType = donutLineItem.getPaperType();

            try {
                final List<String> questions = donutLineItem.getQuestions();
                final String node = nodes.get(counter.incrementAndGet() % nodeSize);

                log.info(aMarker, "preparing {} for rest api call", questions.size());
                final DieModelApiCaller dieModelApiCaller = new DieModelApiCaller(node);

                DieModelResultLineItem lineItems=dieModelApiCaller .compute(filePath, questions,paperType);
                log.info(aMarker, "completed {}", lineItems.attributes.size());

                jdbi.useTransaction(handle -> {
                  String COLUMN_LIST = "process_id,file_path,question, predicted_attribution_value,b_box, image_dpi , image_width , image_height , extracted_image_unit , action_id, root_pipeline_id,status,stage,paper_type";
                  final PreparedBatch batch = handle.prepareBatch("INSERT INTO macro." + dieModel.getResponseAs() + " (" + COLUMN_LIST + ") VALUES(" + action.getPipelineId() + ",:filePath,:question,:predictedAttributionValue, :bBoxes::json, :imageDpi, :imageWidth, :imageHeight , :extractedImageUnit, " + action.getActionId() + "," + action.getRootPipelineId() + ",:status,:stage,:paperType);");

                    Lists.partition(lineItems.attributes, 100).forEach(resultLineItems -> {
                        log.info(aMarker, "inserting into donut_docqa_action {}", resultLineItems.size());
                        resultLineItems.forEach(resultLineItem -> {
                            batch.bind("filePath", filePath)
                                    .bind("question", resultLineItem.question)
                                    .bind("predictedAttributionValue", resultLineItem.predictedAttributionValue)
                                    .bind("paperType",paperType)
                                    .bind("bBoxes", String.valueOf(resultLineItem.bboxes))
                                    .bind("imageDpi", lineItems.imageDPI)
                                    .bind("imageWidth", lineItems.imageWidth)
                                    .bind("imageHeight", lineItems.imageHeight)
                                    .bind("extractedImageUnit", lineItems.extractedImageUnit)
                                    .bind("status", "COMPLETED")
                                    .bind("stage", "VQA_TRANSACTION")
                                    .add();

                        });
                        int[] counts = batch.execute();
                        log.info(aMarker, " persisted {} in donut_docqa_action", counts);
                    });
                });
            } catch (Exception e) {
                jdbi.useTransaction(handle -> handle.createUpdate("INSERT INTO macro." + dieModel.getResponseAs() + "_error (file_path,error_message, action_id, root_pipeline_id,process_id) VALUES(:filePath,:errorMessage, " + action.getActionId() + ", " + action.getRootPipelineId() + "," + action.getPipelineId() + ");")
                        .bind("filePath", filePath)
                        .bind("errorMessage", e.getMessage())
                        .execute());
                log.error(aMarker, "Error in inserting into docqa action {}", ExceptionUtil.toString(e));
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("Error in donut docQa action", handymanException, action);
            }

        });

    }

    @Override
    public boolean executeIf() throws Exception {
        return dieModel.getCondition();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DieModelLineItem {

        private String filePath;
        private List<String> questions;
        private String paperType;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DieModelQueryResult {

        private String filePath;
        private String question;
        private String paperType;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DieModelResultLineItem {
        //private String question;
        //private String predictedAttributionValue;
        //private JsonNode bBoxes;
        private List<DieModelResult> attributes;
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
    public static class DieModelResult {
        private String question;
        private String predictedAttributionValue;
        private JsonNode bboxes;
      private float  scores;
    }


    @Slf4j
    public static class DieModelApiCaller {

        private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
        private static final ObjectMapper MAPPER = new ObjectMapper();
        private final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
                .writeTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
                .readTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
                .build();
        private final String node;

        public DieModelApiCaller(final String node) {
            this.node = node;
        }

        protected DieModelResultLineItem compute(final String inputPath, final List<String> questions,final String paperType) {
            final ObjectNode objectNode = MAPPER.createObjectNode();
            objectNode.put("inputFilePath", inputPath);
            objectNode.putPOJO("attributes", questions);
            objectNode.put("paperType",paperType);
//            objectNode.put("outputDir", outputDir);
            final Request request = new Request.Builder().url(node)
                    .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();
            log.info("Request URL : {} Question List size {}", node, questions.size());
            try (Response response = httpclient.newCall(request).execute()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {

                    DieModelResultLineItem donutLineItems = MAPPER.readValue(responseBody, new TypeReference<>() {
                    });
                    log.info("DieModelLineItem size {}", donutLineItems.attributes.size());
                    return donutLineItems;
                } else {
                    log.error("Error in the donut docqa response {}", responseBody);
                    throw new HandymanException(responseBody);
                }
            } catch (Exception e) {
                log.error("Failed to execute the rest api call");
                throw new HandymanException("Failed to execute the rest api call " + node, e);
            }
        }
    }
}
