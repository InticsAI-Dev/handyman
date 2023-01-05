package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.EntityFilter;
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
        actionName = "EntityFilter"
)
public class EntityFilterAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final EntityFilter entityFilter;

    private final Marker aMarker;

    private final ObjectMapper mapper = new ObjectMapper();

    private final String URI;

    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");

    public EntityFilterAction(final ActionExecutionAudit action, final Logger log,
                              final Object entityFilter) {
        this.entityFilter = (EntityFilter) entityFilter;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" EntityFilter:" + this.entityFilter.getName());
        this.URI = action.getContext().get("copro.doc-filtering.url");
    }

    @Override
    public void execute() throws Exception {
        log.info(aMarker, "<-------Pixel Classifier Action for {} has been started------->", entityFilter.getName());
        final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        final ObjectNode objectNode = mapper.createObjectNode();

        objectNode.put("inputFilePath", entityFilter.getInputFilePath());
        objectNode.set("entityKeysToFilter", mapper.readTree(entityFilter.getEntityKeysToFilter()));
        objectNode.set("mandatoryKeysToFilter", mapper.readTree(entityFilter.getMandatoryKeysToFilter()));

        log.info(aMarker, " Input variables id : {}", action.getActionId());
        Request request = new Request.Builder().url(URI)
                .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();

        log.debug(aMarker, "Request has been build with the parameters \n URI : {} \n Input-File-Path : {} \n entity-key-filters : {} \n mandatory-key-filters : {}", URI, entityFilter.getInputFilePath(), entityFilter.getEntityKeysToFilter(), entityFilter.getMandatoryKeysToFilter());

        String name = entityFilter.getName() + "_response";
        log.debug(aMarker, "The Request Details: {}", request);
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                JSONObject responseObject = new JSONObject(responseBody);
                final Integer paperNo = Optional.ofNullable(entityFilter.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null);
                if (!responseObject.isEmpty()) {
                    final PaperFilteringResultTable paperFilteringResultTable = PaperFilteringResultTable
                            .builder()
                            .fileRefId(Optional.ofNullable(entityFilter.getDocId()).map(String::valueOf).orElse(null))
                            .groupId(Optional.ofNullable(entityFilter.getGroupId()).map(String::valueOf).orElse(null))
                            .filePath(Optional.ofNullable(responseObject.get("filePath")).map(String::valueOf).orElse(null))
                            .fileName(Optional.ofNullable(responseObject.get("fileName")).map(String::valueOf).orElse(null))
                            .pageContent(Optional.ofNullable(responseObject.get("page_content")).map(String::valueOf).orElse(null))
                            .entityResponse(Optional.ofNullable(responseObject.get("entity_response")).map(String::valueOf).orElse(null))
                            .entityConfidenceScore(Optional.ofNullable(responseObject.get("entity_confidence_score")).map(String::valueOf).orElse(null))
                            .mandatoryResponse(Optional.ofNullable(responseObject.get("mandatory_response")).map(String::valueOf).orElse(null))
                            .mandatoryConfidenceScore(Optional.ofNullable(responseObject.get("mandatory_confidence_score")).map(String::valueOf).orElse(null))
                            .paperNo(paperNo)
                            .build();
                    final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(entityFilter.getResourceConn());
                    jdbi.useTransaction(handle -> {
                        handle.createUpdate("INSERT INTO pre_process.paper_filtering_result(intics_reference_id,group_id,paper_no, preprocessed_file_path,preprocessed_file_name,page_content,entity_response,entity_confidence_score,mandatory_response,mandatory_confidence_score)" +
                                        " select :fileRefId, :groupId, :paperNo, :filePath , :fileName, :pageContent, :entityResponse ::jsonb, :entityConfidenceScore ::jsonb, :mandatoryResponse ::jsonb, :mandatoryConfidenceScore ::jsonb;")
                                .bindBean(paperFilteringResultTable)
                                .execute();
                    });
                }

                action.getContext().put(name.concat(".error"), "false");
                log.info(aMarker, "The Successful Response for {} --> {}", name, responseBody);
            } else {
                action.getContext().put(name.concat(".error"), "true");
                action.getContext().put(name.concat(".errorMessage"), responseBody);
                log.info(aMarker, "The Failure Response {} --> {}", name, responseBody);
            }
            log.info(aMarker, "<-------Text Filtering Action for {} has been completed ------->", entityFilter.getName());
        } catch (Exception e) {
            action.getContext().put(name.concat(".error"), "true");
            action.getContext().put(name.concat(".errorMessage"), e.getMessage());
            log.error(aMarker, "The Exception occurred ", e);
            throw new HandymanException("Failed to execute", e);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PaperFilteringResultTable {

        private String fileRefId;
        private Integer paperNo;
        private String groupId;
        private String filePath;
        private String fileName;
        private String pageContent;
        private String entityResponse;
        private String entityConfidenceScore;
        private String mandatoryResponse;
        private String mandatoryConfidenceScore;
        private String createdUserId;
        private String tenantId;
    }

    @Override
    public boolean executeIf() throws Exception {
        return entityFilter.getCondition();
    }
}