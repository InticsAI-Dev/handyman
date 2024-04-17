package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CheckboxVqa;
import in.handyman.raven.lib.model.paperitemizer.ProcessAuditOutputTable;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "CheckboxVqa"
)
public class CheckboxVqaAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private static CheckboxVqa checkboxVqa = new CheckboxVqa();

  private final Marker aMarker;

  public CheckboxVqaAction(final ActionExecutionAudit action, final Logger log,
                           final Object checkboxVqa) {
    this.checkboxVqa = (CheckboxVqa) checkboxVqa;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" CheckboxVqa:" + this.checkboxVqa.getName());
  }
  private static final List<ProcessAuditOutputTable> processOutputAudit = new ArrayList<>();

  @Override
  public void execute() throws Exception {
    try {
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(checkboxVqa.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      log.info(aMarker, "Urgency Triage Action for {} has been started", checkboxVqa.getName());
      final String insertQuery = "INSERT INTO urgency_triage.chk_triage_transaction_"+checkboxVqa.getProcessID()+"(created_on, created_user_id, last_updated_on, last_updated_user_id, process_id, group_id, tenant_id, model_score, origin_id, paper_no, template_id, model_id, triage_label, triage_state, paper_type, status, stage, message, checkbox_bbox)" +
              "values(now(),?,now(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      final List<URL> urls = Optional.ofNullable(action.getContext().get("copro.checkbox-vqa.url")).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
        try {
          return new URL(s1);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL ", e);
          throw new HandymanException("Error in processing the URL", e, action);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());

      final CoproProcessor<CheckboxVqaInputTable, CheckboxVqaOutputTable> coproProcessor =
              new CoproProcessor<>(new LinkedBlockingQueue<>(),
                      CheckboxVqaOutputTable.class,
                      CheckboxVqaInputTable.class,
                      jdbi, log,
                      new CheckboxVqaInputTable(), urls, action);
      coproProcessor.startProducer(checkboxVqa.getQuerySet(), Integer.valueOf(action.getContext().get("read.batch.size")));
      Thread.sleep(1000);
      coproProcessor.startConsumer(insertQuery,  Integer.valueOf(action.getContext().get("ut.consumer.API.count")), Integer.valueOf(action.getContext().get("write.batch.size")), new CheckboxVqaConsumerProcess(log, aMarker, action));
      log.info(aMarker, " Urgency Triage has been completed {}  ", checkboxVqa.getName());
    } catch (Exception t) {
      action.getContext().put(checkboxVqa.getName() + ".isSuccessful", "false");
      log.error(aMarker, "Error at urgency triage execute method {}", ExceptionUtil.toString(t));
      throw new HandymanException("error in execute method for urgency triage", t, action);

    }
  }

  @Override
  public boolean executeIf() throws Exception {
    return checkboxVqa.getCondition();
  }

  public static class CheckboxVqaConsumerProcess implements CoproProcessor.ConsumerProcess<CheckboxVqaInputTable, CheckboxVqaOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public CheckboxVqaConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
      this.log = log;
      this.aMarker = aMarker;
      this.action = action;
    }

    @Override
    public List<CheckboxVqaOutputTable> process(URL endpoint, CheckboxVqaInputTable entity) throws Exception {

      List<CheckboxVqaOutputTable> parentObj = new ArrayList<>();
      final ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("inputFilePath", entity.getFilePath());
      objectNode.put("cadModelPath", checkboxVqa.getCadModelPath());
      objectNode.put("cdModelPath", checkboxVqa.getCdModelPath());
      objectNode.put("crModelPath", checkboxVqa.getCrModelPath());
      objectNode.put("textModel", checkboxVqa.getTextModel());
      objectNode.put("crWidth", checkboxVqa.getCrWidth());
      objectNode.put("crHeight", checkboxVqa.getCrHeight());
      objectNode.put("outputDir", checkboxVqa.getOutputDir());
      log.info(aMarker, "Request List {}", objectNode);

      Request request = new Request.Builder().url(endpoint)
              .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();
      log.debug(aMarker, "The Request Details: {}", objectNode);


      String createdUserId = entity.getCreatedUserId();
      String lastUpdatedUserId = entity.getLastUpdatedUserId();
      Long tenantId = entity.getTenantId();
      Double modelScore = entity.getModelScore();
      Long processId = entity.getProcessId();
      Integer groupId = entity.getGroupId();
      String originId = entity.getOriginId();
      Integer paperNo = entity.getPaperNo();
      String templateId = entity.getTemplateId();
      String modelId = entity.getModelId();
      try (Response response = httpclient.newCall(request).execute()) {
        String responseBody = Objects.requireNonNull(response.body()).string();
        processOutputAudit.add(
                ProcessAuditOutputTable.builder()
                        .originId(originId)
                        .batchId("1")
                        .endpoint(String.valueOf(endpoint))
                        .rootPipelineId(action.getParentPipelineId())
                        .request(String.valueOf(request))
                        .response(String.valueOf(response))
                        .stage("TRIAGE_CHECKBOX")
                        .message("Urgency Triage Finished")
                        .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                        .build());
        if (response.isSuccessful()) {
          String checkboxState = Optional.ofNullable(mapper.readTree(responseBody).get("checkbox_state")).map(JsonNode::asText).orElse(null);
          String extractedPrintedText = Optional.ofNullable(mapper.readTree(responseBody).get("extracted_printed_text")).map(JsonNode::asText).orElse(null);
          String paperFinalResult = Optional.ofNullable(mapper.readTree(responseBody).get("paper_type")).map(JsonNode::asText).orElse(null);
          final String checkboxBoundingBox = Optional.ofNullable(mapper.readTree(responseBody).get("bbox")).map(JsonNode::toString).orElse(null);
          parentObj.add(CheckboxVqaOutputTable.builder()
                  .createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                  .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                  .tenantId(Optional.ofNullable(tenantId).map(Long::valueOf).orElse(null))
                  .modelScore(Optional.ofNullable(modelScore).map(String::valueOf).map(Double::parseDouble).orElse(null))
                  .processId(Optional.ofNullable(processId).map(String::valueOf).map(Long::parseLong).orElse(null))
                  .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                  .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                  .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .triageLabel(extractedPrintedText)
                  .bBox(checkboxBoundingBox)
                  .triageState(checkboxState)
                  .paperType(paperFinalResult)
                  .status("COMPLETED")
                  .stage("TRIAGE_CHECKBOX")
                  .message("Urgency Triage Finished")
                  .build());
          log.info(aMarker, "Execute for urgency triage {}",response);
        } else {
          parentObj.add(CheckboxVqaOutputTable.builder()
                  .createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                  .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                  .tenantId(Optional.ofNullable(tenantId).map(Long::valueOf).orElse(null))
                  .modelScore(Optional.ofNullable(modelScore).map(String::valueOf).map(Double::parseDouble).orElse(null))
                  .processId(Optional.ofNullable(checkboxVqa.getProcessID()).map(String::valueOf).map(Long::parseLong).orElse(null))
                  .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                  .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                  .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .status("FAILED")
                  .stage("TRIAGE_CHECKBOX")
                  .message(response.message())
                  .build());
          log.error(aMarker, "The Exception occurred in urgency triage {}",response);
          processOutputAudit.add(
                  ProcessAuditOutputTable.builder()
                          .originId(originId)
                          .batchId("1")
                          .rootPipelineId(action.getParentPipelineId())
                          .stage("TRIAGE_CHECKBOX")
                          .message(response.message())
                          .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                          .build());
        }
      } catch (Exception e) {
        parentObj.add(CheckboxVqaOutputTable.builder()
                .createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                .tenantId(Optional.ofNullable(tenantId).map(Long::valueOf).orElse(null))
                .modelScore(Optional.ofNullable(modelScore).map(String::valueOf).map(Double::parseDouble).orElse(null))
                .processId(Optional.ofNullable(checkboxVqa.getProcessID()).map(String::valueOf).map(Long::parseLong).orElse(null))
                .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                .status("FAILED")
                .stage("TRIAGE_CHECKBOX")
                .message(ExceptionUtil.toString(e))
                .build());
        log.error(aMarker, "The Exception occurred in urgency triage {}", ExceptionUtil.toString(e));
        HandymanException handymanException = new HandymanException(e);
        HandymanException.insertException("Blank Page removal consumer failed for batch/group "+ groupId, handymanException, this.action);

      }
      return parentObj;
    }

    @Override
    public ProcessAuditOutputTable processAudit() throws Exception {
      return null;
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  public static class CheckboxVqaInputTable implements CoproProcessor.Entity {
    private String createdUserId;
    private String lastUpdatedUserId;
    private Long tenantId;
    private Double modelScore;
    private Long processId;
    private Integer groupId;
    private String originId;
    private Integer paperNo;
    private String templateId;
    private String modelId;
    private String filePath;

    @Override
    public List<Object> getRowData() {
      return null;
    }
  }


  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  public static class CheckboxVqaOutputTable implements CoproProcessor.Entity {

    private String createdUserId;
    private String lastUpdatedUserId;
    private Long tenantId;
    private Double modelScore;
    private Long processId;
    private Integer groupId;
    private String originId;
    private Integer paperNo;
    private String templateId;
    private Integer modelId;
    private String triageState;
    private String triageLabel;
    private String paperType;
    private String status;
    private String stage;
    private String message;
    private String bBox;

    @Override
    public List<Object> getRowData() {
      return Stream.of(this.createdUserId, this.lastUpdatedUserId, this.processId, this.groupId, this.tenantId, this.modelScore,
              this.originId, this.paperNo, this.templateId, this.modelId, this.triageLabel, this.triageState,
              this.paperType, this.status, this.stage, this.message, this.bBox
      ).collect(Collectors.toList());
    }
  }
}