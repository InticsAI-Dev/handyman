package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.HwUrgencyTriage;
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
        actionName = "HwUrgencyTriage"
)
public class HwUrgencyTriageAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private static HwUrgencyTriage hwUrgencyTriage = new HwUrgencyTriage();

  private final Marker aMarker;

  public HwUrgencyTriageAction(final ActionExecutionAudit action, final Logger log,
                               final Object hwUrgencyTriage) {
    this.hwUrgencyTriage = (HwUrgencyTriage) hwUrgencyTriage;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" HwUrgencyTriage:" + this.hwUrgencyTriage.getName());
  }

  @Override
  public void execute() throws Exception {
    try {
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(hwUrgencyTriage.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      log.info(aMarker, "<-------Handwritten Urgency Triage Action for {} has been started------->", hwUrgencyTriage.getName());
      final String insertQuery = "INSERT INTO urgency_triage.hw_triage_transaction_"+hwUrgencyTriage.getProcessID()+"(created_on, created_user_id, last_updated_on, last_updated_user_id, process_id, group_id, tenant_id, model_score, origin_id, paper_no, template_id, model_registry_id, binary_model, multiclass_model, checkbox_model, status, stage, message)" +
              "values(now(),?,now(),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      final List<URL> urls = Optional.ofNullable(action.getContext().get("copro.hw-urgency-triage.url")).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
        try {
          return new URL(s1);
        } catch (MalformedURLException e) {
          log.error("Error in processing the URL ", e);
          throw new RuntimeException(e);
        }
      }).collect(Collectors.toList())).orElse(Collections.emptyList());

      final CoproProcessor<HwUrgencyTriageInputTable, HwUrgencyTriageOutputTable> coproProcessor =
              new CoproProcessor<>(new LinkedBlockingQueue<>(),
                      HwUrgencyTriageOutputTable.class,
                      HwUrgencyTriageInputTable.class,
                      jdbi, log,
                      new HwUrgencyTriageInputTable(), urls, action);
      coproProcessor.startProducer(hwUrgencyTriage.getQuerySet(), 1);
      Thread.sleep(1000);
      coproProcessor.startConsumer(insertQuery, 1, 1, new HwUrgencyTriageConsumerProcess(log, aMarker, action));
      log.info(aMarker, " Handwritten Urgency Triage has been completed {}  ", hwUrgencyTriage.getName());
    } catch (Throwable t) {
      action.getContext().put(hwUrgencyTriage.getName() + ".isSuccessful", "false");
      log.error(aMarker, "Error at handwritten urgency triage execute method {}", t);
    }
  }

  @Override
  public boolean executeIf() throws Exception {
    return hwUrgencyTriage.getCondition();
  }

  public static class HwUrgencyTriageConsumerProcess implements CoproProcessor.ConsumerProcess<HwUrgencyTriageInputTable, HwUrgencyTriageOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public HwUrgencyTriageConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
      this.log = log;
      this.aMarker = aMarker;
      this.action = action;
    }

    @Override
    public List<HwUrgencyTriageOutputTable> process(URL endpoint, HwUrgencyTriageInputTable entity) throws Exception {

      List<HwUrgencyTriageOutputTable> parentObj = new ArrayList<>();
      final ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("inputFilePath", entity.getFilePath());
      objectNode.put("binaryClassifierModelFilePath", hwUrgencyTriage.getBinaryClassifierModelFilePath());
      objectNode.put("multiClassifierModelFilePath", hwUrgencyTriage.getMultiClassifierModelFilePath());
      objectNode.put("checkboxClassifierModelFilePath", hwUrgencyTriage.getCheckboxClassifierModelFilePath());
      objectNode.putPOJO("synonyms", hwUrgencyTriage.getSynonyms().split(","));
      objectNode.putPOJO("binaryClassifierLabels", hwUrgencyTriage.getBinaryClassifierLabels().split(","));
      objectNode.putPOJO("multiClassifierLabels", hwUrgencyTriage.getMultiClassifierLabels().split(","));
      objectNode.putPOJO("checkboxClassifierLabels", hwUrgencyTriage.getCheckboxClassifierLabels().split(","));
      objectNode.put("outputDir", hwUrgencyTriage.getOutputDir());
      objectNode.put("binaryImageWidth", hwUrgencyTriage.getBinaryImageWidth());
      objectNode.put("binaryImageHeight", hwUrgencyTriage.getBinaryImageHeight());
      objectNode.put("multiImageWidth", hwUrgencyTriage.getMultiImageWidth());
      objectNode.put("multiImageHeight", hwUrgencyTriage.getMultiImageHeight());
      objectNode.put("checkboxImageWidth", hwUrgencyTriage.getCheckboxImageWidth());
      objectNode.put("checkboxImageHeight", hwUrgencyTriage.getCheckboxImageHeight());

      Request request = new Request.Builder().url(endpoint)
              .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();
      log.debug(aMarker, "The Request Details: {}", request);


      try (Response response = httpclient.newCall(request).execute()) {
        String responseBody = Objects.requireNonNull(response.body()).string();
        if (response.isSuccessful()) {
          String binaryModelOutput = Optional.ofNullable(mapper.readTree(responseBody).get("binary_model")).map(JsonNode::asText).orElseThrow();
          String multiClassModelOutput = Optional.ofNullable(mapper.readTree(responseBody).get("multiclass_model")).map(JsonNode::asText).orElseThrow();
          String checkboxModelOutput = Optional.ofNullable(mapper.readTree(responseBody).get("checkbox_model")).map(JsonNode::asText).orElseThrow();
          parentObj.add(HwUrgencyTriageOutputTable.builder()
                  .createdUserId(Optional.ofNullable(entity.getCreatedUserId()).map(String::valueOf).orElse(null))
                  .lastUpdatedUserId(Optional.ofNullable(entity.getLastUpdatedUserId()).map(String::valueOf).orElse(null))
                  .tenantId(Optional.ofNullable(entity.getTenantId()).map(String::valueOf).orElse(null))
                  .modelScore(Optional.ofNullable(entity.getModelScore()).map(String::valueOf).map(Double::parseDouble).orElse(null))
                  .processId(Optional.ofNullable(hwUrgencyTriage.getProcessID()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .groupId(Optional.ofNullable(entity.getGroupId()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                  .paperNo(Optional.ofNullable(entity.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .templateId(Optional.ofNullable(entity.getTemplateId()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .modelRegistryId(Optional.ofNullable(entity.getModelRegistryId()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .binaryModel(binaryModelOutput)
                  .multiClassModel(multiClassModelOutput)
                  .checkboxModel(checkboxModelOutput)
                  .status("COMPLETED")
                  .stage("TRIAGE-HANDWRITTEN")
                  .message("Handwritten Urgency Triage Finished")
                  .build());
          log.info(aMarker, "Execute for handwritten urgency triage",response);
        } else {
          parentObj.add(HwUrgencyTriageOutputTable.builder()
                  .createdUserId(Optional.ofNullable(entity.getCreatedUserId()).map(String::valueOf).orElse(null))
                  .lastUpdatedUserId(Optional.ofNullable(entity.getLastUpdatedUserId()).map(String::valueOf).orElse(null))
                  .tenantId(Optional.ofNullable(entity.getTenantId()).map(String::valueOf).orElse(null))
                  .modelScore(Optional.ofNullable(entity.getModelScore()).map(String::valueOf).map(Double::parseDouble).orElse(null))
                  .processId(Optional.ofNullable(hwUrgencyTriage.getProcessID()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .groupId(Optional.ofNullable(entity.getGroupId()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                  .paperNo(Optional.ofNullable(entity.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .templateId(Optional.ofNullable(entity.getTemplateId()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .modelRegistryId(Optional.ofNullable(entity.getModelRegistryId()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                  .status("FAILED")
                  .stage("TRIAGE-HANDWRITTEN")
                  .message("Handwritten Urgency Triage Failed due to Copro API Response")
                  .build());
          log.error(aMarker, "The Exception occurred in handwritten urgency triage",response);
        }
      } catch (Throwable throwable) {
        parentObj.add(HwUrgencyTriageOutputTable.builder()
                .createdUserId(Optional.ofNullable(entity.getCreatedUserId()).map(String::valueOf).orElse(null))
                .lastUpdatedUserId(Optional.ofNullable(entity.getLastUpdatedUserId()).map(String::valueOf).orElse(null))
                .tenantId(Optional.ofNullable(entity.getTenantId()).map(String::valueOf).orElse(null))
                .modelScore(Optional.ofNullable(entity.getModelScore()).map(String::valueOf).map(Double::parseDouble).orElse(null))
                .groupId(Optional.ofNullable(entity.getGroupId()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                .processId(Optional.ofNullable(hwUrgencyTriage.getProcessID()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .paperNo(Optional.ofNullable(entity.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                .templateId(Optional.ofNullable(entity.getTemplateId()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                .modelRegistryId(Optional.ofNullable(entity.getModelRegistryId()).map(String::valueOf).map(Integer::parseInt).orElse(null))
                .status("FAILED")
                .stage("TRIAGE-HANDWRITTEN")
                .message("Handwritten Urgency Triage Failed due to Copro API Request")
                .build());
        log.error(aMarker, "The Exception occurred in handwritten urgency triage", throwable);
      }
      return parentObj;
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  public static class HwUrgencyTriageInputTable implements CoproProcessor.Entity {
    private String createdUserId;
    private String lastUpdatedUserId;
    private String tenantId;
    private Integer processId;
    private Integer groupId;
    private Double modelScore;
    private String originId;
    private Integer paperNo;
    private String templateId;
    private String modelRegistryId;
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
  public static class HwUrgencyTriageOutputTable implements CoproProcessor.Entity {
    private String createdUserId;
    private String lastUpdatedUserId;
    private Integer processId;
    private Integer groupId;
    private String tenantId;
    private Double modelScore;
    private String originId;
    private Integer paperNo;
    private Integer templateId;
    private Integer modelRegistryId;
    private String binaryModel;
    private String multiClassModel;
    private String checkboxModel;
    private String status;
    private String stage;
    private String message;

    @Override
    public List<Object> getRowData() {
      return Stream.of(this.createdUserId, this.lastUpdatedUserId, this.processId, this.groupId, this.tenantId, this.modelScore,
              this.originId, this.paperNo, this.templateId, this.modelRegistryId,
              this.binaryModel, this.multiClassModel,this.checkboxModel, this.status, this.stage, this.message).collect(Collectors.toList());
    }
  }
}