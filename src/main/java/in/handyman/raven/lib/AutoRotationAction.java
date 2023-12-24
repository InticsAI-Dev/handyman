package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AutoRotation;
import in.handyman.raven.lib.model.autorotation.AutoRotationConsumerProcess;
import in.handyman.raven.lib.model.autorotation.AutoRotationInputTable;
import in.handyman.raven.lib.model.autorotation.AutoRotationOutputTable;
import okhttp3.MediaType;
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
        actionName = "AutoRotation"
)
public class AutoRotationAction implements IActionExecution {
  private static final MediaType MediaTypeJSON = MediaType
          .parse("application/json; charset=utf-8");
  public static final String DEFAULT_SOCKET_TIME_OUT = "100";
  public static final String COPRO_CLIENT_SOCKET_TIMEOUT = "copro.client.socket.timeout";
  public static final String COPRO_CLIENT_API_SLEEPTIME = "copro.client.api.sleeptime";
  public static final String CONSUMER_API_COUNT = "auto.rotation.consumer.API.count";
  public static final String WRITE_BATCH_SIZE = "write.batch.size";
  public static final String THREAD_SLEEP_TIME = "1000";
  public static final String INSERT_INTO = "INSERT INTO";
  public static final String DEFAULT_INFO_SCHEMA_NAME = "info";
  public static final String AUTO_ROTATION = "auto_rotation";
  public static final String COLUMN_LIST = "origin_id,group_id,tenant_id,template_id,process_id, processed_file_path,paper_no, status,stage,message,created_on,root_pipeline_id,model_name,model_version";
  public static final String VAL_STRING_LIST = "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  public static final String READ_BATCH_SIZE = "read.batch.size";
  public static final String COPRO_AUTOROTATION_URL1 = "copro.autorotation.url";
  private final int threadSleepTime;
  private final Integer consumerApiCount;
  private final Integer writeBatchSize;
  private final String schemaName;
  private final String targetTableName;
  private final String columnList;
  private final String autoRotateUrl;
  private final String outputDir;
  private final String insertQuery;

  private int timeout;

  private final ActionExecutionAudit action;
  private final Logger log;
  private final AutoRotation autoRotation;
  private final Marker aMarker;
  private final ObjectMapper mapper = new ObjectMapper();
  private final String URI;
  private final int readBatchSize;


  public AutoRotationAction(final ActionExecutionAudit action, final Logger log,
                            final Object autoRotation) {
    this.autoRotation = (AutoRotation) autoRotation;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" AutoRotation:" + this.autoRotation.getName());
    this.URI = ((AutoRotation) autoRotation).getEndPoint();
    String socketTimeStr = action.getContext().get(COPRO_CLIENT_SOCKET_TIMEOUT);
    socketTimeStr = socketTimeStr != null && socketTimeStr.trim().length() > 0 ? socketTimeStr : DEFAULT_SOCKET_TIME_OUT;
    this.timeout = Integer.parseInt(socketTimeStr);
    String threadSleepTimeStr = action.getContext().get(COPRO_CLIENT_API_SLEEPTIME);
    threadSleepTimeStr = threadSleepTimeStr != null && threadSleepTimeStr.trim().length() > 0 ? threadSleepTimeStr : THREAD_SLEEP_TIME;
    this.threadSleepTime = Integer.parseInt(threadSleepTimeStr);
    //TODO - Please do same as above to ensure that you don't assume default values
    String consumerApiCountStr = this.action.getContext().get(CONSUMER_API_COUNT);
    consumerApiCount = Integer.valueOf(consumerApiCountStr);
    String writeBatchSizeStr = this.action.getContext().get(WRITE_BATCH_SIZE);
    this.writeBatchSize = Integer.valueOf(writeBatchSizeStr);
    this.readBatchSize = Integer.valueOf(action.getContext().get(READ_BATCH_SIZE));
    this.schemaName = DEFAULT_INFO_SCHEMA_NAME;
    this.targetTableName = AUTO_ROTATION;
    this.columnList = COLUMN_LIST;
    this.autoRotateUrl = this.autoRotation.getEndPoint();
    this.outputDir = Optional.ofNullable(this.autoRotation.getOutputDir()).map(String::valueOf).orElse(null);
    insertQuery = INSERT_INTO + " " + schemaName + "." + targetTableName + "(" + columnList + ") " + " " + VAL_STRING_LIST;
  }

  @Override
  public void execute() throws Exception {
    try {
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(autoRotation.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      log.info(aMarker, "Auto Rotation Action for {} has been started", autoRotation.getName());

      final List<URL> urls = Optional.ofNullable(action.getContext().get("copro.autorotation.url")).map(s -> Arrays.stream(s.split(",")).map(urlItem -> {
        try {
          return new URL(urlItem);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL " + urlItem, e);
          throw new HandymanException("Error in processing the URL", e, action);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());

      AutoRotationInputTable autoRotationInputTable = new AutoRotationInputTable();
      final CoproProcessor<AutoRotationInputTable, AutoRotationOutputTable> coproProcessor =
              new CoproProcessor<>(new LinkedBlockingQueue<>(),
                      AutoRotationOutputTable.class,
                      AutoRotationInputTable.class,
                      jdbi, log,
                      autoRotationInputTable, urls, action);

      coproProcessor.startProducer(autoRotation.getQuerySet(), readBatchSize);
      Thread.sleep(threadSleepTime);
      final AutoRotationConsumerProcess autoRotationConsumerProcess = new AutoRotationConsumerProcess(log, aMarker, action, outputDir, this);
      coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, autoRotationConsumerProcess);
      log.info(aMarker, " Auto Rotation Action has been completed {}  ", autoRotation.getName());
    } catch (Exception e) {
      action.getContext().put(autoRotation.getName() + ".isSuccessful", "false");
      log.error(aMarker, "error in execute method for auto rotation ", e);
      throw new HandymanException("error in execute method for auto rotation", e, action);
    }
  }

  @Override
  public boolean executeIf() throws Exception {
    return autoRotation.getCondition();
  }

  public Integer getTimeOut() {
    return this.timeout;
  }

}