package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ThemisCompliance;
import in.handyman.raven.lib.model.themis.compliance.ThemisComplianceConsumerProcess;
import in.handyman.raven.lib.model.themis.compliance.ThemisComplianceInputTable;
import in.handyman.raven.lib.model.themis.compliance.ThemisComplianceOutputTable;
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

import static in.handyman.raven.core.enums.DatabaseConstants.DB_INSERT_WRITE_BATCH_SIZE;
import static in.handyman.raven.core.enums.DatabaseConstants.DB_SELECT_READ_BATCH_SIZE;
import static in.handyman.raven.core.enums.NetworkHandlerConstants.COPRO_CLIENT_SOCKET_TIMEOUT;

/**
 * themisCompliance action — regulatory labelling-compliance evaluation for the Themis label
 * pipeline. Reads the extracted label fields + THEMIS_COMPLIANCE prompt (input query), calls
 * copro (NEON) via {@link CoproProcessor} for the per-rule judgements, and deterministically
 * scores the report (counts + verdict) in {@link ThemisComplianceConsumerProcess}. Modelled on
 * {@link NeonKvpAction}, but writes to the output-table exactly as given (schema included), so
 * the pipeline controls the target schema.
 */
@ActionExecution(
        actionName = "ThemisCompliance"
)
public class ThemisComplianceAction implements IActionExecution {
  private final ActionExecutionAudit action;
  private final Logger log;
  private final ThemisCompliance themisCompliance;
  private final Marker aMarker;

  public static final String DEFAULT_SOCKET_TIME_OUT = "100";
  public static final String COPRO_CLIENT_API_SLEEPTIME = "copro.client.api.sleeptime";
  public static final String CONSUMER_API_COUNT = "llm.kvp.consumer.API.count";
  public static final String THREAD_SLEEP_TIME = "1000";
  public static final String INSERT_INTO = "INSERT INTO";

  public static final String COLUMN_LIST = "created_user_id, created_on, last_updated_user_id, last_updated_on, " +
          "input_file_path, total_response_json, compliance_score, overall_status, pass_count, warn_count, fail_count, " +
          "text_model, paper_type, paper_no, origin_id, process_id, process, group_id, tenant_id, action_id, " +
          "root_pipeline_id, batch_id, submission_id, model_registry, response_format, status, stage, message";
  public static final String VAL_STRING_LIST = "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

  private final int threadSleepTime;
  private final Integer consumerApiCount;
  private final Integer writeBatchSize;
  private final String targetTableName;
  private final String insertQuery;
  private final String themisComplianceUrl;
  private int timeout;
  private final int readBatchSize;

  public ThemisComplianceAction(final ActionExecutionAudit action, final Logger log,
                                final Object themisCompliance) {
    this.themisCompliance = (ThemisCompliance) themisCompliance;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" Themis Compliance Action:" + this.themisCompliance.getName());
    String socketTimeStr = action.getContext().get(COPRO_CLIENT_SOCKET_TIMEOUT);
    socketTimeStr = socketTimeStr != null && socketTimeStr.trim().length() > 0 ? socketTimeStr : DEFAULT_SOCKET_TIME_OUT;
    this.timeout = Integer.parseInt(socketTimeStr);
    String threadSleepTimeStr = action.getContext().get(COPRO_CLIENT_API_SLEEPTIME);
    threadSleepTimeStr = threadSleepTimeStr != null && threadSleepTimeStr.trim().length() > 0 ? threadSleepTimeStr : THREAD_SLEEP_TIME;
    this.threadSleepTime = Integer.parseInt(threadSleepTimeStr);
    this.consumerApiCount = Integer.valueOf(this.action.getContext().get(CONSUMER_API_COUNT));
    this.writeBatchSize = Integer.valueOf(this.action.getContext().get(DB_INSERT_WRITE_BATCH_SIZE));
    this.readBatchSize = Integer.valueOf(action.getContext().get(DB_SELECT_READ_BATCH_SIZE));
    this.targetTableName = this.themisCompliance.getOutputTable();
    this.themisComplianceUrl = this.themisCompliance.getEndpoint();
    this.insertQuery = INSERT_INTO + " " + targetTableName + " (" + COLUMN_LIST + ") " + VAL_STRING_LIST;
  }

  @Override
  public void execute() throws Exception {
    try {
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(themisCompliance.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      log.info(aMarker, "themis compliance action for {} started", themisCompliance.getName());

      final List<URL> urls = Optional.ofNullable(themisComplianceUrl).map(s -> Arrays.stream(s.split(",")).map(urlItem -> {
        try {
          return new URL(urlItem);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL {}", urlItem, e);
          throw new HandymanException("Error in processing the URL", e, action);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());

      final CoproProcessor<ThemisComplianceInputTable, ThemisComplianceOutputTable> coproProcessor = getTableCoproProcessor(jdbi, urls);
      Thread.sleep(threadSleepTime);
      final ThemisComplianceConsumerProcess consumerProcess = new ThemisComplianceConsumerProcess(log, aMarker, action, this);
      coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, consumerProcess);
      log.info(aMarker, "themis compliance action completed {}", themisCompliance.getName());
    } catch (Exception e) {
      action.getContext().put(themisCompliance.getName() + ".isSuccessful", "false");
      HandymanException handymanException = new HandymanException(e);
      HandymanException.insertException("error in execute method for themis compliance action", handymanException, action);
    }
  }

  private @NotNull CoproProcessor<ThemisComplianceInputTable, ThemisComplianceOutputTable> getTableCoproProcessor(Jdbi jdbi, List<URL> urls) {
    ThemisComplianceInputTable inputTable = new ThemisComplianceInputTable();
    final CoproProcessor<ThemisComplianceInputTable, ThemisComplianceOutputTable> coproProcessor =
            new CoproProcessor<>(new LinkedBlockingQueue<>(),
                    ThemisComplianceOutputTable.class,
                    ThemisComplianceInputTable.class,
                    themisCompliance.getResourceConn(), log,
                    inputTable, urls, action);
    coproProcessor.startProducer(themisCompliance.getQuerySet(), readBatchSize);
    return coproProcessor;
  }

  @Override
  public boolean executeIf() throws Exception {
    return themisCompliance.getCondition();
  }

  public int getTimeOut() {
    return this.timeout;
  }
}
