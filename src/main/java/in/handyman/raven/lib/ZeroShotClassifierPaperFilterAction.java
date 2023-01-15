package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ZeroShotClassifierPaperFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "ZeroShotClassifierPaperFilter"
)
public class ZeroShotClassifierPaperFilterAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final ZeroShotClassifierPaperFilter zeroShotClassifierPaperFilter;

  private final Marker aMarker;

  private final ObjectMapper mapper = new ObjectMapper();
  private final String URI;

  private static final MediaType MediaTypeJSON = MediaType
          .parse("application/json; charset=utf-8");

  public ZeroShotClassifierPaperFilterAction(final ActionExecutionAudit action, final Logger log,
                                             final Object zeroShotClassifierPaperFilter) {
    this.zeroShotClassifierPaperFilter = (ZeroShotClassifierPaperFilter) zeroShotClassifierPaperFilter;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" ZeroShotClassifierPaperFilter:" + this.zeroShotClassifierPaperFilter.getName());
    this.URI = action.getContext().get("copro.paper-filtering-zero-shot-classifier.url");
  }

  @Override
  public void execute() throws Exception {
    final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(zeroShotClassifierPaperFilter.getResourceConn());
    log.info(aMarker, "<-------Phrase match paper filter Action for {} has been started------->", zeroShotClassifierPaperFilter.getName());
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    final ObjectNode objectNode = mapper.createObjectNode();

    objectNode.put("pageContent", zeroShotClassifierPaperFilter.getPageContent());
    objectNode.set("keysToFilter", mapper.readTree(zeroShotClassifierPaperFilter.getKeysToFilter()));

    log.info(aMarker, " Input variables id : {}", action.getActionId());
    Request request = new Request.Builder().url(URI)
            .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();

    log.debug(aMarker, "Request has been build with the parameters \n URI : {} \n page content : {} \n key-filters : {} ", URI, zeroShotClassifierPaperFilter.getPageContent(), zeroShotClassifierPaperFilter.getKeysToFilter());

    String name = zeroShotClassifierPaperFilter.getName() + "_response";
    log.debug(aMarker, "The Request Details: {}", request);
    try (Response response = httpclient.newCall(request).execute()) {
      String responseBody = Objects.requireNonNull(response.body()).string();
      if (response.isSuccessful()) {
        final String processId = Optional.ofNullable(zeroShotClassifierPaperFilter.getProcessID()).map(String::valueOf).orElse(null);
        JSONObject parentResponseObject = new JSONObject(responseBody);
        final Integer paperNo = Optional.ofNullable(zeroShotClassifierPaperFilter.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null);
        JSONObject responseObject = new JSONObject(String.valueOf(parentResponseObject.get("entity_confidence_score")));
        responseObject.keys().forEachRemaining(key -> {
          ZeroShotClassifierPaperFilterAction.PaperFilteringZeroShotClassifierTable paperFilteringZeroShotClassifierTable = PaperFilteringZeroShotClassifierTable
                  .builder()
                  .originId(Optional.ofNullable(zeroShotClassifierPaperFilter.getOriginId()).map(String::valueOf).orElse(null))
                  .groupId(Optional.ofNullable(zeroShotClassifierPaperFilter.getGroupId()).map(String::valueOf).orElse(null))
                  .entity(Optional.ofNullable(key).map(String::valueOf).orElse(null))
                  .confidenceScore(Optional.ofNullable(responseObject.get(key)).map(String::valueOf).orElse(null))
                  .paperNo(paperNo)
                  .build();
          jdbi.useTransaction(handle -> handle.createUpdate("INSERT INTO paper.zero_shot_classifier_filtering_result_" + processId + "(origin_id,group_id,paper_no,synonym,confidence_score, created_on)" +
                          " select :originId,:groupId,:paperNo,:entity,:confidenceScore,now();")
                  .bindBean(paperFilteringZeroShotClassifierTable)
                  .execute());
        });
        action.getContext().put(name.concat(".error"), "false");
        log.info(aMarker, "The Successful Response for {} --> {}", name, responseBody);
      } else {
        action.getContext().put(name.concat(".error"), "true");
        action.getContext().put(name.concat(".errorMessage"), responseBody);
        log.info(aMarker, "The Failure Response {} --> {}", name, responseBody);
      }
      log.info(aMarker, "<-------Text Filtering Action for {} has been completed ------->", zeroShotClassifierPaperFilter.getName());
    } catch (Exception e) {
      action.getContext().put(name.concat(".error"), "true");
      action.getContext().put(name.concat(".errorMessage"), e.getMessage());
      log.info(aMarker, "The Exception occurred ", e);
      throw new HandymanException("Failed to execute", e);
    }
  }

  @Override
  public boolean executeIf() throws Exception {
    return zeroShotClassifierPaperFilter.getCondition();
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class PaperFilteringZeroShotClassifierTable {
    private String originId;
    private Integer paperNo;
    private String groupId;
    private String entity;
    private String confidenceScore;

  }
}