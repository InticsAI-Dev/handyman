package in.handyman.raven.lib;

import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AgenticPaperFilter;
import in.handyman.raven.lib.model.agentic.paper.filter.AgenticPaperFilterConsumerProcess;
import in.handyman.raven.lib.model.agentic.paper.filter.AgenticPaperFilterInput;
import in.handyman.raven.lib.model.agentic.paper.filter.AgenticPaperFilterOutput;
import in.handyman.raven.lib.utils.CustomBatchWithScaling;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@ActionExecution(actionName = "AgenticPaperFilter")
public class AgenticPaperFilterAction implements IActionExecution {

    private static final String DEFAULT_SOCKET_TIMEOUT = "100";
    private static final String INSERT_COLUMNS_UPDATED = "origin_id,group_id,tenant_id,template_id,process_id,file_path,extracted_text,container_name,container_value,paper_no,file_name,status,stage,message,is_blank_page,created_on,root_pipeline_id,template_name,model_name,model_version,batch_id,last_updated_on,request,response,endpoint,container_id,prompt_type";
    private static final String INSERT_QUERY_TEMPLATE = "INSERT INTO %s (%s) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String READ_BATCH_SIZE = "read.batch.size";
    private static final String WRITE_BATCH_SIZE = "write.batch.size";
    private static final String PAGE_CONTENT_MIN_LENGTH = "page.content.min.length.threshold";
    private static final String CONSUMER_API_COUNT_KEY = "agentic.paper.filter.consumer.API.count";
    private static final String FILE_PROCESS_FORMAT = "pipeline.copro.api.process.file.format";

    private final ActionExecutionAudit action;
    private final Logger log;
    private final Marker aMarker;
    private final AgenticPaperFilter filterConfig;
    private final String processBase64;
    private final int timeout;

    public AgenticPaperFilterAction(ActionExecutionAudit action, Logger log, Object config) {
        this.action = action;
        this.log = log;
        this.filterConfig = (AgenticPaperFilter) config;
        this.aMarker = MarkerFactory.getMarker("AgenticPaperFilter:" + filterConfig.getName());
        this.processBase64 = action.getContext().get(FILE_PROCESS_FORMAT);
        this.timeout = parseContextInt("copro.client.socket.timeout", DEFAULT_SOCKET_TIMEOUT);
    }

    @Override
    public void execute() {
        try {
            log.info(aMarker, "Starting Agentic Paper Filter Action for {}", filterConfig.getName());
            FileProcessingUtils fileUtils = new FileProcessingUtils(log, aMarker, action);
            log.info(aMarker, "Agentic Paper Filter execution started for {}", filterConfig.getName());

            String insertQuery = String.format(INSERT_QUERY_TEMPLATE, filterConfig.getResultTable(), INSERT_COLUMNS_UPDATED);
            List<URL> endpoints = parseEndpoints(filterConfig.getEndPoint());

            CoproProcessor<AgenticPaperFilterInput, AgenticPaperFilterOutput> processor = new CoproProcessor<>(
                    new LinkedBlockingQueue<>(),
                    AgenticPaperFilterOutput.class,
                    AgenticPaperFilterInput.class,
                    filterConfig.getResourceConn(),
                    log,
                    new AgenticPaperFilterInput(),
                    endpoints,
                    action
            );

            int consumerCount = getConsumerCount();

            int readBatchSize = getReadBatchSize(consumerCount);

            int writeBatchSize = parseContextInt(WRITE_BATCH_SIZE, "50");
            log.info(aMarker, "Parsed write batch size from context: {}", writeBatchSize);

            int pageContentMinLength = parseContextInt(PAGE_CONTENT_MIN_LENGTH, "1");
            log.info(aMarker, "Parsed page content minimum length from context: {}", pageContentMinLength);

            processor.startProducer(filterConfig.getQuerySet(), readBatchSize);

            AgenticPaperFilterConsumerProcess consumerProcess = new AgenticPaperFilterConsumerProcess(
                    log, aMarker, action, this, pageContentMinLength, fileUtils, processBase64, filterConfig.getResourceConn()
            );

            processor.startConsumer(insertQuery, consumerCount, writeBatchSize, consumerProcess);

            log.info(aMarker, "Agentic Paper Filter Action completed for {}", filterConfig.getName());
        } catch (Exception e) {
            log.error(aMarker, "Execution failed in Agentic Paper Filter", e);
            action.getContext().put(filterConfig.getName() + ".isSuccessful", "false");
            throw new HandymanException("Execution failed in Agentic Paper Filter", e, action);
        }
    }

    private int getConsumerCount() {
        int consumerCount = determineConsumerCount();
        log.info(aMarker, "Initial consumer count determined: {}", consumerCount);

        int contextConsumerCount = parseContextInt(CONSUMER_API_COUNT_KEY, "10");
        if (consumerCount < contextConsumerCount) {
            log.info(aMarker, "Consumer count {} is less than context override {}. Using max of both.", consumerCount, contextConsumerCount);
        }
        consumerCount = Math.max(consumerCount, contextConsumerCount);
        log.info(aMarker, "Final consumer count used: {}", consumerCount);
        return consumerCount;
    }

    private int getReadBatchSize(int consumerCount) {
        int readBatchSize = parseContextInt(READ_BATCH_SIZE, "10");
        log.info(aMarker, "Parsed read batch size from context: {}", readBatchSize);

        if (consumerCount >= readBatchSize) {
            log.info(aMarker, "Consumer count {} is greater than or equal to read batch size {}. Updating read batch size to match consumer count.", consumerCount, readBatchSize);
            readBatchSize = consumerCount;
        } else {
            log.info(aMarker, "Consumer count {} is less than read batch size {}. Keeping read batch size unchanged.", consumerCount, readBatchSize);
        }
        return readBatchSize;
    }

    @Override
    public boolean executeIf() {
        return filterConfig.getCondition();
    }

    public int getTimeOut() {
        return this.timeout;
    }

    private int parseContextInt(String key, String defaultVal) {
        String value = action.getContext().getOrDefault(key, defaultVal).trim();
        int result;
        if (value.isEmpty()) {
            result = Integer.parseInt(defaultVal);
            log.info("Context key '{}' is empty or missing. Using default value: {}", key, defaultVal);
        } else {
            result = Integer.parseInt(value);
            log.info("Context key '{}' found with value: {}. Parsed integer: {}", key, value, result);
        }
        return result;
    }


    private int determineConsumerCount() {
        CustomBatchWithScaling scaling = new CustomBatchWithScaling(action, log);
        int consumerCount;

        boolean podScalingCheckEnabled = scaling.isPodScalingCheckEnabled();
        log.info(aMarker, "Pod scaling check enabled: {}", podScalingCheckEnabled);
        if (podScalingCheckEnabled) {
            log.info(aMarker, "Using Kubernetes API to determine consumer count");
            consumerCount = scaling.computePaperFilterApiCount();
        } else {
            consumerCount = parseContextInt(CONSUMER_API_COUNT_KEY, "1");
        }
        log.info(aMarker, "Determined consumer count: {}", consumerCount);
        return consumerCount;
    }


    private List<URL> parseEndpoints(String endpointsStr) {
        if (endpointsStr == null || endpointsStr.isEmpty()) {
            log.warn("No endpoints provided in context; returning empty endpoint list.");
            return Collections.emptyList();
        }

        List<URL> urls = new ArrayList<>();
        String[] rawEndpoints = endpointsStr.split(",");
        log.debug("Parsing endpoints from context: '{}'", endpointsStr);

        for (String endpoint : rawEndpoints) {
            try {
                URL url = new URL(endpoint.trim());
                urls.add(url);
                log.debug("Parsed valid endpoint URL: {}", url);
            } catch (MalformedURLException e) {
                log.error("Invalid endpoint URL: {}", endpoint.trim(), e);
                throw new HandymanException("Invalid endpoint URL: " + endpoint.trim(), e, action);
            }
        }

        log.info("Successfully parsed {} endpoint(s).", urls.size());
        return urls;
    }

}
