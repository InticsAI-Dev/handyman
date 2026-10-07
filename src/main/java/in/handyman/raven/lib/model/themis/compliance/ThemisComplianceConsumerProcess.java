package in.handyman.raven.lib.model.themis.compliance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.ThemisComplianceAction;
import in.handyman.raven.lib.model.kvp.llm.neon.processor.NeonKvpExtractionRequest;
import in.handyman.raven.lib.model.kvp.llm.neon.processor.NeonKvpExtractionResponse;
import in.handyman.raven.lib.model.kvp.llm.neon.processor.NeonKvpLineItem;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Themis compliance consumer: calls copro (NEON) with the THEMIS_COMPLIANCE prompt (same
 * Triton/copro transport as neon kvp), then runs the Themis-specific deterministic logic in
 * handyman — it parses the LLM compliance report and computes the pass/warn/fail counts,
 * the 0-100 score and the overall verdict (COMPLIANT / NEEDS_REVIEW / NON_COMPLIANT). The LLM
 * provides the per-rule judgements; handyman owns the aggregation/scoring so the verdict is
 * deterministic rather than whatever the model happens to emit.
 */
public class ThemisComplianceConsumerProcess implements CoproProcessor.ConsumerProcess<ThemisComplianceInputTable, ThemisComplianceOutputTable> {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.neon.kvp.activator";
    public static final String PROCESS_NAME = "THEMIS_COMPLIANCE_ACTION";
    private static final int CRITICAL_WEIGHT = 8;
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;

    public ThemisComplianceConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, ThemisComplianceAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        int timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder().connectTimeout(timeOut, TimeUnit.MINUTES)
                .writeTimeout(timeOut, TimeUnit.MINUTES).readTimeout(timeOut, TimeUnit.MINUTES).build();
    }

    @Override
    public List<ThemisComplianceOutputTable> process(URL endpoint, ThemisComplianceInputTable entity) throws Exception {
        List<ThemisComplianceOutputTable> parentObj = new ArrayList<>();

        NeonKvpExtractionRequest req = new NeonKvpExtractionRequest();
        req.setRootPipelineId(entity.getRootPipelineId());
        req.setActionId(action.getActionId());
        req.setProcess(PROCESS_NAME);
        req.setInputFilePath(entity.getInputFilePath());
        req.setGroupId(entity.getGroupId());
        req.setPrompt(entity.getPrompt());
        req.setProcessId(entity.getProcessId());
        req.setModelRegistry(entity.getModelRegistry());
        req.setPaperNo(entity.getPaperNo());
        req.setTextModel(entity.getTextModel());
        req.setTenantId(entity.getTenantId());
        req.setResponseFormat(entity.getResponseFormat());
        req.setOriginId(entity.getOriginId());
        req.setPaperType(entity.getPaperType());

        String jsonInputRequest = mapper.writeValueAsString(req);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("THEMIS COMPLIANCE START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));
        String jsonRequest = mapper.writeValueAsString(tritonInputRequest);

        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);
        if (Objects.equals("false", tritonRequestActivator)) {
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
            callCopro(entity, request, parentObj);
        } else {
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();
            callCopro(entity, request, parentObj);
        }
        return parentObj;
    }

    private void callCopro(ThemisComplianceInputTable entity, Request request, List<ThemisComplianceOutputTable> parentObj) {
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                NeonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, NeonKvpExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(dataItem -> {
                        try {
                            buildOutput(entity, dataItem, parentObj);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }));
                }
            } else {
                parentObj.add(failRow(entity, response.message()));
                log.info(aMarker, "Error response from copro server {}", response.message());
            }
        } catch (IOException e) {
            parentObj.add(failRow(entity, ExceptionUtil.toString(e)));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Themis compliance consumer failed for group " + entity.getGroupId(), handymanException, this.action);
            log.error(aMarker, "Exception calling copro for themis compliance {}", ExceptionUtil.toString(e));
        }
    }

    private void buildOutput(ThemisComplianceInputTable entity, String dataItem, List<ThemisComplianceOutputTable> parentObj) throws Exception {
        NeonKvpLineItem lineItem = mapper.readValue(dataItem, NeonKvpLineItem.class);
        String reportJson = mapper.writeValueAsString(lineItem.getResponse());
        Verdict v = score(reportJson);
        parentObj.add(ThemisComplianceOutputTable.builder()
                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                .createdUserId(entity.getTenantId())
                .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                .lastUpdatedUserId(entity.getTenantId())
                .inputFilePath(entity.getInputFilePath())
                .totalResponseJson(reportJson)
                .complianceScore(v.score)
                .overallStatus(v.status)
                .passCount(v.passCount)
                .warnCount(v.warnCount)
                .failCount(v.failCount)
                .textModel(entity.getTextModel())
                .paperType(entity.getPaperType())
                .paperNo(entity.getPaperNo())
                .originId(entity.getOriginId())
                .processId(entity.getProcessId())
                .process(entity.getProcess())
                .groupId(entity.getGroupId())
                .tenantId(entity.getTenantId())
                .actionId(action.getActionId())
                .rootPipelineId(entity.getRootPipelineId())
                .batchId(entity.getBatchId())
                .submissionId(entity.getSubmissionId())
                .modelRegistry(entity.getModelRegistry())
                .responseFormat(entity.getResponseFormat())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(PROCESS_NAME)
                .message("Themis compliance completed")
                .build());
    }

    private ThemisComplianceOutputTable failRow(ThemisComplianceInputTable entity, String message) {
        return ThemisComplianceOutputTable.builder()
                .originId(entity.getOriginId())
                .paperNo(entity.getPaperNo())
                .groupId(entity.getGroupId())
                .inputFilePath(entity.getInputFilePath())
                .tenantId(entity.getTenantId())
                .actionId(action.getActionId())
                .processId(entity.getProcessId())
                .rootPipelineId(entity.getRootPipelineId())
                .process(entity.getProcess())
                .submissionId(entity.getSubmissionId())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(PROCESS_NAME)
                .message(message)
                .batchId(entity.getBatchId())
                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                .createdUserId(entity.getTenantId())
                .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                .lastUpdatedUserId(entity.getTenantId())
                .build();
    }

    /** Deterministic scoring/aggregation over the LLM compliance report (handyman owns the verdict). */
    private Verdict score(String reportJson) {
        Verdict v = new Verdict();
        try {
            JsonNode root = mapper.readTree(reportJson);
            JsonNode rules = root.get("rules");
            boolean criticalFail = false;
            if (rules != null && rules.isArray()) {
                for (JsonNode rule : rules) {
                    String verdict = rule.path("verdict").asText("").toUpperCase(Locale.ROOT);
                    int weight = rule.path("weight").asInt(1);
                    switch (verdict) {
                        case "PASS" -> v.passCount++;
                        case "WARN" -> v.warnCount++;
                        case "FAIL" -> {
                            v.failCount++;
                            if (weight >= CRITICAL_WEIGHT) criticalFail = true;
                        }
                        default -> { /* NOT_APPLICABLE / unknown: ignored */ }
                    }
                }
            }
            int evaluated = v.passCount + v.warnCount + v.failCount;
            // Prefer the model's explicit score when present and valid, else derive from counts.
            v.score = root.hasNonNull("score") && root.get("score").isInt()
                    ? root.get("score").asInt()
                    : (evaluated == 0 ? 0 : Math.round((v.passCount * 100f) / evaluated));
            if (criticalFail || v.failCount > 0) {
                v.status = criticalFail ? "NON_COMPLIANT" : "NEEDS_REVIEW";
            } else if (v.warnCount > 0) {
                v.status = "NEEDS_REVIEW";
            } else {
                v.status = evaluated == 0 ? "NEEDS_REVIEW" : "COMPLIANT";
            }
        } catch (Exception e) {
            log.warn(aMarker, "Could not score themis compliance report, defaulting NEEDS_REVIEW: {}", e.getMessage());
            v.status = "NEEDS_REVIEW";
        }
        return v;
    }

    private static final class Verdict {
        int score = 0;
        String status = "NEEDS_REVIEW";
        int passCount = 0;
        int warnCount = 0;
        int failCount = 0;
    }
}
