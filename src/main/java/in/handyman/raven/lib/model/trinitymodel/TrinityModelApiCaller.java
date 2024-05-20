package in.handyman.raven.lib.model.trinitymodel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.TrinityModelAction;
import in.handyman.raven.lib.model.triton.ProcessAuditOutputTable;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TrinityModelApiCaller {

    private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final TrinityModelAction aAction;
    private final OkHttpClient httpclient;
    List<ProcessAuditOutputTable> processAuditOutputTables = new ArrayList<>();

    private final Jdbi jdbi;

    private final String node;

    public TrinityModelApiCaller(TrinityModelAction aAction, final String node, final Logger log, Jdbi jdbi) {
        this.aAction = aAction;
        this.node = node;
        this.httpclient = new OkHttpClient.Builder()
                .connectTimeout(Long.parseLong(aAction.getHttpClientTimeout()), TimeUnit.MINUTES)
                .writeTimeout(Long.parseLong(aAction.getHttpClientTimeout()), TimeUnit.MINUTES)
                .readTimeout(Long.parseLong(aAction.getHttpClientTimeout()), TimeUnit.MINUTES)
                .build();
        this.jdbi = jdbi;
    }

    public String computeTriton(final String inputPath, final String paperType, final List<String> questions, final String modelRegistry, final Long tenantId, ActionExecutionAudit action) throws JsonProcessingException {

        Long actionId = action.getActionId();
        Long rootpipelineId = action.getRootPipelineId();
        final String trinityProcessName = "VQA_VALUATION";
        ObjectMapper objectMapper = new ObjectMapper();


        TrinityModelPayload trinityModelPayload = new TrinityModelPayload();
        trinityModelPayload.setActionId(actionId);
        trinityModelPayload.setProcess(trinityProcessName);
        trinityModelPayload.setRootPipelineId(rootpipelineId);
        trinityModelPayload.setPaperType(paperType);
        trinityModelPayload.setTenantId(tenantId);
        trinityModelPayload.setAttributes(questions);
        trinityModelPayload.setInputFilePath(inputPath);
        trinityModelPayload.setModelRegistry(modelRegistry);

        String jsonInputRequest = objectMapper.writeValueAsString(trinityModelPayload);

        TritonRequest tritonRequest = getTritonRequestPaperType(paperType, modelRegistry, jsonInputRequest);


        TrinityModelRequest trinityModelRequest = new TrinityModelRequest();
        trinityModelRequest.setInputs(Collections.singletonList(tritonRequest));

        String jsonRequest = objectMapper.writeValueAsString(trinityModelRequest);

        final Request request = new Request.Builder().url(node)
                .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
        log.info("Request URL : {} Question List size {}", node, questions.size());
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {

                processAuditOutputTables.add( ProcessAuditOutputTable.builder()
                        .originId(trinityModelPayload.getOriginId())
                        .stage("VQA_TRANSACTION")
                        .status("COMPLETED")
                        .request(jsonInputRequest)
                        .response(responseBody)
                        .endpoint(node)
                        .message("trinity model completed")
                        .tenantId(tenantId)
                        .rootPipelineId(rootpipelineId)
                        .build());
                insertRowsProcessedIntoProcessAudit(jdbi, processAuditOutputTables, action);
                return responseBody;
            } else {
                log.error("Error in the trinity model response {}", responseBody);
                processAuditOutputTables.add( ProcessAuditOutputTable.builder()
                        .originId(trinityModelPayload.getOriginId())
                        .stage("VQA_TRANSACTION")
                        .status("FAILED")
                        .request(jsonInputRequest)
                        .response(response.message())
                        .endpoint(node)
                        .message("Error in the trinity model response")
                        .tenantId(tenantId)
                        .rootPipelineId(rootpipelineId)
                        .build());
                insertRowsProcessedIntoProcessAudit(jdbi, processAuditOutputTables, action);
                throw new HandymanException(responseBody);
            }
        } catch (Exception e) {
            log.error("Failed to execute the rest api call");
            throw new HandymanException("Failed to execute the rest api call " + node, e);
        }
    }

    @NotNull
    private static TritonRequest getTritonRequestPaperType(String paperType, String modelRegistry, String jsonInputRequest) {
        TritonRequest tritonRequest = new TritonRequest();

        if (Objects.equals(paperType, "Printed")) {
            tritonRequest.setShape(List.of(1, 1));
            tritonRequest.setDatatype("BYTES");
            tritonRequest.setData(Collections.singletonList(jsonInputRequest));
            if (Objects.equals(modelRegistry, "ARGON")) {
                tritonRequest.setName("ARGON VQA START");
            } else if (Objects.equals(modelRegistry, "XENON")) {
                tritonRequest.setName("XENON VQA START");
            }
        } else if (Objects.equals(paperType, "Handwritten")) {
            tritonRequest.setShape(List.of(1, 1));
            tritonRequest.setName("XENON VQA START");
            tritonRequest.setDatatype("BYTES");
            tritonRequest.setData(Collections.singletonList(jsonInputRequest));

        }
        return tritonRequest;
    }

    public String computeCopro(final String inputPath, final String paperType, final List<String> questions, final String modelRegistry, final Long tenantId, ActionExecutionAudit action) throws JsonProcessingException {

        Long actionId = action.getActionId();
        Long rootPipelineId = action.getRootPipelineId();
        final String trinityProcessName = "VQA_VALUATION";
        ObjectMapper objectMapper = new ObjectMapper();


        TrinityModelPayload trinityModelPayload = new TrinityModelPayload();
        trinityModelPayload.setActionId(actionId);
        trinityModelPayload.setProcess(trinityProcessName);
        trinityModelPayload.setRootPipelineId(rootPipelineId);
        trinityModelPayload.setPaperType(paperType);
        trinityModelPayload.setAttributes(questions);
        trinityModelPayload.setInputFilePath(inputPath);
        trinityModelPayload.setModelRegistry(modelRegistry);
        trinityModelPayload.setTenantId(tenantId);


        String jsonInputRequest = objectMapper.writeValueAsString(trinityModelPayload);

        final Request request = new Request.Builder().url(node)
                .post(RequestBody.create(jsonInputRequest, MediaTypeJSON)).build();
        log.info("Request URL : {} Question List size {}", node, questions.size());
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                processAuditOutputTables.add( ProcessAuditOutputTable.builder()
                        .originId(trinityModelPayload.getOriginId())
                        .stage("VQA_TRANSACTION")
                        .status("COMPLETED")
                        .request(jsonInputRequest)
                        .response(responseBody)
                        .endpoint(node)
                        .message("Trinity model completed")
                        .tenantId(tenantId)
                        .rootPipelineId(rootPipelineId)
                        .build());
                insertRowsProcessedIntoProcessAudit(jdbi, processAuditOutputTables, action);
                return responseBody;

            } else {
                log.error("Error in the trinity model response {}", responseBody);
                processAuditOutputTables.add( ProcessAuditOutputTable.builder()
                        .originId(trinityModelPayload.getOriginId())
                        .stage("VQA_TRANSACTION")
                        .status("FAILED")
                        .request(jsonInputRequest)
                        .response(response.message())
                        .endpoint(node)
                        .message("Error in the trinity model response")
                        .tenantId(tenantId)
                        .rootPipelineId(rootPipelineId)
                        .build());
                throw new HandymanException(responseBody);
            }
        } catch (Exception e) {
            log.error("Failed to execute the rest api call");
            throw new HandymanException("Failed to execute the rest api call " + node, e);
        }
    }
    private void insertRowsProcessedIntoProcessAudit(Jdbi jdbi, List<ProcessAuditOutputTable> auditResults, ActionExecutionAudit actionExecutionAudit) throws Exception {
        try {
            jdbi.useTransaction(handle -> {
                System.out.println(auditResults.size());
                for (ProcessAuditOutputTable auditResult : auditResults) {
                    handle.createUpdate("INSERT INTO audit.process_audit_output" +
                                    " (tenant_id, root_pipeline_id, origin_id, created_on, endpoint, request, response, status, stage, message) " +
                                    "VALUES(:tenantId,:rootPipelineId, :originId, now(), :endpoint, :request, :response, :status, :stage, :message)")
                            .bindBean(auditResult)
                            .execute();


                }
            });
        } catch (Exception exception) {
            log.error("Error inserting into batch insert audit {}", ExceptionUtil.toString(exception));
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Error inserting into batch insert audit", handymanException, actionExecutionAudit);
        }
    }
}
