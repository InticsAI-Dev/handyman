package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.BulletInExtraction;
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

import in.handyman.raven.lib.model.bulletin.detection.BulletinExtractionConsumerProcess;
import in.handyman.raven.lib.model.bulletin.detection.BulletinQueryInputTable;
import in.handyman.raven.lib.model.bulletin.detection.BulletinQueryOutputTable;
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
    actionName = "BulletInExtraction"
)
public class BulletInExtractionAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final BulletInExtraction bulletInExtraction;

  private final Marker aMarker;


  private static final MediaType MediaTypeJSON = MediaType
          .parse("application/json; charset=utf-8");
  public static final String DEFAULT_SOCKET_TIME_OUT = "100";
  public static final String COPRO_CLIENT_SOCKET_TIMEOUT = "copro.client.socket.timeout";
  public static final String COPRO_CLIENT_API_SLEEPTIME = "copro.client.api.sleeptime";
  public static final String CONSUMER_API_COUNT = "bulletin.extraction.consumer.API.count";
  public static final String WRITE_BATCH_SIZE = "write.batch.size";
  public static final String THREAD_SLEEP_TIME = "1000";
  public static final String INSERT_INTO = "INSERT INTO";
  public static final String DEFAULT_INFO_SCHEMA_NAME = "bulletin_extraction";
  public static final String BULLETIN_RESULT_TABLR = "bulletin_extraction_result";
  public static final String COLUMN_LIST = "origin_id, paper_no, group_id, file_path, tenant_id, " +
                                            "process_id, output_dir, root_pipeline_id, process, status, " +
                                            "stage, message, model_name, model_version, synonym_id," +
                                            "bulletin_section,bulletin_points";
  public static final String VAL_STRING_LIST = "VALUES(?,?,?,?,?" +
                                                      ",?,?,?,?,?," +
                                                      "?,?,?,?,?," +
                                                      "?,?)";
  public static final String READ_BATCH_SIZE = "read.batch.size";
  private final int threadSleepTime;
  private final Integer consumerApiCount;
  private final Integer writeBatchSize;
  private final String schemaName;
  private final String targetTableName;
  private final String columnList;
  private final String BulletinExtractionUrl;
  private final String outputDir;
  private final String insertQuery;

  private int timeout;

  private final ObjectMapper mapper = new ObjectMapper();
  private final String URI;
  private final int readBatchSize;
  public BulletInExtractionAction(final ActionExecutionAudit action, final Logger log,
      final Object bulletInExtraction) {
    this.bulletInExtraction = (BulletInExtraction) bulletInExtraction;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" BulletInExtraction:"+this.bulletInExtraction.getName());
    this.URI = ((BulletInExtraction) bulletInExtraction).getEndpoint();
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
    this.targetTableName = BULLETIN_RESULT_TABLR;
    this.columnList = COLUMN_LIST;
    this.BulletinExtractionUrl = this.bulletInExtraction.getEndpoint();
    this.outputDir = Optional.ofNullable(this.bulletInExtraction.getOutputDir()).map(String::valueOf).orElse(null);
    insertQuery = INSERT_INTO + " " + schemaName + "." + targetTableName + "(" + columnList + ") " + " " + VAL_STRING_LIST;
  }

  @Override
  public void execute() throws Exception {


    try {
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(bulletInExtraction.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      log.info(aMarker, "Bulletin extraction Action for {} has been started", bulletInExtraction.getName());
      final BulletinExtractionConsumerProcess bulletinExtractionConsumerProcess = new BulletinExtractionConsumerProcess(log, aMarker, action, outputDir, this);

      final List<URL> urls = Optional.ofNullable(BulletinExtractionUrl).map(s -> Arrays.stream(s.split(",")).map(urlItem -> {
        try {
          return new URL(urlItem);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL " + urlItem, e);
          throw new HandymanException("Error in processing the URL", e, action);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());

      BulletinQueryInputTable bulletinQueryInputTable = new BulletinQueryInputTable();
      final CoproProcessor<BulletinQueryInputTable, BulletinQueryOutputTable> coproProcessor =
              new CoproProcessor<>(new LinkedBlockingQueue<>(),
                      BulletinQueryOutputTable.class,
                      BulletinQueryInputTable.class,
                      jdbi, log,
                      bulletinQueryInputTable, urls, action);

      coproProcessor.startProducer(bulletInExtraction.getQuerySet(), readBatchSize);
      Thread.sleep(threadSleepTime);
      coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, bulletinExtractionConsumerProcess);
      log.info(aMarker, " Bulletin extraction Action has been completed {}  ", bulletInExtraction.getName());
    } catch (Exception e) {
      action.getContext().put(bulletInExtraction.getName() + ".isSuccessful", "false");
      log.error(aMarker, "error in execute method for Bulletin extraction ", e);
      throw new HandymanException("error in execute method for Bulletin extraction", e, action);
    }

  }

  @Override
  public boolean executeIf() throws Exception {
    return bulletInExtraction.getCondition();
  }

  public Integer getTimeOut() {
    return this.timeout;
  }
}