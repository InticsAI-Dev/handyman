package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TrinityModelExecutor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "TrinityModelExecutor"
)
public class TrinityModelExecutorAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final TrinityModelExecutor trinityModelExecutor;

    private final Marker aMarker;

    public static final String COLUMN_LIST = "process_id,file_path, question, predicted_attribution_value,b_box, image_dpi , image_width , image_height , extracted_image_unit , action_id, root_pipeline_id,status,stage,paper_type, score,model_name,model_version,tenant_id, model_registry";
    public static final String CREATE_TABLE_COLUMN = "id bigserial not null, file_path text,question text, predicted_attribution_value text , score float8 NULL, b_box json null, image_dpi int8 null, image_width int8 null, image_height int8 null, extracted_image_unit varchar null, action_id bigint, root_pipeline_id bigint,process_id bigint, created_on timestamp not null default now(),status varchar NULL,stage varchar NULL ,paper_type varchar NULL,tenant_id int8 null, model_registry varchar NULL,model_name varchar null,model_version varchar null";
    public static final String CREATE_ERROR_TABLE_COLUMN = "id bigserial not null, file_path text,error_message text,  action_id bigint, root_pipeline_id bigint,process_id bigint, created_on timestamp not null default now() ,tenant_id int8 null";
    private static String httpClientTimeout = new String();
    public static final String READ_BATCH_SIZE = "read.batch.size";
    public static final String WRITE_BATCH_SIZE = "write.batch.size";


    public TrinityModelExecutorAction(final ActionExecutionAudit action, final Logger log,
                                      final Object trinityModelExecutor) {
        this.trinityModelExecutor = (TrinityModelExecutor) trinityModelExecutor;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" TrinityModelExecutor:" + this.trinityModelExecutor.getName());
        httpClientTimeout = Optional.ofNullable(action.getContext().get("okhttp.client.timeout")).orElse("100");
    }

    @Override
    public void execute() throws Exception {
        try {
            log.info(aMarker, "Trinity Model Executor Action has been started {}", trinityModelExecutor);

            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(trinityModelExecutor.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            final String outputDir = Optional.ofNullable(trinityModelExecutor.getOutputDir()).map(String::valueOf).orElse(null);
            log.info(aMarker, "Trinity Model Executor Action output directory {}", outputDir);

            final String insertQuery = "INSERT INTO macro." + trinityModelExecutor.getResponseAs() +
                    "(" + COLUMN_LIST + ") " +
                    " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            log.info(aMarker, "trinity model executor Insert query {}", insertQuery);

            String trinityUrl = trinityModelExecutor.getRequestUrl();
            final List<URL> urls = Optional.ofNullable(trinityUrl).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new HandymanException("Error in processing the URL", e, action);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());
            log.info(aMarker, "trinity executor copro urls {}", urls);

            jdbi.useTransaction(handle -> handle.execute("create table if not exists macro." + trinityModelExecutor.getResponseAs() + " ( " + CREATE_TABLE_COLUMN + ");"));
            jdbi.useTransaction(handle -> handle.execute("create table if not exists macro." + trinityModelExecutor.getResponseAs() + "_error ( " + CREATE_ERROR_TABLE_COLUMN + ");"));

            Integer consumerApiCount = Integer.valueOf(trinityModelExecutor.getForkBatchSize());
            final CoproProcessor<TrinityModelExecutorInputTable, TrinityModelExecutorOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            TrinityModelExecutorOutputTable.class,
                            TrinityModelExecutorInputTable.class,
                            jdbi, log,
                            new TrinityModelExecutorInputTable(), urls, action, consumerApiCount);

            log.info(aMarker, "trinity model executor copro Processor initialization  {}", coproProcessor);

            //4. call the method start producer from coproprocessor
            Integer readBatchSize = Integer.valueOf(action.getContext().get(READ_BATCH_SIZE));
            Integer writeBatchSize = Integer.valueOf(action.getContext().get(WRITE_BATCH_SIZE));

            coproProcessor.startProducer(trinityModelExecutor.getQuestionSql(), readBatchSize);
            log.info(aMarker, "trinity model executor coproProcessor startProducer called read batch size {}", readBatchSize);
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, new TrinityModelExecutorConsumerProcess(log, aMarker, action));
            log.info(aMarker, "trinity model executor coproProcessor startConsumer called consumer count {} write batch count {} ", consumerApiCount, writeBatchSize);

        } catch (Exception ex) {
            log.error(aMarker, "error in execute method for trinity model executor  ", ex);
            throw new HandymanException("error in execute method for trinity model executor", ex, action);
        }

    }

    @Override
    public boolean executeIf() throws Exception {
        return trinityModelExecutor.getCondition();
    }



    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrinityModelExecutorInputTable implements CoproProcessor.Entity {

        private String filePath;
        private List<String> questions;
        private String paperType;
        private String modelRegistry;
        private Long tenantId;

        @Override
        public List<Object> getRowData() {
            return null;
        }
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrinityModelExecutorOutputTable implements CoproProcessor.Entity {

        private Long processId;
        private String filePath;
        private String question;
        private String predictedAttributionValue;
        private JsonNode bBox; // Change data type to JsonNode
        private Integer imageDPI;
        private Integer imageWidth;
        private Integer imageHeight;
        private String extractedImageUnit;
        private Long actionId;
        private Long rootPipelineId;
        private String status;
        private String stage;
        private String paperType;
        private Double score;
        private String modelName;
        private String modelVersion;
        private Long tenantId;
        private String modelRegistry;

        @Override
        public List<Object> getRowData() {
            return Stream.of(this.processId, this.filePath, this.question, this.predictedAttributionValue, this.bBox,
                    this.imageDPI, this.imageWidth, this.imageHeight, this.extractedImageUnit, this.actionId, this.rootPipelineId, this.status,
                    this.stage, this.paperType, this.score, this.modelName, this.modelVersion, this.tenantId, this.modelRegistry).collect(Collectors.toList());
        }
    }
}
