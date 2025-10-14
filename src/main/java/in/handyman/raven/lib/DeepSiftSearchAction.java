package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DeepSiftSearch;
import in.handyman.raven.lib.model.deepSiftSearch.DeepSiftSearchConsumerProcess;
import in.handyman.raven.lib.model.deepSiftSearch.DeepSiftSearchInputTable;
import in.handyman.raven.lib.model.deepSiftSearch.DeepSiftSearchOutputTable;
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

@ActionExecution(actionName = "DeepSiftSearch")
public class DeepSiftSearchAction implements IActionExecution {

  public static final String INSERT_COLUMNS =
          "sor_item_id, sor_item_name, sor_container_id, sor_container_name, source_document_type, "
                  + "origin_id, root_pipeline_id, search_id, search_name, batch_id, tenant_id, "
                  + "created_on, created_by, search_output, matched_terms, paper_no, group_id, timetaken_ms, status";
  public static final String INSERT_INTO = "INSERT INTO ";
  public static final String INSERT_INTO_VALUES =
          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::timestamp, ?, ?, ?, ?, ?, ?, ?)";

  public static final String READ_BATCH_SIZE = "read.batch.size";
  public static final String DEEP_SIFT_SEARCH_CONSUMER_API_COUNT = "deep.sift.search.consumer.API.count";
  public static final String WRITE_BATCH_SIZE = "write.batch.size";
  public static final String COPRO_FILE_PROCESS_FORMAT = "pipeline.copro.api.process.file.format";

  private final ActionExecutionAudit action;
  private final Logger log;
  private final DeepSiftSearch deepSiftSearch;
  private final Marker aMarker;
  private final String processBase64;

  public DeepSiftSearchAction(final ActionExecutionAudit action,
                              final Logger log,
                              final Object deepSiftSearch) {
    this.deepSiftSearch = (DeepSiftSearch) deepSiftSearch;
    this.action = action;
    this.log = log;
    this.processBase64 = action.getContext().get(COPRO_FILE_PROCESS_FORMAT);
    this.aMarker = MarkerFactory.getMarker("DeepSiftSearch:" + this.deepSiftSearch.getName());
  }

  @Override
  public void execute() throws Exception {
    try {
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(deepSiftSearch.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));

      log.info(aMarker, "DeepSift Search Action for {} has been started", deepSiftSearch.getName());

      String outputTable = deepSiftSearch.getResultTable();
      String insertQuery = INSERT_INTO + outputTable + " (" + INSERT_COLUMNS + ") " + INSERT_INTO_VALUES;

      final List<URL> urls = Optional.ofNullable(deepSiftSearch.getEndPoint())
              .map(s -> Arrays.stream(s.split(","))
                      .map(s1 -> {
                        try {
                          return new URL(s1);
                        } catch (MalformedURLException e) {
                          log.error(aMarker, "Error in processing URL", e);
                          throw new HandymanException("Error processing URL", e, action);
                        }
                      })
                      .collect(Collectors.toList()))
              .orElse(Collections.emptyList());

      final CoproProcessor<DeepSiftSearchInputTable, DeepSiftSearchOutputTable> coproProcessor =
              new CoproProcessor<>(
                      new LinkedBlockingQueue<>(),
                      DeepSiftSearchOutputTable.class,
                      DeepSiftSearchInputTable.class,
                      deepSiftSearch.getResourceConn(),
                      log,
                      new DeepSiftSearchInputTable(),
                      urls,
                      action
              );

      Integer readBatchSize = Integer.parseInt(action.getContext().getOrDefault(READ_BATCH_SIZE, "10"));
      Integer consumerCount = Math.max(1, Integer.parseInt(action.getContext().getOrDefault(DEEP_SIFT_SEARCH_CONSUMER_API_COUNT, "1")));
      Integer writeBatchSize = Math.max(1, Integer.parseInt(action.getContext().getOrDefault(WRITE_BATCH_SIZE, "1")));

      DeepSiftSearchConsumerProcess searchConsumerProcess =
              new DeepSiftSearchConsumerProcess(log, aMarker, action);

      coproProcessor.startProducer(deepSiftSearch.getQuerySet(), readBatchSize);
      Thread.sleep(1000);
      coproProcessor.startConsumer(insertQuery, consumerCount, writeBatchSize, searchConsumerProcess);

      log.info(aMarker, "DeepSift Search Action has been completed for {}", deepSiftSearch.getName());
      action.getContext().put(deepSiftSearch.getName() + ".isSuccessful", "true");

    } catch (Exception e) {
      action.getContext().put(deepSiftSearch.getName() + ".isSuccessful", "false");
      log.error(aMarker, "Error in execute method in DeepSift Search", e);
      throw new HandymanException("Error in execute method in DeepSift Search", e, action);
    }
  }

  @Override
  public boolean executeIf() throws Exception {
    return deepSiftSearch.getCondition();
  }

}
