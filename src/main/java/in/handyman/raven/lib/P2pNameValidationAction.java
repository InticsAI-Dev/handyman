package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.P2pNameValidation;
import in.handyman.raven.lib.model.p2pNameValidation.P2PNameValidationInputTable;
import in.handyman.raven.lib.model.p2pNameValidation.P2PNameValidationOutputTable;
import in.handyman.raven.lib.model.p2pNameValidation.P2pNameValidationConsumerProcess;
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
@ActionExecution(actionName = "P2pNameValidation")
public class P2pNameValidationAction implements IActionExecution {
    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger log;
    private final P2pNameValidation p2pNameValidation;
    public static final String READ_BATCH_SIZE = "read.batch.size";
    public static final String WRITE_BATCH_SIZE = "write.batch.size";
    public static final String THREAD_SLEEP_TIME = "1000";
    public static final String P2P_URL = "http://localhost:10181/copro/preprocess/autorotation";
    public static final String CONSUMER_API_COUNT = "p2p.name.concat.consumer.API.count";
    private final Marker aMarker;
    private final Integer threadSleepTime;
    private final Integer readBatchSize;
    private final Integer writeBatchSize;
    private final Integer consumerApiCount;
    private final String p2pUrl;

    public P2pNameValidationAction(final ActionExecutionAudit actionExecutionAudit, final Logger log, final Object p2pNameValidation) {
        this.p2pNameValidation = (P2pNameValidation) p2pNameValidation;
        this.actionExecutionAudit = actionExecutionAudit;
        this.log = log;
        this.threadSleepTime = Integer.parseInt(THREAD_SLEEP_TIME);
        String writeBatchSizeStr = this.actionExecutionAudit.getContext().get(WRITE_BATCH_SIZE);
        this.writeBatchSize = Integer.valueOf(writeBatchSizeStr);
        String consumerApiCountStr = this.actionExecutionAudit.getContext().get(CONSUMER_API_COUNT);
        consumerApiCount = Integer.valueOf(consumerApiCountStr);
        this.p2pUrl = P2P_URL;
        this.readBatchSize = Integer.valueOf(actionExecutionAudit.getContext().get(READ_BATCH_SIZE));
        this.aMarker = MarkerFactory.getMarker(" P2pNameValidation:" + this.p2pNameValidation.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(p2pNameValidation.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "p2p name concatenation action {} has been started ", p2pNameValidation.getName());

            final String insertQuery = "INSERT INTO " + p2pNameValidation.getOutputTable() +
                    "(origin_id, group_id, b_box, confidence_score, filter_score, maximum_score, extracted_value, " +
                    "paper_no, root_pipeline_id, tenant_id, sor_item_name, batch_id,question_id, synonym_id, model_registry) "
                    + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            log.info(aMarker, "p2p name concatenation insert query p2p name concatenation insert query {}", insertQuery);

            final List<URL> urls = Optional.ofNullable(p2pUrl).map(s -> Arrays.stream(s.split(",")).map(urlItem -> {
                try {
                    return new URL(urlItem);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL {}", urlItem, e);
                    throw new HandymanException("Error in processing the URL", e, actionExecutionAudit);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            final CoproProcessor<P2PNameValidationInputTable, P2PNameValidationOutputTable> coproProcessor = getP2PNameValidationInputTableP2PNameValidationOutputTableCoproProcessor(jdbi, urls);
            Thread.sleep(threadSleepTime);

            final P2pNameValidationConsumerProcess p2pNameValidationConsumerProcess = new P2pNameValidationConsumerProcess(actionExecutionAudit, log, aMarker);
            coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, p2pNameValidationConsumerProcess);
            log.info(aMarker, "P2P name concatenation action has been completed {}  ", p2pNameValidation.getName());
        } catch (Exception e) {
            actionExecutionAudit.getContext().put(p2pNameValidation.getName() + ".isSuccessful", "false");
            log.error(aMarker, "error in execute method for p2p name concatenation ", e);
            throw new HandymanException("error in execute method for p2p name concatenation", e, actionExecutionAudit);
        }
    }

    @NotNull
    private CoproProcessor<P2PNameValidationInputTable, P2PNameValidationOutputTable> getP2PNameValidationInputTableP2PNameValidationOutputTableCoproProcessor(Jdbi jdbi, List<URL> urls) {
        P2PNameValidationInputTable p2PNameValidationInputTable = new P2PNameValidationInputTable();
        final CoproProcessor<P2PNameValidationInputTable, P2PNameValidationOutputTable> coproProcessor = new CoproProcessor<>(new LinkedBlockingQueue<>(), P2PNameValidationOutputTable.class, P2PNameValidationInputTable.class, jdbi, log, p2PNameValidationInputTable, urls, actionExecutionAudit);
        coproProcessor.startProducer(p2pNameValidation.getQuerySet(), readBatchSize);
        return coproProcessor;
    }


    @Override
    public boolean executeIf() throws Exception {
        return p2pNameValidation.getCondition();
    }
}
