package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.HwDetection;
import in.handyman.raven.lib.model.hwdectection.*;
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
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "HwDetection"
)
public class HwDetectionAction implements IActionExecution {
  public static final String READ_BATCH_SIZE = "read.batch.size";
  public static final String PAPER_CLASSIFICATION_CONSUMER_API_COUNT = "paper.classification.consumer.API.count";
  public static final String WRITE_BATCH_SIZE = "write.batch.size";
  public static final String PAPER_CLASSIFICATION = "paper_classification";
  public static final String INSERT_INTO = "INSERT INTO ";
  public static final String PAPER_CLASSIFICATION_RESULT = "paper_classification_result";
  public static final String INSERT_INTO_COLUMNS = "created_on, created_user_id, last_updated_on, last_updated_user_id, tenant_id, origin_id, paper_no, template_id, model_id, document_type, status, stage, message, group_id, root_pipeline_id, confidence_score,model_name,model_version";
  public static final String INSERT_INTO_VALUES = "now(),?,now(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
  public static final String OKHTTP_CLIENT_TIMEOUT = "okhttp.client.timeout";
  private final ActionExecutionAudit action;

  private final Logger log;

  private static HwDetection hwDetection = new HwDetection();

  private final Marker aMarker;
  public static String httpClientTimeout = new String();

  public HwDetectionAction(final ActionExecutionAudit action, final Logger log,
                           final Object hwDetection) {
    this.hwDetection = (HwDetection) hwDetection;
    this.action = action;
    this.log = log;
    this.httpClientTimeout = action.getContext().get(OKHTTP_CLIENT_TIMEOUT);
    this.aMarker = MarkerFactory.getMarker(" HwDetection:" + this.hwDetection.getName());
  }

  @Override
  public void execute() throws Exception {
    Integer readBatchSize = Integer.valueOf(action.getContext().get(READ_BATCH_SIZE));
    Integer consumerCount = Integer.valueOf(action.getContext().get(PAPER_CLASSIFICATION_CONSUMER_API_COUNT));
    Integer writeBatchSize = Integer.valueOf(action.getContext().get(WRITE_BATCH_SIZE));
    String outputDir = hwDetection.getDirectoryPath();
    HwClassificationConsumerProcess hwClassificationConsumerProcess = new HwClassificationConsumerProcess(log, aMarker, action, outputDir);
    try {
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(hwDetection.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));

      log.info(aMarker, "Handwritten Classification Action for {} has been started", hwDetection.getName());
      final String insertQuery = INSERT_INTO +  hwDetection.getOutputTable()+ "(" + INSERT_INTO_COLUMNS + ")" +
              "values(" + INSERT_INTO_VALUES + ")";
      final List<URL> urls = Optional.ofNullable(hwDetection.getEndPoint()).map(s -> Arrays.stream(s.split(",")).map(url -> {
        try {
          return new URL(url);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL {}", url, e);
          throw new HandymanException("Error in processing the URL", e, action);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());
      log.info("urls used in hw detection macro {}", urls);
      final CoproProcessor<HwClassificationInputTable, HwClassificationOutputTable> coproProcessor =
              new CoproProcessor<>(new LinkedBlockingQueue<>(),
                      HwClassificationOutputTable.class,
                      HwClassificationInputTable.class,
                      jdbi, log,
                      new HwClassificationInputTable(), urls, action);

      coproProcessor.startProducer(hwDetection.getQuerySet(), readBatchSize);
      log.info("hwdetection read batch size {} and queryset from macro {} ", readBatchSize, hwDetection.getQuerySet());
      Thread.sleep(1000);

      coproProcessor.startConsumer(insertQuery, consumerCount, writeBatchSize, hwClassificationConsumerProcess);
      log.info(aMarker, " Handwritten Classification has been completed {}  ", hwDetection.getName());
    } catch (Exception e) {
      action.getContext().put(hwDetection.getName() + ".isSuccessful", "false");
      log.error(aMarker, "Error at handwritten classification execute method {}", ExceptionUtil.toString(e));
      throw new HandymanException("Error at handwritten classification execute method ", e, action);
    }
  }

  @Override
  public boolean executeIf() throws Exception {
    return hwDetection.getCondition();
  }

}