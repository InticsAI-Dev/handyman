package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.UrgencyTriageBeta;

import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import in.handyman.raven.lib.model.urgencyTriageBeta.UrgencyTriageBetaConsumerProcess;
import in.handyman.raven.lib.model.urgencyTriageBeta.UrgencyTriageBetaOutputTable;
import in.handyman.raven.lib.model.utmodel.UrgencyTriageInputTable;
import in.handyman.raven.util.ExceptionUtil;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "UrgencyTriageBeta"
)
public class UrgencyTriageBetaAction implements IActionExecution {

  private final ActionExecutionAudit action;

  private final Logger log;

  public static UrgencyTriageBeta urgencyTriageBeta = new UrgencyTriageBeta();

  private final Marker aMarker;

  public UrgencyTriageBetaAction(final ActionExecutionAudit action, final Logger log,
                                 final Object urgencyTriageBeta) {
    UrgencyTriageBetaAction.urgencyTriageBeta = (UrgencyTriageBeta) urgencyTriageBeta;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" UrgencyTriageBeta:" + UrgencyTriageBetaAction.urgencyTriageBeta.getName());
  }

  @Override
  public void execute() throws Exception {
    try {
      final Integer writeBatchSize = Integer.valueOf(action.getContext().get("write.batch.size"));
      final Integer consumerCount = Integer.valueOf(action.getContext().get("ut.consumer.API.count"));
      final Integer readBatchSize = Integer.valueOf(action.getContext().get("read.batch.size"));
      final UrgencyTriageBetaConsumerProcess urgencyTriageConsumerProcess = new UrgencyTriageBetaConsumerProcess(log, aMarker, action);
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(urgencyTriageBeta.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      log.info(aMarker, "Urgency Triage Action for {} has been started", urgencyTriageBeta.getName());
      final String insertQuery = "INSERT INTO " + urgencyTriageBeta.getOutputTable() + "(created_on, created_user_id, last_updated_on, " +
              "last_updated_user_id, process_id, group_id, tenant_id, origin_id, paper_no, template_id, model_id, status, stage, message, " +
              "root_pipeline_id, model_version, batch_id, checkbox_result, hw_result, binary_result, checkbox_bbox, hw_bbox, binary_bbox, " +
              "checkbox_score, hw_score, binary_score, model_name)" +
              "VALUES (now(), ?, now(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
      final List<URL> urls = Optional.ofNullable(urgencyTriageBeta.getEndPoint()).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
        try {
          return new URL(s1);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL ", e);
          throw new RuntimeException(e);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());

      final CoproProcessor<UrgencyTriageInputTable, UrgencyTriageBetaOutputTable> coproProcessor =
              new CoproProcessor<>(new LinkedBlockingQueue<>(),
                      UrgencyTriageBetaOutputTable.class,
                      UrgencyTriageInputTable.class,
                      jdbi, log,
                      new UrgencyTriageInputTable(), urls, action);

      coproProcessor.startProducer(urgencyTriageBeta.getQuerySet(), readBatchSize);
      Thread.sleep(1000);
      coproProcessor.startConsumer(insertQuery, consumerCount, writeBatchSize, urgencyTriageConsumerProcess);
      log.info(aMarker, "Urgency Triage has been completed {}  ", urgencyTriageBeta.getName());
    } catch (Exception t) {
      action.getContext().put(urgencyTriageBeta.getName() + ".isSuccessful", "false");
      log.error(aMarker, "Error at urgency triage execute method {}", ExceptionUtil.toString(t));
      throw new HandymanException("Error at Urgency triage model execute method ", t, action);

    }
  }

  @Override
  public boolean executeIf() throws Exception {
    return urgencyTriageBeta.getCondition();
  }
}
