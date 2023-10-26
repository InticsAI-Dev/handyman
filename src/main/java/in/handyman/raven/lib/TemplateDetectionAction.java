package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.templateDetection.TemplateDetectionConsumerProcess;
import in.handyman.raven.lib.model.templateDetection.TemplateDetectionInputTable;
import in.handyman.raven.lib.model.templateDetection.TemplateDetectionOutputTable;
import in.handyman.raven.lib.model.TemplateDetection;
import in.handyman.raven.util.ExceptionUtil;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.jetbrains.annotations.NotNull;
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

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "TemplateDetection"
)
public class TemplateDetectionAction implements IActionExecution {

    private final ActionExecutionAudit action;

    private final Logger log;

    private final TemplateDetection templateDetection;

    private final Marker aMarker;
    public static final String DEFAULT_BATCH_SIZE = "100";
    public static final String READ_BATCH_SIZE = "read.batch.size";
    public static final String WRITE_BATCH_SIZE = "read.batch.size";
    public static final String CONSUMER_API_COUNT = "template.detection.consumer.API.count";
    public static final String INSERT_INTO = "INSERT INTO ";
    public static final String SCHEMA_NAME = "macro";
    public static final String COLUMN_LIST = "process_id, origin_id, paper_no, group_id, processed_file_path, question, predictedattribution_value, score, bboxes, image_width, image_height, image_dpi, extracted_image_unit, tenant_id, template_id, status, stage, message, created_on, root_pipeline_id,model_name,model_version";
    public static final String VAL_STRING_LIST = "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String THREAD_SLEEP_TIME = "1000";
    public static final String COPRO_CLIENT_SOCKET_TIMEOUT = "copro.client.socket.timeout";
    private final String DEFAULT_THREAD_SLEEP = "1000";
    private final Integer writeBatchSize;
    private final int readBatchSize;
    private final Integer consumerApiCount;
    private final String insertQuery;
    public static final String COPRO_CLIENT_API_SLEEPTIME = "copro.client.api.sleeptime";
    public static final String DEFAULT_SOCKET_TIME_OUT = "1000";
    private final String schemaName;
    private final String targetTableName;
    private final String columnList;
    private final Integer threadSleepTime;
    private int timeout;


    public TemplateDetectionAction(final ActionExecutionAudit action, final Logger log,
                                   final Object templateDetection) {
        this.templateDetection = (TemplateDetection) templateDetection;
        this.action = action;
        this.log = log;
        this.schemaName = SCHEMA_NAME;
        this.targetTableName = ((TemplateDetection) templateDetection).getOuputTable();
        this.columnList = COLUMN_LIST;
        String readBatchSizeStr = Optional.ofNullable(this.action.getContext().get(READ_BATCH_SIZE)).orElse(DEFAULT_BATCH_SIZE);
        String writeBatchSizeStr = Optional.ofNullable(this.action.getContext().get(WRITE_BATCH_SIZE)).orElse(DEFAULT_BATCH_SIZE);
        String consumerCount = Optional.ofNullable(this.action.getContext().get(CONSUMER_API_COUNT)).orElse("1000");
        String socketTimeStr= Optional.ofNullable(this.action.getContext().get(COPRO_CLIENT_SOCKET_TIMEOUT)).orElse(DEFAULT_SOCKET_TIME_OUT);
        String threadSleep = Optional.ofNullable(this.action.getContext().get(THREAD_SLEEP_TIME)).orElse(DEFAULT_THREAD_SLEEP);

        insertQuery = INSERT_INTO +  targetTableName + "(" + columnList + ")" + " " + VAL_STRING_LIST;
        this.timeout = Integer.parseInt(socketTimeStr);
        this.writeBatchSize = Integer.parseInt(writeBatchSizeStr);
        this.readBatchSize = Integer.parseInt(readBatchSizeStr);
        this.consumerApiCount = Integer.parseInt(consumerCount);
        this.aMarker = MarkerFactory.getMarker(" TemplateDetection:" + this.templateDetection.getName());
        this.threadSleepTime = Integer.parseInt(threadSleep);

    }

    @Override
    public void execute() throws Exception {
        try {
            final String jdbiStr = templateDetection.getResourceConn();
            final String nameStr = templateDetection.getName();
            final String querysetStr = templateDetection.getQuerySet();
            final String coproUrlStr = templateDetection.getCoproUrl();

            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(jdbiStr);
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "Template detection macro started {}", templateDetection);

            final List<URL> urls = Optional.ofNullable(coproUrlStr).map(s -> Arrays.stream(s.split(",")).map(urlItem -> {
                try {
                    return new URL(urlItem);
                } catch (MalformedURLException e) {
                    log.error("Error in Template detection while processing the URL " + urlItem, e);
                    throw new HandymanException("Error in Template detection while  processing the URL", e, action);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            final CoproProcessor<TemplateDetectionInputTable, TemplateDetectionOutputTable> coproProcessor = getTemplateDetectionCoproProcessor(jdbi, urls, querysetStr);
            log.info(aMarker, "Copro processor start compose completed {}", nameStr);
            Thread.sleep(threadSleepTime);
            final TemplateDetectionConsumerProcess templateDetectionConsumerProcess = new TemplateDetectionConsumerProcess(log, aMarker, action, this);
            coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, templateDetectionConsumerProcess);

        } catch (Exception e) {
            log.error(aMarker, "Error in execute method for template detection {}", ExceptionUtil.toString(e));
            throw new HandymanException("Error in execute method for template detection {}", e, action);
        }


    }

    @NotNull
    private CoproProcessor<TemplateDetectionInputTable, TemplateDetectionOutputTable> getTemplateDetectionCoproProcessor(Jdbi jdbi, List<URL> urls, String querysetStr) {
        final TemplateDetectionInputTable templateDetectionInputTable = new TemplateDetectionInputTable();
        final CoproProcessor<TemplateDetectionInputTable, TemplateDetectionOutputTable> coproProcessor =
                new CoproProcessor<>(new LinkedBlockingQueue<>(),
                        TemplateDetectionOutputTable.class,
                        TemplateDetectionInputTable.class,
                        jdbi, log,
                        templateDetectionInputTable, urls, action);


        coproProcessor.startProducer(querysetStr, readBatchSize);
        return coproProcessor;
    }

    @Override
    public boolean executeIf() throws Exception {
        return templateDetection.getCondition();
    }
    private List<URL> extractCoproEndPoints(String endpoint) {
        final List<URL> urls;
        if (endpoint.isEmpty() && endpoint.isBlank()) {
            urls = Optional.of(endpoint).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new HandymanException("Error in processing the URL", e, action);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());
            log.info(aMarker, "paper itemizer copro urls {}", urls);
        } else {
            log.info(aMarker, "paper itemizer copro url not found");
            return Collections.emptyList();

        }
        return urls;
    }


    public Integer getTimeOut() {
        return this.timeout;
    }
}
