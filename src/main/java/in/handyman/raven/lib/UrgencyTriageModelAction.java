

package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.UrgencyTriageModel;
import in.handyman.raven.lib.model.utmodel.UrgencyTriageConsumerProcess;
import in.handyman.raven.lib.model.utmodel.UrgencyTriageInputTable;
import in.handyman.raven.lib.model.utmodel.UrgencyTriageOutputTable;
import in.handyman.raven.util.ExceptionUtil;
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


/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "UrgencyTriageModel"
)
public class UrgencyTriageModelAction implements IActionExecution {
    public static final String COPRO_URGENCY_TRIAGE_MODEL_URL = "copro.urgency-triage-model.url";
    private final ActionExecutionAudit action;
    private final Logger log;
    public static UrgencyTriageModel urgencyTriageModel = new UrgencyTriageModel();
    private final Marker aMarker;
    private final String URI;


    public UrgencyTriageModelAction(final ActionExecutionAudit action, final Logger log,
                                    final Object UrgencyTriageModel) {
        UrgencyTriageModelAction.urgencyTriageModel = (UrgencyTriageModel) UrgencyTriageModel;
        this.action = action;
        this.log = log;
        this.URI = action.getContext().get(COPRO_URGENCY_TRIAGE_MODEL_URL);
        this.aMarker = MarkerFactory.getMarker(" UrgencyTriageModel:" + UrgencyTriageModelAction.urgencyTriageModel.getName());
    }


    @Override
    public void execute() throws Exception {
        try {
            Integer writeBatchSize = Integer.valueOf(action.getContext().get("write.batch.size"));
            Integer consumerCount = Integer.valueOf(action.getContext().get("ut.consumer.API.count"));
            Integer readBatchSize = Integer.valueOf(action.getContext().get("read.batch.size"));
            String outputDir = urgencyTriageModel.getOutputDir();
            UrgencyTriageConsumerProcess urgencyTriageConsumerProcess = new UrgencyTriageConsumerProcess(log, aMarker, action);
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(urgencyTriageModel.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "Urgency Triage Action for {} has been started", urgencyTriageModel.getName());
            final String insertQuery = "INSERT INTO "+urgencyTriageModel.getOutputTable()+" (created_on, created_user_id, last_updated_on, last_updated_user_id, process_id, group_id, tenant_id, confidence_score, origin_id, paper_no, template_id, model_id, status, stage, message, paper_type, bboxes, root_pipeline_id, model_name,model_version,batch_id)" +
                    "values(now(),?,now(),?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?,?,?,?)";
            final List<URL> urls = Optional.ofNullable(urgencyTriageModel.getEndPoint()).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            final CoproProcessor<UrgencyTriageInputTable, UrgencyTriageOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            UrgencyTriageOutputTable.class,
                            UrgencyTriageInputTable.class,
                            jdbi, log,
                            new UrgencyTriageInputTable(), urls, action);

            coproProcessor.startProducer(urgencyTriageModel.getQuerySet(), readBatchSize);
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, consumerCount, writeBatchSize, urgencyTriageConsumerProcess);
            log.info(aMarker, "Urgency Triage has been completed {}  ", urgencyTriageModel.getName());
        } catch (Exception t) {
            action.getContext().put(urgencyTriageModel.getName() + ".isSuccessful", "false");
            log.error(aMarker, "Error at urgency triage execute method {}", ExceptionUtil.toString(t));
            throw new HandymanException("Error at Urgency triage model execute method ", t, action);

        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return urgencyTriageModel.getCondition();
    }


}