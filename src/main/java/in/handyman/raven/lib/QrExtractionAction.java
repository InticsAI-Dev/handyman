package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.QrExtraction;
import in.handyman.raven.lib.model.qrextraction.*;
import in.handyman.raven.util.ExceptionUtil;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "QrExtraction"
)
public class QrExtractionAction implements IActionExecution {
  public static final String READ_BATCH_SIZE = "read.batch.size";
  public static final String QR_CONSUMER_API_COUNT = "qr.consumer.API.count";
  public static final String WRITE_BATCH_SIZE = "write.batch.size";
  public static final String INSERT_INTO = "INSERT INTO ";
  public static final String INSERT_INTO_COLUMNS = "origin_id, group_id, paper_no, created_on,   qr_format, qr_format_id, extracted_value,   file_id, b_box, angle, confidence_score, status, stage, message,decode_type, model_name, model_version,root_pipeline_id,tenant_id,request,response,endpoint";
  public static final String INSERT_INTO_VALUES = "?,?,?,?,  ?,?,? ,?,?,  ?,?,?, ?,?  ,?, ?,? , ?,?,?,?,?";
  public static final String OKHTTP_CLIENT_TIMEOUT = "okhttp.client.timeout";
  private final ActionExecutionAudit action;

  private final Logger log;

  private final QrExtraction qrExtraction;
  private static String httpClientTimeout = new String();
  private final Marker aMarker;

  public QrExtractionAction(final ActionExecutionAudit action, final Logger log,
                            final Object qrExtraction) {
    this.qrExtraction = (QrExtraction) qrExtraction;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" QrExtraction:" + this.qrExtraction.getName());
    this.httpClientTimeout = action.getContext().get(OKHTTP_CLIENT_TIMEOUT);
  }

  @Override
  public void execute() throws Exception {


    Integer consumerCount = Integer.valueOf(action.getContext().get(QR_CONSUMER_API_COUNT));
    Integer writeBatchSize = Integer.valueOf(action.getContext().get(WRITE_BATCH_SIZE));

    String outputDir=action.getContext().get("target_directory_path") + "/" + action.getContext().get("process-id") + "/qr-extraction/";
    QrConsumerProcess qrConsumerProcess = new QrConsumerProcess(log, aMarker, action, outputDir);
    Integer readBatchSize = Integer.valueOf(action.getContext().get(READ_BATCH_SIZE));

    try {
      log.info(aMarker, "Qr extraction Action for {} with group by eoc-id has started", qrExtraction.getName());
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(qrExtraction.getResourceConn());

      //3. initiate copro processor and copro urls
      final List<URL> urls = Optional.ofNullable(qrExtraction.getEndPoint()).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
        try {
          return new URL(s1);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL {}", s1, e);
          throw new HandymanException("Error in processing the URL", e, action);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());

      log.info("Urls for the qr attribution for sor grouping {}", urls);

      //5. build insert prepare statement with output table columns
      final String insertQuery = INSERT_INTO + qrExtraction.getOutputTable() +
              "            (" + INSERT_INTO_COLUMNS + ")" +
              "VALUES(" + INSERT_INTO_VALUES + ")";
      final CoproProcessor<QrInputEntity, QrOutputEntity> coproProcessor =
              new CoproProcessor<>(new LinkedBlockingQueue<>(),
                      QrOutputEntity.class,
                      QrInputEntity.class,
                      jdbi, log,
                      new QrInputEntity(), urls, action);

      //4. call the method start producer from coproprocessor
      coproProcessor.startProducer(qrExtraction.getQuerySet(), readBatchSize);
      log.info("start producer method from copro processor ");
      Thread.sleep(1000);
      //8. call the method start consumer from coproprocessor
      coproProcessor.startConsumer(insertQuery, consumerCount, writeBatchSize, qrConsumerProcess);
      log.info("start consumer method from copro processor ");


    } catch (Exception e) {
      log.error("Error in the Qr extraction action {}", ExceptionUtil.toString(e));
      throw new HandymanException("QR extraction action failed ", e, action);

    }

  }


  //6. write consumer process class which implements CoproProcessor.ConsumerProcess

  @Override
  public boolean executeIf() throws Exception {
    return qrExtraction.getCondition();
  }
}