package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AgenticPaperFilter;
import in.handyman.raven.lib.model.agentic.paper.filter.AgenticPaperFilterConsumerProcess;
import in.handyman.raven.lib.model.agentic.paper.filter.AgenticPaperFilterInput;
import in.handyman.raven.lib.model.agentic.paper.filter.AgenticPaperFilterOutput;
import in.handyman.raven.lib.utils.CustomBatchWithScaling;
import lombok.*;
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

import static in.handyman.raven.core.enums.DatabaseConstants.DB_INSERT_WRITE_BATCH_SIZE;
import static in.handyman.raven.core.enums.DatabaseConstants.DB_SELECT_READ_BATCH_SIZE;

@ActionExecution(actionName = "AgenticPaperFilter")
public class AgenticPaperFilterAction implements IActionExecution {

    public static final String INSERT_COLUMNS = "origin_id,group_id,tenant_id,template_id,process_id, file_path, extracted_text,container_name,container_value,paper_no,file_name, status,stage,message,is_blank_page, created_on ,root_pipeline_id,template_name,model_name,model_version,batch_id, last_updated_on,request,response,endpoint";
    public static final String INSERT_COLUMNS_UPDATED = "origin_id,group_id,tenant_id,template_id,process_id, file_path, extracted_text,container_name,container_value,paper_no,file_name, status,stage,message,is_blank_page, created_on ,root_pipeline_id,template_name,model_name,model_version,batch_id, last_updated_on,request,response,endpoint,container_id,prompt_type";
    private static final String DEFAULT_SOCKET_TIMEOUT = "100";

    public static final String INSERT_INTO = "INSERT INTO ";
    public static final String INSERT_INTO_VALUES = "VALUES(?,? ,?,?,? ,?,?,?,?, ?,?,?,?,? ,?, ?,?,?,?,  ?,?,?,?,?,?)";
    public static final String INSERT_INTO_VALUES_UPDATED = "VALUES(?,? ,?,?,? ,?,?,?,?, ?,?,?,?,? ,?, ?,?,?,?,  ?,?,?,?,?,?,?,?)";

    public static final String AGENTIC_PAPER_FILTER_CONSUMER_API_COUNT = "agentic.paper.filter.consumer.API.count";
    public static final String PAGE_CONTENT_MIN_LENGTH = "page.content.min.length.threshold";
    private final ActionExecutionAudit action;
    public static final String COPRO_FILE_PROCESS_FORMAT = "pipeline.copro.api.process.file.format";

    private final Logger log;

    private final AgenticPaperFilter agenticPaperFilter;
    private final Marker aMarker;
    private final String processBase64;
    private final int timeout;
    @Getter
    private final String httpClientType;


    public AgenticPaperFilterAction(final ActionExecutionAudit action, final Logger log, final Object agenticPaperFilter) {
        this.agenticPaperFilter = (AgenticPaperFilter) agenticPaperFilter;
        this.action = action;
        this.log = log;
        this.processBase64 = action.getContext().get(COPRO_FILE_PROCESS_FORMAT);
        this.timeout = parseContextValue(action, "copro.client.socket.timeout", DEFAULT_SOCKET_TIMEOUT);
        this.httpClientType = parseContextValueStr(action, "copro.http.client.type", "default");


        this.aMarker = MarkerFactory.getMarker(" AgenticPaperFilter:" + this.agenticPaperFilter.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(agenticPaperFilter.getResourceConn());
            FileProcessingUtils fileProcessingUtils = new FileProcessingUtils(log, aMarker, action);

            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "Agentic Paper Filter Action for {} has been started", agenticPaperFilter.getName());


            String outputTableName = agenticPaperFilter.getResultTable();
            final String insertQuery = INSERT_INTO + outputTableName + " ( " + INSERT_COLUMNS_UPDATED + " ) " + INSERT_INTO_VALUES_UPDATED;
            final List<URL> urls = Optional.ofNullable(agenticPaperFilter.getEndPoint()).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new HandymanException("Error in processing the URL", e, action);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            final CoproProcessor<AgenticPaperFilterInput, AgenticPaperFilterOutput> coproProcessor = new CoproProcessor<>(new LinkedBlockingQueue<>(), AgenticPaperFilterOutput.class, AgenticPaperFilterInput.class, agenticPaperFilter.getResourceConn(), log, new AgenticPaperFilterInput(), urls, action);

            int consumerApiCount = 0;
            CustomBatchWithScaling customBatchWithScaling = new CustomBatchWithScaling(action, log);
            boolean isPodScalingCheckEnabled = customBatchWithScaling.isPodScalingCheckEnabled();
            if (isPodScalingCheckEnabled) {
                log.info(aMarker, "Pod Scaling Check is enabled, computing consumer API count using CustomBatchWithScaling");
                consumerApiCount = customBatchWithScaling.computePaperFilterApiCount();
            }

            String apiCountValue = (agenticPaperFilter.getForkBatchSize() != null && !agenticPaperFilter.getForkBatchSize().isEmpty()) ? agenticPaperFilter.getForkBatchSize().trim() : "1";
            int apiCount = Integer.parseInt(apiCountValue);

            if (consumerApiCount < apiCount) {
                consumerApiCount = apiCount;
            }

            log.info(aMarker, "Consumer API count for Agentic Paper Filter is {}", consumerApiCount);

            String readBatchSizeDefaultValue = "10";
            String value = action.getContext().getOrDefault(DB_SELECT_READ_BATCH_SIZE, readBatchSizeDefaultValue).trim();
            int readBatchSize = value.isEmpty() ? Integer.parseInt(readBatchSizeDefaultValue) : Integer.parseInt(value);

            if (consumerApiCount >= readBatchSize) {
                log.info(aMarker, "Consumer API count {} is greater than read batch size {}, setting read batch size to consumer API count", consumerApiCount, readBatchSize);
                readBatchSize = consumerApiCount;
            } else {
                log.info(aMarker, "Consumer API count {} is less than or equal to read batch size {}, keeping read batch size as is", consumerApiCount, readBatchSize);
            }

            coproProcessor.startProducer(agenticPaperFilter.getQuerySet(), readBatchSize);
            Thread.sleep(1000);

            Integer writeBatchSize = Integer.valueOf(action.getContext().get(DB_INSERT_WRITE_BATCH_SIZE));
            Integer pageContentMinLength = Integer.valueOf(action.getContext().get(PAGE_CONTENT_MIN_LENGTH));
            AgenticPaperFilterConsumerProcess agenticPaperFilterConsumerProcess =
                    new AgenticPaperFilterConsumerProcess(log, aMarker, action, this, pageContentMinLength, fileProcessingUtils, processBase64, agenticPaperFilter.getResourceConn());


            coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, agenticPaperFilterConsumerProcess);
            log.info(aMarker, " Agentic Paper Filter Action has been completed {}  ", agenticPaperFilter.getName());
        } catch (Exception e) {
            action.getContext().put(agenticPaperFilter.getName() + ".isSuccessful", "false");
            log.error(aMarker, "error in execute method in Agentic Paper Filter", e);
            throw new HandymanException("error in execute method in Agentic Paper Filter", e, action);
        }

    }

    private int parseContextValue(ActionExecutionAudit action, String key, String defaultValue) {
        String value = action.getContext().getOrDefault(key, defaultValue).trim();
        return value.isEmpty() ? Integer.parseInt(defaultValue) : Integer.parseInt(value);
    }

    private String parseContextValueStr(ActionExecutionAudit action, String key, String defaultValue) {
        String value = action.getContext().getOrDefault(key, defaultValue).trim();
        return value.isEmpty() ? defaultValue : value;
    }


    @Override
    public boolean executeIf() throws Exception {
        return agenticPaperFilter.getCondition();
    }

    public int getTimeOut() {
        return this.timeout;
    }

    @Data
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssetAttributionResponse {
        private String pageContent;
    }

}
