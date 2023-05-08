package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.PaperItemizer;
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
    actionName = "PaperItemizer"
)
public class PaperItemizerAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final PaperItemizer paperItemizer;

  private final Marker aMarker;

  public PaperItemizerAction(final ActionExecutionAudit action, final Logger log,
      final Object paperItemizer) {
    this.paperItemizer = (PaperItemizer) paperItemizer;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" PaperItemizer:"+this.paperItemizer.getName());
  }

  @Override
  public void execute(){
    try {
      log.info(aMarker, "paper itemizer Action has been started {}",paperItemizer);

      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(paperItemizer.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      final String outputDir = Optional.ofNullable(paperItemizer.getOutputDir()).map(String::valueOf).orElse(null);
      log.info(aMarker, "paper itemizer Action output directory {}",outputDir);
      //5. build insert prepare statement with output table columns
      final String insertQuery = "INSERT INTO " +paperItemizer.getResultTable()+
              "(origin_id,group_id,tenant_id,template_id,processed_file_path,paper_no, status,stage,message,created_on,process_id,root_pipeline_id) " +
              " VALUES(?,?, ?,?, ?,?, ?,?,?,? ,?,  ?)";
      log.info(aMarker, "paper itemizer Insert query {}", insertQuery);

      //3. initiate copro processor and copro urls
      final List<URL> urls = Optional.ofNullable(action.getContext().get("copro.paper-itemizer.url")).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
        try {
          return new URL(s1);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL ", e);
          throw new HandymanException("Error in processing the URL", e, action);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());
      log.info(aMarker, "paper itemizer copro urls {}", urls);

      final CoproProcessor<PaperItemizerInputTable, PaperItemizerOutputTable> coproProcessor =
              new CoproProcessor<>(new LinkedBlockingQueue<>(),
                      PaperItemizerOutputTable.class,
                      PaperItemizerInputTable.class,
                      jdbi, log,
                      new PaperItemizerInputTable(), urls, action);

      log.info(aMarker, "paper itemizer copro coproProcessor initialization  {}", coproProcessor);

      //4. call the method start producer from coproprocessor
      coproProcessor.startProducer(paperItemizer.getQuerySet(), Integer.valueOf(action.getContext().get("read.batch.size")));
      log.info(aMarker, "paper itemizer copro coproProcessor startProducer called read batch size {}",action.getContext().get("read.batch.size"));
      Thread.sleep(1000);
      coproProcessor.startConsumer(insertQuery, Integer.valueOf(action.getContext().get("consumer.API.count")), Integer.valueOf(action.getContext().get("write.batch.size")), new PaperItemizerConsumerProcess(log, aMarker,outputDir, action));
      log.info(aMarker, "paper itemizer copro coproProcessor startConsumer called consumer count {} write batch count {} ",Integer.valueOf(action.getContext().get("consumer.API.count")),Integer.valueOf(action.getContext().get("write.batch.size")));


    }catch(Exception ex){
      log.error(aMarker,"error in execute method for paper itemizer  ",ex);
      throw new HandymanException("error in execute method for paper itemizer", ex, action);
    }
  }

  public static class PaperItemizerConsumerProcess implements CoproProcessor.ConsumerProcess<PaperItemizerInputTable,PaperItemizerOutputTable>{

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

    public PaperItemizerConsumerProcess(Logger log, Marker aMarker, String outputDir, ActionExecutionAudit action) {
      this.log = log;
      this.aMarker = aMarker;
      this.outputDir = outputDir;
      this.action = action;
    }

    @Override
    public List<PaperItemizerOutputTable> process(URL endpoint, PaperItemizerInputTable entity) throws Exception {
      log.info(aMarker,"coproProcessor consumer process started with endpoint {} and entity {}",endpoint,entity);
      List<PaperItemizerOutputTable> parentObj = new ArrayList<>();
      final ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("inputFilePath", entity.filePath);
      objectNode.put("outputDir", outputDir);
      log.info(aMarker,"coproProcessor mapper object node {}",objectNode);
      Request request = new Request.Builder().url(endpoint)
              .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();
      log.info(aMarker,"coproProcessor Request builder {}",request);
      AtomicInteger atomicInteger = new AtomicInteger();
      try(Response response=httpclient.newCall(request).execute()){

        log.info(aMarker,"coproProcessor consumer process response  {}",response);
        if (response.isSuccessful()) {
          log.info(aMarker,"coproProcessor consumer process response status {}",response.message());
          String responseParse = Objects.requireNonNull(response.body()).string();
          log.info(aMarker,"coproProcessor consumer process response body {}",responseParse);
          JSONObject parentResponse = new JSONObject(responseParse);
          JSONArray filePathArray = new JSONArray(parentResponse.get("itemizedPapers").toString());
          log.info(aMarker,"coproProcessor consumer process response body filePathArray {}",filePathArray);
          filePathArray.forEach(s -> {
            parentObj.add(
                    PaperItemizerOutputTable
                            .builder()
                            .processedFilePath(String.valueOf(s))
                            .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                            .groupId(entity.getGroupId())
                            .templateId(entity.templateId)
                            .tenantId(entity.tenantId)
                            .processId(entity.processId)
                            .paperNo(atomicInteger.incrementAndGet())
                            .status("COMPLETED")
                            .stage("PAPER_ITEMIZER")
                            .message("Paper Itemizer macro completed")
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .rootPipelineId(entity.rootPipelineId)
                            .build());
          });
        }else{
          parentObj.add(
                  PaperItemizerOutputTable
                          .builder()
                          .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                          .groupId(entity.getGroupId())
                          .processId(entity.processId)
                          .templateId(entity.templateId)
                          .tenantId(entity.tenantId)
                          .paperNo(atomicInteger.incrementAndGet())
                          .status("FAILED")
                          .stage("PAPER_ITEMIZER")
                          .message(response.message())
                          .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                          .rootPipelineId(entity.rootPipelineId)
                          .build());
          log.error(aMarker, "Error in response {}",response.message());
        }
      }catch (Exception e) {
        parentObj.add(
                        PaperItemizerOutputTable
                        .builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .groupId(entity.getGroupId())
                        .processId(entity.processId)
                        .templateId(entity.templateId)
                        .tenantId(entity.tenantId)
                        .paperNo(atomicInteger.incrementAndGet())
                        .status("FAILED")
                        .stage("PAPER_ITEMIZER")
                        .message(ExceptionUtil.toString(e))
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .rootPipelineId(entity.rootPipelineId)
                        .build());
        log.error(aMarker, "The Exception occurred in request ", e);
      }
      atomicInteger.set(0);
      log.info(aMarker,"coproProcessor consumer process with output entity {}",parentObj);
      return parentObj;
    }
  }
  @Override
  public boolean executeIf() throws Exception {
    return paperItemizer.getCondition();
  }
  //1. input pojo from select query, which implements CoproProcessor.Entity
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PaperItemizerInputTable implements CoproProcessor.Entity {
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
  public static class PaperItemizerOutputTable implements CoproProcessor.Entity {

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
