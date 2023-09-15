


package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.MasterdataComparison;
import in.handyman.raven.lib.model.common.*;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import in.handyman.raven.util.InstanceUtil;
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

/**
 /**
 /**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "MasterdataComparison"
)
public class MasterdataComparisonAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final MasterdataComparison masterdataComparison;
  private String name;

  private String resourceConn;

  private String matchResult;

  private String inputSet;
  private Boolean condition = true;
  final String URI;
  private final Marker aMarker;
  final ObjectMapper MAPPER;
  final OkHttpClient httpclient;
  private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");

  private final Integer writeBatchSize = 1000;
  private static String httpClientTimeout = new String();


  public MasterdataComparisonAction(final ActionExecutionAudit action, final Logger log,
                                    final Object intellimatch) {
    this.masterdataComparison = (MasterdataComparison) intellimatch;
    this.action = action;
    this.log = log;
    this.URI = action.getContext().get("copro.masterdata-comparison.url");
    this.MAPPER = new ObjectMapper();
    this.httpclient = InstanceUtil.createOkHttpClient();
    this.aMarker = MarkerFactory.getMarker(" Intellimatch:" + this.masterdataComparison.getName());
    this.httpClientTimeout = action.getContext().get("okhttp.client.timeout");
  }



  @Override
  public void execute() throws Exception {
    log.info(aMarker, "master data comparison process for {} has been started" + masterdataComparison.getName());
    try {

      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(masterdataComparison.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      // build insert prepare statement with output table columns
      final String insertQuery = "INSERT INTO " + masterdataComparison.getMatchResult() +
              " ( origin_id, paper_no,eoc_identifier,created_on, actual_value, extracted_value,intelli_match,status,stage,message, root_pipeline_id)" +
              " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ? , ?);";
      log.info(aMarker, "master data comparison Insert query {}", insertQuery);

      //3. initiate copro processor and copro urls
      final List<URL> urls = Optional.ofNullable(action.getContext().get("copro.masterdata-comparison.url")).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
        try {
          return new URL(s1);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL ", e);
          throw new HandymanException("Error in processing the URL", e, action);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());
      log.info(aMarker, "master data comparison copro urls {}", urls);

      final CoproProcessor<MasterDataInputTable, MasterDataOutputTable> coproProcessor =
              new CoproProcessor<>(new LinkedBlockingQueue<>(),
                      MasterDataOutputTable.class,
                      MasterDataInputTable.class,
                      jdbi, log,
                      new MasterDataInputTable(), urls, action);
      log.info(aMarker, "master data comparison copro Processor initialization  {}", coproProcessor);

      //4. call the method start producer from coproprocessor
      coproProcessor.startProducer(masterdataComparison.getInputSet(), Integer.valueOf(action.getContext().get("read.batch.size")));
      log.info(aMarker, "master data comparison coproProcessor startProducer called read batch size {}", action.getContext().get("read.batch.size"));
      Thread.sleep(1000);
      coproProcessor.startConsumer(insertQuery, Integer.valueOf(action.getContext().get("consumer.masterdata.API.count")), Integer.valueOf(action.getContext().get("write.batch.size")),
              new MasterdataComparisonProcess(log, aMarker, action));
      log.info(aMarker, "master data comparison coproProcessor startConsumer called consumer count {} write batch count {} ", Integer.valueOf(action.getContext().get("consumer.masterdata.API.count")), Integer.valueOf(action.getContext().get("write.batch.size")));

    } catch (Exception ex) {
        log.error(aMarker, "Error in execute method for Drug Match {} ", ExceptionUtil.toString(ex));
        throw new HandymanException("Error in execute method for Drug Match", ex, action);
    }
      log.info(aMarker, "master data comparison process for {} has been completed" , masterdataComparison.getName());
  }



  public static class MasterdataComparisonProcess implements CoproProcessor.ConsumerProcess<MasterDataInputTable, MasterDataOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    public final ActionExecutionAudit action;
    final ObjectMapper mapper;
    private final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
            .writeTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
            .readTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
            .build();
    String URI;

    public MasterdataComparisonProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
      this.log = log;
      this.aMarker = aMarker;
      this.mapper = new ObjectMapper();
      this.action = action;

    }

    @Override
    public List<MasterDataOutputTable> process(URL endpoint, MasterDataInputTable result) throws Exception {
      log.info(aMarker, "coproProcessor consumer process started with endpoint {} and entity {}", endpoint, result);
      List<MasterDataOutputTable> parentObj = new ArrayList<>();
      AtomicInteger atomicInteger = new AtomicInteger();


      String eocIdentifier = result.getEocIdentifier();
      String extractedValue = result.getExtractedValue();
      String actualValue = result.getActualValue();
      Integer paperNo = result.getPaperNo();
      String originId = result.getOriginId();
      String process="MASTER_DATA";
      Long actionId= action.getActionId();
      Long rootpipelineId= result.getRootPipelineId();
      String inputSentence = result.getActualValue();
      List<String> sentence = Collections.singletonList(result.getExtractedValue());
      ObjectMapper objectMapper = new ObjectMapper();

//payload
      ComparisonPayload Comparisonpayload = new ComparisonPayload();
      Comparisonpayload.setRootPipelineId(rootpipelineId);
      Comparisonpayload.setActionId(actionId);
      Comparisonpayload.setProcess(process);
      Comparisonpayload.setInputSentence(inputSentence);
      Comparisonpayload.setSentence(sentence);
      String jsonInputRequest = objectMapper.writeValueAsString(Comparisonpayload);

      ComparisonResquest requests = new ComparisonResquest();
      TritonRequest requestBody = new TritonRequest();
      requestBody.setName("MASTERDATA COMPARISON START");
      requestBody.setShape(List.of(1, 1));
      requestBody.setDatatype("BYTES");
      requestBody.setData(Collections.singletonList(jsonInputRequest));

      //  requestBody.setData(Collections.singletonList(jsonNodeRequest));


     // requestBody.setData(Collections.singletonList(Comparisonpayload));

      TritonInputRequest tritonInputRequest=new TritonInputRequest();
      tritonInputRequest.setInputs(Collections.singletonList(requestBody));

      String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

      if (result.getActualValue() != null) {
        final Request request = new Request.Builder().url(endpoint)
                .post(RequestBody.create(jsonInputRequest.toString(), MediaTypeJSON)).build();
        log.info("master data comparison reqest body {}",request);
        try (Response response = httpclient.newCall(request).execute()) {
          log.info("master data comparison response body {}",response.body());
          String responseBody = Objects.requireNonNull(response.body()).string();
          if (response.isSuccessful()) {

            List<IntelliMatchCopro> output = mapper.readValue(responseBody, new TypeReference<>() {
            });
            double matchPercent = output.get(0) != null ? Math.round(output.get(0).getSimilarityPercent() * 100.0) / 100.0 : 0.0;
            ObjectMapper objectMappers = new ObjectMapper();
          ComparisonResponse Response = objectMappers.readValue(responseBody, ComparisonResponse.class);
            if (Response.getOutputs() != null && !Response.getOutputs().isEmpty()) {
              Response.getOutputs().forEach(o -> {
                o.getData().forEach(ComparisonDataItem -> {
                  parentObj.add(
                          MasterDataOutputTable
                                  .builder()
                                  .originId(originId)
                                  .eocIdentifier(eocIdentifier)
                                  .paperNo(paperNo)
                                  .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                  .extractedValue(extractedValue)
                                  .actualValue(actualValue)
                                  .intelliMatch(ComparisonDataItem.getSimilarityPercent())
                                  .status("COMPLETED")
                                  .stage("MASTER-DATA-COMPARISON")
                                  .message("Master data comparison macro completed")
                                  .rootPipelineId(result.getRootPipelineId())
                                  .build());

                });
              });
            }
          } else {
            parentObj.add(
                    MasterDataOutputTable.builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .eocIdentifier(eocIdentifier)
                            .paperNo(paperNo)
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .extractedValue(extractedValue)
                            .actualValue(actualValue)
                            .intelliMatch(0)
                            .status("FAILED")
                            .stage("MASTER-DATA-COMPARISON")
                            .message("Master data comparison macro failed")
                            .rootPipelineId(result.getRootPipelineId())
                            .build()
            );
            log.error(aMarker, "The Exception occurred in master data comparison by {} ", response);
            throw new HandymanException(responseBody);
          }
        } catch (Exception exception) {
          parentObj.add(
                  MasterDataOutputTable.builder()
                          .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                          .eocIdentifier(eocIdentifier)
                          .paperNo(paperNo)
                          .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                          .extractedValue(extractedValue)
                          .actualValue(actualValue)
                          .intelliMatch(0)
                          .status("FAILED")
                          .stage("MASTER-DATA-COMPARISON")
                          .message("Master data comparison macro failed")
                          .rootPipelineId(result.getRootPipelineId())
                          .build()
          );

                    log.error(aMarker, "Exception occurred in copro api for master data comparison - {} ", ExceptionUtil.toString(exception));
                    HandymanException handymanException = new HandymanException(exception);
                    HandymanException.insertException("Paper classification (hw-detection) consumer failed for originId "+ originId, handymanException, this.action);
                }
            } else {
                parentObj.add(
                        MasterDataOutputTable.builder()
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .eocIdentifier(eocIdentifier)
                                .paperNo(paperNo)
                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                .extractedValue(extractedValue)
                                .actualValue(actualValue)
                                .intelliMatch(0)
                                .status("COMPLETED")
                                .stage("MASTER-DATA-COMPARISON")
                                .message("Master data comparison macro completed")
                                .build()
                );
                log.info(aMarker, "coproProcessor consumer process with empty actual value entity {}", result);
            }
            atomicInteger.set(0);
            log.info(aMarker, "coproProcessor consumer process with output entity {}", parentObj);
            return parentObj;
        }
    }


  @Override
  public boolean executeIf() throws Exception {
    return masterdataComparison.getCondition();
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class IntelliMatchCopro {
    String sentence;
    double similarityPercent;
  }


}
