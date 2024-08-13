package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.NeonKvp;
import in.handyman.raven.lib.model.kvp.llm.neon.processor.NeonKvpConsumerProcess;
import in.handyman.raven.lib.model.kvp.llm.neon.processor.NeonQueryInputTable;
import in.handyman.raven.lib.model.kvp.llm.neon.processor.NeonQueryOutputTable;
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
        actionName = "NeonKvp"
)
public class NeonKvpAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final NeonKvp neonKvp;

  private final Marker aMarker;

  public static final String DEFAULT_SOCKET_TIME_OUT = "100";
  public static final String COPRO_CLIENT_SOCKET_TIMEOUT = "copro.client.socket.timeout";
  public static final String COPRO_CLIENT_API_SLEEPTIME = "copro.client.api.sleeptime";
  public static final String CONSUMER_API_COUNT = "llm.kvp.consumer.API.count";
  public static final String WRITE_BATCH_SIZE = "write.batch.size";
  public static final String THREAD_SLEEP_TIME = "1000";
  public static final String INSERT_INTO = "INSERT INTO";
  public static final String DEFAULT_INFO_SCHEMA_NAME = "sor_transaction";

  public static final String COLUMN_LIST = "created_user_id,created_on,last_updated_user_id,  last_updated_on, " +
          "input_file_path, total_response_json,text_model,paper_type, paper_no,origin_id ,process_id,process," +
          "group_id, tenant_id, action_id, root_pipeline_id, batch_id , model_registry, response_format, image_dpi," +
          " image_width, image_height, extracted_image_unit , status, stage, message";
  public static final String VAL_STRING_LIST = "VALUES( ?,?,?,?,?," +
          "?,?,?,?,?" +
          ",?,?,?,?,?," +
          "?,?, ?, ?, ?,? ,?,?, ?, ?, ?)";

  public static final String READ_BATCH_SIZE = "read.batch.size";
  private final int threadSleepTime;
  private final Integer consumerApiCount;
  private final Integer writeBatchSize;
  private final String schemaName;
  private final String targetTableName;
  private final String columnList;
  private final String insertQuery;
  private final String neonKvpUrl;

  private int timeout;


  private final int readBatchSize;


  public NeonKvpAction(final ActionExecutionAudit action, final Logger log,
                          final Object neonKvp) {
    this.neonKvp = (NeonKvp) neonKvp;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" Neon kvp Action:" + this.neonKvp.getName());
    String socketTimeStr = action.getContext().get(COPRO_CLIENT_SOCKET_TIMEOUT);
    socketTimeStr = socketTimeStr != null && socketTimeStr.trim().length() > 0 ? socketTimeStr : DEFAULT_SOCKET_TIME_OUT;
    this.timeout = Integer.parseInt(socketTimeStr);
    String threadSleepTimeStr = action.getContext().get(COPRO_CLIENT_API_SLEEPTIME);
    threadSleepTimeStr = threadSleepTimeStr != null && threadSleepTimeStr.trim().length() > 0 ? threadSleepTimeStr : THREAD_SLEEP_TIME;
    this.threadSleepTime = Integer.parseInt(threadSleepTimeStr);
    String consumerApiCountStr = this.action.getContext().get(CONSUMER_API_COUNT);
    consumerApiCount = Integer.valueOf(consumerApiCountStr);
    String writeBatchSizeStr = this.action.getContext().get(WRITE_BATCH_SIZE);
    this.writeBatchSize = Integer.valueOf(writeBatchSizeStr);
    this.readBatchSize = Integer.valueOf(action.getContext().get(READ_BATCH_SIZE));
    this.schemaName = DEFAULT_INFO_SCHEMA_NAME;
    this.targetTableName = this.neonKvp.getOutputTable();
    this.columnList = COLUMN_LIST;
    this.neonKvpUrl = this.neonKvp.getEndpoint();
    insertQuery = INSERT_INTO + " " + schemaName + "." + targetTableName + "(" + columnList + ") " + " " + VAL_STRING_LIST;
  }

  @Override
  public void execute() throws Exception {

    try {
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(neonKvp.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      log.info(aMarker, "kvp extraction with llm Action for {} has been started", neonKvp.getName());

      final List<URL> urls = Optional.ofNullable(neonKvpUrl).map(s -> Arrays.stream(s.split(",")).map(urlItem -> {
        try {
          return new URL(urlItem);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL {}", urlItem, e);
          throw new HandymanException("Error in processing the URL", e, action);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());

      final CoproProcessor<NeonQueryInputTable, NeonQueryOutputTable> coproProcessor = getTableCoproProcessor(jdbi, urls);
      Thread.sleep(threadSleepTime);
      final NeonKvpConsumerProcess neonKvpConsumerProcess = new NeonKvpConsumerProcess(log, aMarker, action, this);
      coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, neonKvpConsumerProcess);
      log.info(aMarker, " llm kvp Action has been completed {}  ", neonKvp.getName());
    } catch (Exception e) {
      action.getContext().put(neonKvp.getName() + ".isSuccessful", "false");
      HandymanException handymanException = new HandymanException(e);
      HandymanException.insertException("error in execute method for llm kvp action", handymanException, action);

    }


  }

  private @NotNull CoproProcessor<NeonQueryInputTable, NeonQueryOutputTable> getTableCoproProcessor(Jdbi jdbi, List<URL> urls) {
    NeonQueryInputTable neonQueryInputTable = new NeonQueryInputTable();
    final CoproProcessor<NeonQueryInputTable, NeonQueryOutputTable> coproProcessor =
            new CoproProcessor<>(new LinkedBlockingQueue<>(),
                    NeonQueryOutputTable.class,
                    NeonQueryInputTable.class,
                    jdbi, log,
                    neonQueryInputTable, urls, action);

    coproProcessor.startProducer(neonKvp.getQuerySet(), readBatchSize);
    return coproProcessor;
  }

  @Override
  public boolean executeIf() throws Exception {
    return neonKvp.getCondition();
  }

  public int getTimeOut() {
    return this.timeout;
  }
}
