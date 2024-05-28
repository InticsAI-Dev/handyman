package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.KryptonKvp;
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

import in.handyman.raven.lib.model.krypton.kvp.KryptonKvpConsumerProcess;
import in.handyman.raven.lib.model.krypton.kvp.KryptonQueryInputTable;
import in.handyman.raven.lib.model.krypton.kvp.KryptonQueryOutputTable;
import okhttp3.MediaType;
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
    actionName = "KryptonKvp"
)
public class KryptonKvpAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final KryptonKvp kryptonKvp;

  private final Marker aMarker;

  private static final MediaType MediaTypeJSON = MediaType
          .parse("application/json; charset=utf-8");
  public static final String DEFAULT_SOCKET_TIME_OUT = "100";
  public static final String COPRO_CLIENT_SOCKET_TIMEOUT = "copro.client.socket.timeout";
  public static final String COPRO_CLIENT_API_SLEEPTIME = "copro.client.api.sleeptime";
  public static final String CONSUMER_API_COUNT = "krypton.kvp.consumer.API.count";
  public static final String WRITE_BATCH_SIZE = "write.batch.size";
  public static final String THREAD_SLEEP_TIME = "1000";
  public static final String INSERT_INTO = "INSERT INTO";
  public static final String DEFAULT_INFO_SCHEMA_NAME = "krypton_kvp";
  public static final String KRYPTON_KVP_OUTPUT = "krypton_kvp_output";
  public static final String COLUMN_LIST = "created_user_id,created_on,last_updated_user_id,  last_updated_on, " +
          "input_file_path, total_response_json,text_model,paper_type, paper_no,origin_id ,process_id,process," +
          "group_id, tenant_id, action_id, root_pipeline_id, batch_id , model_registry, response_format, imageDPI," +
          " imageWidth, imageHeight, extractedImageUnit ";
  public static final String VAL_STRING_LIST = "VALUES( ?,?,?,?,?," +
          "?,?,?,?,?" +
          ",?,?,?,?,?," +
          "?,?, ?, ?, ?,? ,?,?)";

  public static final String READ_BATCH_SIZE = "read.batch.size";
  private final int threadSleepTime;
  private final Integer consumerApiCount;
  private final Integer writeBatchSize;
  private final String schemaName;
  private final String targetTableName;
  private final String columnList;
  private final String insertQuery;
  private final String kryptonKvpUrl;

  private int timeout;

  private final ObjectMapper mapper = new ObjectMapper();
  private final String URI;
  private final int readBatchSize;



  public KryptonKvpAction(final ActionExecutionAudit action, final Logger log,
                          final Object kryptonKvp) {
    this.kryptonKvp = (KryptonKvp) kryptonKvp;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" Krypton kvp Action:"+this.kryptonKvp.getName());
    this.URI = ((KryptonKvp) kryptonKvp).getEndpoint();
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
    this.targetTableName = KRYPTON_KVP_OUTPUT;
    this.columnList = COLUMN_LIST;
    this.kryptonKvpUrl = this.kryptonKvp.getEndpoint();
    insertQuery = INSERT_INTO + " " + schemaName + "." + targetTableName + "(" + columnList + ") " + " " + VAL_STRING_LIST;
  }

  @Override
  public void execute() throws Exception {

    try {
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(kryptonKvp.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      log.info(aMarker, "paragraph Extraction Action for {} has been started", kryptonKvp.getName());

      final List<URL> urls = Optional.ofNullable(kryptonKvpUrl).map(s -> Arrays.stream(s.split(",")).map(urlItem -> {
        try {
          return new URL(urlItem);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL " + urlItem, e);
          throw new HandymanException("Error in processing the URL", e, action);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());

      KryptonQueryInputTable kryptonQueryInputTable = new KryptonQueryInputTable();
      final CoproProcessor<KryptonQueryInputTable, KryptonQueryOutputTable> coproProcessor =
              new CoproProcessor<>(new LinkedBlockingQueue<>(),
                      KryptonQueryOutputTable.class,
                      KryptonQueryInputTable.class,
                      jdbi, log,
                      kryptonQueryInputTable, urls, action);

      coproProcessor.startProducer(kryptonKvp.getQuerySet(), readBatchSize);
      Thread.sleep(threadSleepTime);
      final KryptonKvpConsumerProcess kryptonKvpConsumerProcess = new KryptonKvpConsumerProcess(log, aMarker, action, this);
      coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, kryptonKvpConsumerProcess);
      log.info(aMarker, " krypton kvp Action has been completed {}  ", kryptonKvp.getName());
    } catch (Exception e) {
      action.getContext().put(kryptonKvp.getName() + ".isSuccessful", "false");
      log.error(aMarker, "error in execute method for krypton kvp action ", e);
      throw new HandymanException("error in execute method for krypton kvp action", e, action);
    }


  }

  @Override
  public boolean executeIf() throws Exception {
    return kryptonKvp.getCondition();
  }

  public int getTimeOut() {
    return this.timeout;
  }
}
