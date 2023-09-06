package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TableExtraction;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import in.handyman.raven.util.ExceptionUtil;
import in.handyman.raven.util.UniqueID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
/**
 * Auto Generated By Raven
 */
@ActionExecution(
    actionName = "TableExtraction"
)
public class TableExtractionAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final TableExtraction tableExtraction;

  private final Marker aMarker;

  public TableExtractionAction(final ActionExecutionAudit action, final Logger log,
                               final Object tableExtraction) {
    this.tableExtraction = (TableExtraction) tableExtraction;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" TableExtraction:"+this.tableExtraction.getName());
  }

  @Override
  public void execute(){
    try {
      log.info(aMarker, "Table Extraction Action has been started {}",tableExtraction);

      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(tableExtraction.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      final String outputDir = Optional.ofNullable(tableExtraction.getOutputDir()).map(String::valueOf).orElse(null);
      log.info(aMarker, "Table Extraction Action output directory {}",outputDir);
      //5. build insert prepare statement with output table columns
      final String insertQuery = "INSERT INTO " +tableExtraction.getResultTable()+
              "(origin_id,group_id,tenant_id,template_id,processed_file_path,paper_no, status,stage,message,created_on,process_id,root_pipeline_id) " +
              " VALUES(?,?, ?,?, ?,?, ?,?,?,? ,?,  ?)";
      log.info(aMarker, "table extraction Insert query {}", insertQuery);

      //3. initiate copro processor and copro urls
      final List<URL> urls = Optional.ofNullable(action.getContext().get("copro.table-extraction.url")).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
        try {
          return new URL(s1);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL ", e);
          throw new HandymanException("Error in processing the URL", e, action);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());
      log.info(aMarker, "table extraction copro urls {}", urls);

      final CoproProcessor<TableExtractionInputTable, TableExtractionOutputTable> coproProcessor =
              new CoproProcessor<>(new LinkedBlockingQueue<>(),
                      TableExtractionOutputTable.class,
                      TableExtractionInputTable.class,
                      jdbi, log,
                      new TableExtractionInputTable(), urls, action);

      log.info(aMarker, "table extraction copro coproProcessor initialization  {}", coproProcessor);

      //4. call the method start producer from coproprocessor
      coproProcessor.startProducer(tableExtraction.getQuerySet(), Integer.valueOf(action.getContext().get("read.batch.size")));
      log.info(aMarker, "table extraction copro coproProcessor startProducer called read batch size {}",action.getContext().get("read.batch.size"));
      Thread.sleep(1000);
      coproProcessor.startConsumer(insertQuery, Integer.valueOf(action.getContext().get("table.extraction.consumer.API.count")), Integer.valueOf(action.getContext().get("write.batch.size")), new TableExtractionConsumerProcess(log, aMarker,outputDir, action));
      log.info(aMarker, "table extraction copro coproProcessor startConsumer called consumer count {} write batch count {} ",Integer.valueOf(action.getContext().get("table.extraction.consumer.API.count")),Integer.valueOf(action.getContext().get("write.batch.size")));


    }catch(Exception ex){
      log.error(aMarker,"error in execute method for table extraction  ",ex);
      throw new HandymanException("error in execute method for table extraction", ex, action);
    }
  }

  public static class TableExtractionConsumerProcess implements CoproProcessor.ConsumerProcess<TableExtractionInputTable,TableExtractionOutputTable>{

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    private final String outputDir;

    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public TableExtractionConsumerProcess(Logger log, Marker aMarker, String outputDir, ActionExecutionAudit action) {
      this.log = log;
      this.aMarker = aMarker;
      this.outputDir = outputDir;
      this.action = action;
    }

    @Override
    public List<TableExtractionOutputTable> process(URL endpoint, TableExtractionInputTable entity) throws Exception {
      log.info(aMarker,"coproProcessor consumer process started with endpoint {} and entity {}",endpoint,entity);
      List<TableExtractionOutputTable> parentObj = new ArrayList<>();
      final ObjectNode objectNode = mapper.createObjectNode();
      String inputFilePath = entity.getFilePath();
      Long uniqueId= UniqueID.getId();
      String uniqueIdStr=String.valueOf(uniqueId);
      String outputDirectory=outputDir.concat("/").concat(String.valueOf(entity.getRootPipelineId())).concat("/").concat(entity.getOriginId()).concat("/").concat(uniqueIdStr);
      Long rootPipelineId=entity.getRootPipelineId();
      final String tableExtractionProcessName = "TABLE_EXTRACTION";
      Long actionId=action.getActionId();
      objectNode.put("rootPipelineId",rootPipelineId);
      objectNode.put("process",tableExtractionProcessName);
      objectNode.put("inputFilePath", inputFilePath);
      objectNode.put("outputDir", outputDirectory);
      objectNode.put("actionId", actionId);

      log.info(aMarker,"coproProcessor mapper object node {}",objectNode);
      Request request = new Request.Builder().url(endpoint)
              .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();

      if(log.isInfoEnabled()) {
        log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} and outputDir {}", endpoint, inputFilePath, outputDirectory);
      }
      AtomicInteger atomicInteger = new AtomicInteger();
      String originId = entity.getOriginId();
      Integer groupId = entity.getGroupId();
      String templateId = entity.templateId;
      String tenantId = entity.tenantId;
      Long processId = entity.processId;


      try(Response response=httpclient.newCall(request).execute()){

        if(log.isInfoEnabled())
          log.info(aMarker,"coproProcessor consumer process response with status{}, and message as {}, ", response.isSuccessful(), response.message());
        if (response.isSuccessful()) {
          log.info(aMarker,"coproProcessor consumer process response status {}",response.message());
          String responseParse = Objects.requireNonNull(response.body()).string();
          log.info(aMarker,"coproProcessor consumer process response body {}",responseParse);
          JSONObject parentResponse = new JSONObject(responseParse);
          JSONArray filePathArray = new JSONArray(parentResponse.get("csvTablesPath").toString());
          log.info(aMarker,"coproProcessor consumer process response body filePathArray {}",filePathArray);
          filePathArray.forEach(s -> {
            parentObj.add(
                    TableExtractionOutputTable
                            .builder()
                            .processedFilePath(String.valueOf(s))
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .groupId(groupId)
                            .templateId(templateId)
                            .tenantId(tenantId)
                            .processId(processId)
                            .paperNo(atomicInteger.incrementAndGet())
                            .status("COMPLETED")
                            .stage(tableExtractionProcessName)
                            .message("Table Extraction macro completed")
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .rootPipelineId(rootPipelineId)
                            .build());
          });
        }else{
          parentObj.add(
                  TableExtractionOutputTable
                          .builder()
                          .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                          .groupId(groupId)
                          .processId(processId)
                          .templateId(templateId)
                          .tenantId(tenantId)
                          .paperNo(atomicInteger.incrementAndGet())
                          .status("FAILED")
                          .stage(tableExtractionProcessName)
                          .message(response.message())
                          .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                          .rootPipelineId(rootPipelineId)
                          .build());
          log.error(aMarker, "Error in response {}",response.message());
        }
      }catch (Exception exception) {
        parentObj.add(
                TableExtractionOutputTable
                        .builder()
                        .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                        .groupId(groupId)
                        .processId(processId)
                        .templateId(templateId)
                        .tenantId(tenantId)
                        .paperNo(atomicInteger.incrementAndGet())
                        .status("FAILED")
                        .stage(tableExtractionProcessName)
                        .message(exception.getMessage())
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .rootPipelineId(rootPipelineId)
                        .build());
        HandymanException handymanException = new HandymanException(exception);
        HandymanException.insertException("Table Extraction  consumer failed for originId "+ originId, handymanException, this.action);
        log.error(aMarker, "The Exception occurred in request {}", request, exception);
      }
      atomicInteger.set(0);
      log.info(aMarker,"coproProcessor consumer process with output entity {}",parentObj);
      return parentObj;
    }
  }
  @Override
  public boolean executeIf() throws Exception {
    return tableExtraction.getCondition();
  }
  //1. input pojo from select query, which implements CoproProcessor.Entity
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class TableExtractionInputTable implements CoproProcessor.Entity {
    private String originId;
    private Long processId;
    private Integer groupId;
    private String tenantId;
    private String templateId;
    private String filePath;
    private String outputDir;
    private Long rootPipelineId;



    @Override
    public List<Object> getRowData() {
      return null;
    }
  }

  //2. output pojo for table, which implements CoproProcessor.Entity
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class TableExtractionOutputTable implements CoproProcessor.Entity {

    private String originId;
    private Integer groupId;
    private String tenantId;
    private Long processId;
    private String templateId;
    private String processedFilePath;
    private Integer paperNo;
    private String status;
    private String stage;
    private String message;
    private Timestamp createdOn;
    private Long rootPipelineId;

    @Override
    public List<Object> getRowData() {
      return Stream.of(this.originId, this.groupId,this.tenantId,this.templateId, this.processedFilePath,
              this.paperNo,this.status,this.stage,this.message,this.createdOn,this.processId,this.rootPipelineId).collect(Collectors.toList());
    }
  }
}
