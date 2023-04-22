package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DataExtraction;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Auto Generated By Raven
 */
@ActionExecution(actionName = "DataExtraction")
public class DataExtractionAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final DataExtraction dataExtraction;
  private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
  private final Marker aMarker;
  private final ObjectMapper mapper = new ObjectMapper();

  private final String URI;


  public DataExtractionAction(final ActionExecutionAudit action, final Logger log, final Object dataExtraction) {
    this.dataExtraction = (DataExtraction) dataExtraction;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" DataExtraction:" + this.dataExtraction.getName());
    this.URI = action.getContext().get("copro.data-extraction.url");
  }

  @Override
  public void execute() throws Exception {
    try{
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(dataExtraction.getResourceConn());

      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      log.info(aMarker, "Data Extraction Action for {} has been started", dataExtraction.getName());

      final String insertQuery = "INSERT INTO info.data_extraction(origin_id,group_id,tenant_id,template_id,process_id, file_path, extracted_text,paper_no,file_name, status,stage,message,is_blank_page, created_on) " +
              " VALUES(?,? ,?,?,? ,?,?,?,?, ?,?,?,?,?)";
      final List<URL> urls = Optional.ofNullable(action.getContext().get("copro.data-extraction.url")).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
        try {
          return new URL(s1);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL ", e);
          throw new HandymanException("Error in processing the URL",e, action);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());

      final CoproProcessor<DataExtractionAction.DataExtractionInputTable, DataExtractionAction.DataExtractionOutputTable> coproProcessor = new CoproProcessor<>(new LinkedBlockingQueue<>(), DataExtractionAction.DataExtractionOutputTable.class, DataExtractionAction.DataExtractionInputTable.class, jdbi, log, new DataExtractionAction.DataExtractionInputTable(), urls, action);
      coproProcessor.startProducer(dataExtraction.getQuerySet(), Integer.valueOf(action.getContext().get("read.batch.size")));
      Thread.sleep(1000);
      coproProcessor.startConsumer(insertQuery, Integer.valueOf(action.getContext().get("consumer.API.count")), Integer.valueOf(action.getContext().get("write.batch.size")), new DataExtractionAction.DataExtractionConsumerProcess(log, aMarker, action));
      log.info(aMarker, " Data Extraction Action has been completed {}  ", dataExtraction.getName());
    }catch (Exception e){
      action.getContext().put(dataExtraction.getName() + ".isSuccessful", "false");
      log.error(aMarker,"error in execute method in data extraction", e);
      throw new HandymanException("error in execute method in data extraction", e, action);
    }

  }

  public static class DataExtractionConsumerProcess implements CoproProcessor.ConsumerProcess<DataExtractionAction.DataExtractionInputTable, DataExtractionAction.DataExtractionOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.MINUTES).writeTimeout(10, TimeUnit.MINUTES).readTimeout(10, TimeUnit.MINUTES).build();

    public DataExtractionConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
      this.log = log;
      this.aMarker = aMarker;
      this.action = action;
    }


    @Override
    public List<DataExtractionAction.DataExtractionOutputTable> process(URL endpoint, DataExtractionAction.DataExtractionInputTable entity) throws JsonProcessingException {
      List<DataExtractionAction.DataExtractionOutputTable> parentObj = new ArrayList<>();
      final ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("inputFilePath", entity.filePath);
      log.info(aMarker, " Input variables id : {}", action.getActionId());
      Request request = new Request.Builder().url(endpoint).post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();
      log.debug(aMarker, "Request has been build with the parameters \n URI :  \n page content : \n key-filters :{}", request.body());
      log.debug(aMarker, "The Request Details: {}", request);
        try (Response response = httpclient.newCall(request).execute()) {
        String responseBody = Objects.requireNonNull(response.body()).string();
        if (response.isSuccessful()) {
          JSONObject parentResponseObject = new JSONObject(responseBody);
          final String contentString=Optional.ofNullable(parentResponseObject.get("pageContent")).map(String::valueOf).orElse(null);
          final String flag=(!Objects.isNull(contentString) && contentString.length() > 5 ) ? "no" : "yes";
          parentObj.add(DataExtractionOutputTable.builder()
                  .filePath(new File(entity.getFilePath()).getAbsolutePath())
                  .extractedText(contentString)
                  .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                  .groupId(entity.getGroupId())
                  .fileName(Optional.ofNullable(parentResponseObject.get("fileName")).map(String::valueOf).orElse(null))
                  .paperNo(entity.paperNo)
                          .status("COMPLETED")
                          .stage("DATA_EXTRACTION")
                          .message("Data extraction macro completed")
                          .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                          .tenantId(entity.tenantId)
                          .templateId(entity.templateId)
                          .processId(entity.processId)
                          .isBlankPage(flag)
                  .build());
        }else{
          parentObj.add(DataExtractionOutputTable.builder()
                  .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                  .groupId(entity.getGroupId())
                  .paperNo(entity.paperNo)
                  .status("FAILED")
                  .stage("DATA_EXTRACTION")
                  .tenantId(entity.tenantId)
                  .templateId(entity.templateId)
                  .processId(entity.processId)
                  .message(response.message())
                  .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                  .build());
          log.info(aMarker, "The Exception occurred ");
        }
      } catch (Exception e) {
        parentObj.add(DataExtractionOutputTable.builder()
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .groupId(entity.getGroupId())
                .paperNo(entity.paperNo)
                .status("FAILED")
                .stage("DATA_EXTRACTION")
                .tenantId(entity.tenantId)
                .templateId(entity.templateId)
                .processId(entity.processId)
                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                .message(ExceptionUtil.toString(e))
                .build());

        log.info(aMarker, "The Exception occurred ", e);

      }
      return parentObj;
    }

  }

  @Override
  public boolean executeIf() throws Exception {
    return dataExtraction.getCondition();
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DataExtractionInputTable implements CoproProcessor.Entity {
    private String originId;
    private Integer groupId;
    private String filePath;
    private Integer paperNo;
    private String tenantId;
    private String templateId;
    private Long processId;

    @Override
    public List<Object> getRowData() {
      return null;
    }
  }


  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DataExtractionOutputTable implements CoproProcessor.Entity {

    private String originId;
    private Integer groupId;
    private String tenantId;
    private String templateId;
    private Long processId;
    private String filePath;
    private String extractedText;
    private String fileName;
    private Integer paperNo;
    private String status;
    private String stage;
    private String message;
    private String isBlankPage;
    private Timestamp createdOn;


    @Override
    public List<Object> getRowData() {
      return Stream.of(this.originId, this.groupId, this.tenantId,this.templateId,this.processId,this.filePath, this.extractedText,this.paperNo,this.fileName,this.status,this.stage,this.message,this.isBlankPage,this.createdOn).collect(Collectors.toList());
    }
  }

  @Data
  @AllArgsConstructor
  @Builder
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AssetAttributionResponse {
    private String pageContent;
  }

}