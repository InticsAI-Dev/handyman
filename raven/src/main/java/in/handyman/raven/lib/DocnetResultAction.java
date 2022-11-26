package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DocnetResult;
import in.handyman.raven.util.CommonQueryUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.beanutils.converters.StringArrayConverter;
import org.jdbi.v3.core.Jdbi;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "DocnetResult"
)
public class DocnetResultAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final DocnetResult docnetResult;

    private final Marker aMarker;

    public DocnetResultAction(final ActionExecutionAudit action, final Logger log,
                              final Object docnetResult) {
        this.docnetResult = (DocnetResult) docnetResult;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" DocnetResult:" + this.docnetResult.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            log.info(aMarker, "<-------Docnet Result Action for {} has been started------->" + docnetResult.getName());
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(docnetResult.getResourceConn());
            final List<Map<String, Object>> results = new ArrayList<>();
            jdbi.useTransaction(handle -> {
                final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(docnetResult.getCoproResultSqlQuery());
                formattedQuery.forEach(sqlToExecute -> {
                    results.addAll(handle.createQuery(sqlToExecute).mapToMap().stream().collect(Collectors.toList()));
                });
            });

            if (results.size() > 0) {
                results.forEach(jsonData -> {
                    final String groupId = Optional.ofNullable(jsonData.get("group_id")).map(String::valueOf).orElse(null);
                    final Integer paperNo = Optional.ofNullable(jsonData.get("paper_no")).map(String::valueOf).map(Integer::parseInt).orElse(null);
                    final String fileRefId = Optional.ofNullable(jsonData.get("intics_reference_id")).map(String::valueOf).orElse(null);
                    final String sorAttributionType = Optional.ofNullable(jsonData.get("sor_item_name")).map(String::valueOf).orElse(null);
                    final String createdUserId = Optional.ofNullable(jsonData.get("created_user_id")).map(String::valueOf).orElse(null);
                    final String tenantId = Optional.ofNullable(jsonData.get("tenant_id")).map(String::valueOf).orElse(null);
                    final String response = Optional.ofNullable(jsonData.get("response")).map(String::valueOf).orElse(null);

                    JSONArray jObj = new JSONArray(response);

                    jObj.forEach(resultObject -> {
                        JSONObject obj = (JSONObject) resultObject;
                        JSONArray result = obj.getJSONArray("attributionResult");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject object = (JSONObject) result.get(i);
                            final DocnetResultTable docnetResult = DocnetResultTable.builder()
                                    .fileRefId(fileRefId)
                                    .paperNo(paperNo)
                                    .groupId(groupId)
                                    .triageResultId(12345678)
                                    .sorId(obj.getInt("sorId"))
                                    .sorItemName(obj.getString("sorKey"))
                                    .answer(object.getString("predictedAttributionValue"))
                                    .question(object.getString("question"))
                                    .createdUserId(createdUserId)
                                    .tenantId(tenantId)
                                    .confidenceScore(0)
                                    .build();
                            insertDocnutResult(jdbi, docnetResult);
                        }
                    });
                });
            }
        } catch (Exception e) {
            action.getContext().put(docnetResult.getName().concat(".error"), "true");
            log.info(aMarker, "The Exception occurred ", e);
            throw new HandymanException("Failed to execute", e);
        }

        log.info(aMarker, "<-------Docnut Result Action for {} has been completed------->" + docnetResult.getName());

    }


    @Override
    public boolean executeIf() throws Exception {
        return docnetResult.getCondition();
    }


    private void insertDocnutResult(final Jdbi jdbi, final DocnetResultTable docnetResultTable) {
        final List<Map<String, Object>> sorConfigList = new ArrayList<>();

        jdbi.useTransaction(handle -> {
            final List<String> formattedQuery = CommonQueryUtil
                    .getFormattedQuery(docnetResult.getWeightageSqlQuery() + " WHERE si.sor_key= '" + docnetResultTable.getSorItemName()+"'");
            formattedQuery.forEach(sqlToExecute -> {
                sorConfigList.addAll(handle.createQuery(sqlToExecute).mapToMap().stream().collect(Collectors.toList()));
            });
        });

        if (sorConfigList.size() > 0) {
            sorConfigList.forEach(jsonData -> {
                Integer wordCount = Optional.ofNullable(jsonData.get("word_count")).map(String::valueOf).map(Integer::parseInt).orElse(null);
                Integer characterCount = Optional.ofNullable(jsonData.get("character_count")).map(String::valueOf).map(Integer::parseInt).orElse(null);
                String datatypePattern =  Optional.ofNullable(jsonData.get("datatype_list")).map(String::valueOf).orElse(null);
                Integer threshold = Optional.ofNullable(jsonData.get("threshold")).map(String::valueOf).map(Integer::parseInt).orElse(null);

                TCSConfiguration config = TCSConfiguration.builder()
                        .wordCount(wordCount)
                        .characterCount(characterCount)
                        .datatypeList(datatypePattern)
                        .threshold(threshold)
                        .answer(docnetResultTable.getAnswer())
                        .build();
                int confidenceScore = createConfidenceScore(config);
                docnetResultTable.setConfidenceScore(confidenceScore);
            });
        }


        jdbi.useTransaction(handle -> {
            handle.createUpdate("INSERT INTO truth_attribution.docnet_result (intics_reference_id,paper_no,group_id,triage_result_id,sor_item_id,sor_item_name,question,answer,created_user_id,tenant_id,confidence_score)" +
                            " select  :fileRefId , :paperNo, :groupId, :triageResultId, :sorId, :sorItemName, :question, :answer, :createdUserId, :tenantId, :confidenceScore;")
                    .bindBean(docnetResultTable)
                    .execute();
        });
    }

    private Integer createConfidenceScore(TCSConfiguration config) {
        Integer confidenceScoreNegative = 0;
        try {
            Integer wordCount = config.wordCount;
            Integer charactersCount = config.characterCount;
            JSONArray datatypeList = new JSONArray(config.datatypeList);
            String answer = config.answer;
            Integer threshold = config.threshold;
            // Word count
            int tokenizedCount = countWordsUsingStringTokenizer(answer);
            confidenceScoreNegative = tokenizedCount >= wordCount ? confidenceScoreNegative : confidenceScoreNegative + threshold;
            // Datatype Pattern
            boolean datatypePattern = false;
            for (Object datatype : datatypeList) {
                String patterns = datatype.toString();
                boolean pattern = Pattern.matches(patterns, answer);
                if (pattern) {
                    datatypePattern = true;
                }
            }
            confidenceScoreNegative = datatypePattern ? confidenceScoreNegative : confidenceScoreNegative + threshold;
            // Character count
            int characterLength = answer.length();
            confidenceScoreNegative = characterLength <= charactersCount ? confidenceScoreNegative : confidenceScoreNegative + threshold;

            int confidenceScore = 100 - confidenceScoreNegative;
            return confidenceScore;
        } catch (Exception ex) {
            log.info(aMarker, "The Exception occurred in Confidence score validation ", ex);
            throw new HandymanException("Failed to execute", ex);
        }
    }

    private int countWordsUsingStringTokenizer(String sentence) {
        if (sentence == null || sentence.isEmpty()) {
            return 0;
        }
        StringTokenizer tokens = new StringTokenizer(sentence);
        return tokens.countTokens();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DocnetResultTable {

        private String fileRefId;
        private Integer paperNo;
        private String groupId;
        private Integer triageResultId;
        private Integer sorId;
        private String sorItemName;
        private String question;
        private String answer;
        private String createdUserId;
        private String tenantId;
        private Integer confidenceScore;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TCSConfiguration {
        private Integer wordCount;
        private Integer characterCount;
        private Integer threshold;
        private String datatypeList;
        private String answer;
    }
}
