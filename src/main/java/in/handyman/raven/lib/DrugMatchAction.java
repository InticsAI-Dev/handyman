package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.*;

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
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "DrugMatch"
)
public class DrugMatchAction implements IActionExecution {
  private final ActionExecutionAudit action;
  private final Logger log;
  private final DrugMatch drugMatch;
  private final Marker aMarker;

  public DrugMatchAction(final ActionExecutionAudit action, final Logger log,
                         final Object drugMatch) {
    this.drugMatch = (DrugMatch) drugMatch;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" DrugMatch:" + this.drugMatch.getName());
  }

  @Override
  public void execute() throws Exception {
    log.info(aMarker, "drug match process for {} has been started" , drugMatch.getName());
    try {
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(drugMatch.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      // build insert prepare statement with output table columns
      final String insertQuery = "INSERT INTO " + drugMatch.getDrugCompare() +
              " (origin_id ,eoc_identifier,paper_no,created_on,document_id,drug_name,drug_jcode,actual_value,status,stage,message, root_pipeline_id, batch_id)" +
              " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?);";
      log.info(aMarker, "Drug Match Insert query {}", insertQuery);

            //3. initiate copro processor and copro urls
            final List<URL> urls = Optional.ofNullable(action.getContext().get("drugname.api.url")).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new HandymanException("Error in processing the URL", e, action);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());
            log.info(aMarker, "Drug Match copro urls {}", urls);

      final CoproProcessor<DrugMatchInputTable, DrugMatchOutputTable> coproProcessor =
              new CoproProcessor<>(new LinkedBlockingQueue<>(),
                      DrugMatchOutputTable.class,
                      DrugMatchInputTable.class,
                      jdbi, log,
                      new DrugMatchInputTable(), urls, action);

      log.info(aMarker, "Drug Match coproProcessor initialization  {}", coproProcessor);

      //4. call the method start producer from coproprocessor
      coproProcessor.startProducer(drugMatch.getInputSet(), Integer.valueOf(action.getContext().get("read.batch.size")));
      log.info(aMarker, "Drug Match coproProcessor startProducer called read batch size {}", action.getContext().get("read.batch.size"));
      Thread.sleep(1000);
      coproProcessor.startConsumer(insertQuery, Integer.valueOf(action.getContext().get("consumer.masterdata.API.count")), Integer.valueOf(action.getContext().get("write.batch.size")),
              new DrugMatchConsumerProcess(log, aMarker, action));
      log.info(aMarker, "Drug Match coproProcessor startConsumer called consumer count {} write batch count {} ", Integer.valueOf(action.getContext().get("consumer.API.count")), Integer.valueOf(action.getContext().get("write.batch.size")));


        } catch (Exception ex) {
            log.error(aMarker, "error in execute method for Drug Match {}", ExceptionUtil.toString(ex));
            throw new HandymanException("error in execute method for Drug Match", ex, action);
        }
        log.info(aMarker, "drug match process for {} has been completed" , drugMatch.getName());
    }


  public static class DrugMatchConsumerProcess implements CoproProcessor.ConsumerProcess<DrugMatchInputTable, DrugMatchOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    public final ActionExecutionAudit action;
    private String appId;
    private String appKeyId;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    private final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");

    public DrugMatchConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
      this.log = log;
      this.aMarker = aMarker;
      this.action = action;
      this.appId = action.getContext().get("agadia.appId");
      this.appKeyId = action.getContext().get("agadia.appKeyId");
    }

    @Override
    public List<DrugMatchOutputTable> process(URL endpoint, DrugMatchInputTable result) throws Exception {
      log.info(aMarker, "coproProcessor consumer process started with endpoint {} and entity {}", endpoint, result);
      List<DrugMatchOutputTable> parentObj = new ArrayList<>();
      AtomicInteger atomicInteger = new AtomicInteger();
      String requestString;
      ObjectMapper mapper = new ObjectMapper();
      String drugName = result.getDrugName();
      if (drugName != null) {
        DrugNameRequest drugNameRequest = DrugNameRequest.builder()
                .drugName(result.drugName)
                .jCode(result.jCode)
                .build();
        try {
          requestString = mapper.writeValueAsString(drugNameRequest);
        } catch (JsonProcessingException e) {
          log.error(aMarker, "error in mapper value {}", ExceptionUtil.toString(e));
          throw new HandymanException("Error in mapper value", e, action);
        }

        final Request request = new Request.Builder().url(endpoint).header("appId", appId)
                .header("appKeyId", appKeyId)
                .post(RequestBody.create(requestString, MediaTypeJSON))
                .build();
        String originId = result.getOriginId();
        String eocIdentifier = result.eocIdentifier;
        Integer paperNo = result.paperNo;
        String documentId = result.documentId;
        String jCode = result.getJCode();
        try (Response response = httpclient.newCall(request).execute()) {
          String responseBody = Objects.requireNonNull(response.body()).string();
          if (response.isSuccessful()) {
            List<drugNameResponse> output = mapper.readValue(responseBody, new TypeReference<>() {
            });
            output.forEach(drugNameResponse -> {
              parentObj.add(
                      DrugMatchOutputTable.builder()
                              .originId(originId)
                              .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                              .eocIdentifier(eocIdentifier)
                              .paperNo(paperNo)
                              .documentId(documentId)
                              .drugJCode(jCode)
                              .drugName(drugName)
                              .actualValue(drugNameResponse.getDrugName())
                              .status("COMPLETED")
                              .stage("PAHUB-DRUGNAME")
                              .rootPipelineId(result.rootPipelineId)
                              .batchId(result.getBatchId())
                              .message("Drug name master data extracted").build());
            });

          } else {
            parentObj.add(
                    DrugMatchOutputTable
                            .builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .eocIdentifier(eocIdentifier)
                            .paperNo(paperNo)
                            .documentId(documentId)
                            .drugJCode(jCode)
                            .drugName(drugName)
                            .actualValue("")
                            .status("COMPLETED")
                            .stage("PAHUB-DRUGNAME")
                            .message("Drug name with pahub instance has been failed")
                            .rootPipelineId(result.rootPipelineId)
                            .batchId(result.getBatchId())
                            .build());
            log.error(aMarker, "failed for request {} and response {}", request, response);
            throw new HandymanException(responseBody);
          }
        } catch (Exception exception) {
          parentObj.add(
                  DrugMatchOutputTable
                          .builder()
                          .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                          .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                          .eocIdentifier(eocIdentifier)
                          .paperNo(paperNo)
                          .documentId(documentId)
                          .drugJCode(jCode)
                          .drugName(drugName)
                          .actualValue("")
                          .status("FAILED")
                          .stage("PAHUB-DRUGNAME")
                          .message("Drug name with pahub instance has been failed")
                          .rootPipelineId(result.rootPipelineId)
                          .batchId(result.getBatchId())
                          .build());
          log.error(aMarker, "error in hitting the file for mentioned request {} with exception {}", request, ExceptionUtil.toString(exception));
          HandymanException handymanException = new HandymanException(exception);
          HandymanException.insertException("Blank Page removal consumer failed for originId"+ originId, handymanException, this.action);
        }
      }

      atomicInteger.set(0);
      log.info(aMarker, "coproProcessor consumer process with output entity {}", parentObj);
      return parentObj;
    }
  }

  @Override
  public boolean executeIf() throws Exception {
    return drugMatch.getCondition();
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DrugMatchInputTable implements CoproProcessor.Entity {

    private String originId;
    private String eocIdentifier;
    private Integer paperNo;
    private String documentId;
    private String drugName;
    private String jCode;
    private Long rootPipelineId;
    private String batchId;

    @Override
    public List<Object> getRowData() {
      return null;
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DrugMatchOutputTable implements CoproProcessor.Entity {

    private String originId;
    private String eocIdentifier;
    private Integer paperNo;
    private String documentId;
    private String drugName;
    private String drugJCode;
    private Timestamp createdOn;
    private String actualValue;
    private String status;
    private String stage;
    private String message;
    private Long rootPipelineId;
    private String batchId;

    @Override
    public List<Object> getRowData() {
      return Stream.of(this.originId, this.eocIdentifier, this.paperNo, this.createdOn, this.documentId,
              this.drugName, this.drugJCode,
              this.actualValue, this.status, this.stage, this.message, this.rootPipelineId,this.batchId
      ).collect(Collectors.toList());
    }
  }
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DrugNameRequest {
    private String drugName;
    private String jCode;
  }
  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class drugNameResponse {
    private String drugName;
    private String ndc;
    private String jCode;
  }
}


