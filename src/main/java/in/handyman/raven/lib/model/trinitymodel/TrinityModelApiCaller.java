package in.handyman.raven.lib.model.trinitymodel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.TrinityModelAction;
import in.handyman.raven.lib.model.triton.ModelRegistry;
import in.handyman.raven.lib.model.triton.TritonDataTypes;
import in.handyman.raven.lib.model.triton.TritonRequest;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class TrinityModelApiCaller {

    private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
    private final TrinityModelAction aAction;
    private final OkHttpClient httpclient;
    private final Logger log;
    private final String node;
    public final ActionExecutionAudit action;


    public TrinityModelApiCaller(TrinityModelAction aAction, final String node, final Logger log, ActionExecutionAudit action) {
        this.aAction = aAction;
        this.node = node;
        this.log = log;
        this.httpclient = new OkHttpClient.Builder()
                .connectTimeout(Long.parseLong(aAction.getHttpClientTimeout()), TimeUnit.MINUTES)
                .writeTimeout(Long.parseLong(aAction.getHttpClientTimeout()), TimeUnit.MINUTES)
                .readTimeout(Long.parseLong(aAction.getHttpClientTimeout()), TimeUnit.MINUTES)
                .build();

        this.action = action;
    }

    public String computeTriton(TrinityModelLineItem asset, ActionExecutionAudit action) throws JsonProcessingException {

        Long actionId = action.getActionId();
        Long rootpipelineId = action.getRootPipelineId();
        final String trinityProcessName = "VQA_VALUATION";
        ObjectMapper objectMapper = new ObjectMapper();


        TrinityModelPayload trinityModelPayload = new TrinityModelPayload();
        trinityModelPayload.setActionId(actionId);
        trinityModelPayload.setProcess(trinityProcessName);
        trinityModelPayload.setRootPipelineId(rootpipelineId);
        trinityModelPayload.setPaperType(asset.getPaperType());
        trinityModelPayload.setTenantId(asset.getTenantId());
        trinityModelPayload.setAttributes(asset.getAttributes());
        trinityModelPayload.setInputFilePath(asset.getFilePath());
        trinityModelPayload.setModelRegistry(asset.getModelRegistry());
        trinityModelPayload.setOriginId(asset.getOriginId());
        trinityModelPayload.setGroupId(asset.getGroupId());
        trinityModelPayload.setPaperNo(asset.getPaperNo());
        trinityModelPayload.setProcessId(asset.getProcessId());
        trinityModelPayload.setQnCategory(asset.getQnCategory());
        trinityModelPayload.setModelRegistryId(asset.getModelRegistryId());

        String jsonInputRequest = objectMapper.writeValueAsString(trinityModelPayload);
        TritonRequest tritonRequest;

        if (Objects.equals(action.getContext().get("sor.transaction.filter.by.paper.type"), "true")) {
            tritonRequest = getTritonRequestByPaperType(asset.getPaperType(), asset.getModelRegistry(), jsonInputRequest);
        } else {
            tritonRequest = getTritonRequestByModelRegistry(asset.getModelRegistry(), jsonInputRequest);
        }


        TrinityModelRequest trinityModelRequest = new TrinityModelRequest();
        trinityModelRequest.setInputs(Collections.singletonList(tritonRequest));

        String jsonRequest = objectMapper.writeValueAsString(trinityModelRequest);

        final Request request = new Request.Builder().url(node)
                .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
        log.info("Triton Request URL : {} Question List size {}", node, asset.getAttributes().size());
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {


                return responseBody;
            } else {
                log.error("Error in the Triton trinity model response {}", responseBody);
                throw new HandymanException(responseBody);
            }
        } catch (Exception e) {
            log.error("Failed to execute the Triton rest api call {1}", e);
            throw new HandymanException("Failed to execute the Copro rest api call " + node, e);
        }
    }

    @NotNull
    private TritonRequest getTritonRequestByPaperType(String paperType, String modelRegistry, String jsonInputRequest) {
        TritonRequest tritonRequest = new TritonRequest();

        if (Objects.equals(paperType, "Printed")) {
            tritonRequest.setShape(List.of(1, 1));
            tritonRequest.setDatatype(TritonDataTypes.BYTES.name());
            tritonRequest.setData(Collections.singletonList(jsonInputRequest));
            if (Objects.equals(modelRegistry, ModelRegistry.ARGON.name())) {
                tritonRequest.setName(action.getContext().get("ARGON.VQA.START"));

            } else if (Objects.equals(modelRegistry, ModelRegistry.XENON.name())) {
                tritonRequest.setName(action.getContext().get("XENON.VQA.START"));

            }
        } else if (Objects.equals(paperType, "Handwritten")) {
            tritonRequest.setShape(List.of(1, 1));
            tritonRequest.setName(action.getContext().get("XENON.VQA.START"));
            tritonRequest.setDatatype(TritonDataTypes.BYTES.name());
            tritonRequest.setData(Collections.singletonList(jsonInputRequest));


        }
        log.info("Triton request set api call based on paper type : {} api request: {} ", paperType, tritonRequest.getName());
        return tritonRequest;
    }

    @NotNull
    private TritonRequest getTritonRequestByModelRegistry(String modelRegistry, String jsonInputRequest) {
        TritonRequest tritonRequest = new TritonRequest();
        tritonRequest.setShape(List.of(1, 1));
        tritonRequest.setDatatype(TritonDataTypes.BYTES.name());
        tritonRequest.setData(Collections.singletonList(jsonInputRequest));

        if (Objects.equals(modelRegistry, ModelRegistry.ARGON.name())) {
            tritonRequest.setName(action.getContext().get("ARGON.VQA.START"));

        } else if (Objects.equals(modelRegistry, ModelRegistry.XENON.name())) {
            tritonRequest.setName(action.getContext().get("XENON.VQA.START"));

        } else if (Objects.equals(modelRegistry, ModelRegistry.KRYPTON.name())) {
            tritonRequest.setName(action.getContext().get("KRYPTON.MODEL.START"));

        } else if (Objects.equals(modelRegistry, ModelRegistry.BORON.name())) {
            tritonRequest.setName(action.getContext().get("BORON.VQA.START"));

        }
        log.info("Triton request set api call based on model registry : {} api request: {} ", modelRegistry, tritonRequest.getName());
        return tritonRequest;
    }

    public String computeCopro(TrinityModelLineItem asset, ActionExecutionAudit action) throws JsonProcessingException {

        Long actionId = action.getActionId();
        Long rootPipelineId = action.getRootPipelineId();
        ObjectMapper objectMapper = new ObjectMapper();


        TrinityModelPayload trinityModelPayload = new TrinityModelPayload();
        trinityModelPayload.setActionId(actionId);
        trinityModelPayload.setProcess(action.getContext().get("VQA.VALUATION"));
        trinityModelPayload.setRootPipelineId(rootPipelineId);
        trinityModelPayload.setPaperType(asset.getPaperType());
        trinityModelPayload.setAttributes(asset.getAttributes());
        trinityModelPayload.setInputFilePath(asset.getFilePath());
        trinityModelPayload.setModelRegistry(asset.getModelRegistry());
        trinityModelPayload.setTenantId(asset.getTenantId());
        trinityModelPayload.setOriginId(asset.getOriginId());
        trinityModelPayload.setGroupId(asset.getGroupId());
        trinityModelPayload.setPaperNo(asset.getPaperNo());
        trinityModelPayload.setProcessId(asset.getProcessId());
        trinityModelPayload.setQnCategory(asset.getQnCategory());
        trinityModelPayload.setModelRegistryId(asset.getModelRegistryId());



        String jsonInputRequest = objectMapper.writeValueAsString(trinityModelPayload);

        final Request request = new Request.Builder().url(node)
                .post(RequestBody.create(jsonInputRequest, MediaTypeJSON)).build();
        log.info("Copro Request URL : {} Question List size {}", node, asset.getAttributes());
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {


                return responseBody;
            } else {
                log.error("Error in the Copro trinity model response {}", responseBody);
                throw new HandymanException(responseBody);
            }
        } catch (Exception e) {
            log.error("Failed to execute the Copro rest api call");
            throw new HandymanException("Failed to execute the Copro rest api call " + node, e);
        }
    }


}
