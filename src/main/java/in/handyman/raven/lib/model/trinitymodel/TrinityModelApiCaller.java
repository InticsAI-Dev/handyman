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
    public static final String XENON_VQA_START = "XENON VQA START";
    public static final String ARGON_VQA_START = "ARGON VQA START";
    public static final String KRYPTON_VQA_START = "KRYPTON VQA START";
    public static final String BORON_VQA_START = "BORON VQA START";
    public static final String VQA_VALUATION = "VQA_VALUATION";
    private final TrinityModelAction aAction;
    private final OkHttpClient httpclient;
    private final Logger log;

    private final String node;

    public TrinityModelApiCaller(TrinityModelAction aAction, final String node, final Logger log) {
        this.aAction = aAction;
        this.node = node;
        this.log = log;
        this.httpclient = new OkHttpClient.Builder()
                .connectTimeout(Long.parseLong(aAction.getHttpClientTimeout()), TimeUnit.MINUTES)
                .writeTimeout(Long.parseLong(aAction.getHttpClientTimeout()), TimeUnit.MINUTES)
                .readTimeout(Long.parseLong(aAction.getHttpClientTimeout()), TimeUnit.MINUTES)
                .build();
    }

    public String computeTriton(final String inputPath, final String paperType, final List<String> questions, final String modelRegistry, final Long tenantId, ActionExecutionAudit action) throws JsonProcessingException {

        Long actionId = action.getActionId();
        Long rootPipelineId = action.getRootPipelineId();
        ObjectMapper objectMapper = new ObjectMapper();


        TrinityModelPayload trinityModelPayload = new TrinityModelPayload();
        trinityModelPayload.setActionId(actionId);
        trinityModelPayload.setProcess(VQA_VALUATION);
        trinityModelPayload.setRootPipelineId(rootPipelineId);
        trinityModelPayload.setPaperType(paperType);
        trinityModelPayload.setTenantId(tenantId);
        trinityModelPayload.setAttributes(questions);
        trinityModelPayload.setInputFilePath(inputPath);
        trinityModelPayload.setModelRegistry(modelRegistry);
        trinityModelPayload.setTemplateVersion("vqa");


        String jsonInputRequest = objectMapper.writeValueAsString(trinityModelPayload);
        TritonRequest tritonRequest;

        if (Objects.equals(action.getContext().get("sor.transaction.filter.by.paper.type"), "true")) {
            tritonRequest = getTritonRequestByPaperType(paperType, modelRegistry, jsonInputRequest);
        } else {
            tritonRequest = getTritonRequestByModelRegistry(modelRegistry, jsonInputRequest);
        }


        TrinityModelRequest trinityModelRequest = new TrinityModelRequest();
        trinityModelRequest.setInputs(Collections.singletonList(tritonRequest));

        String jsonRequest = objectMapper.writeValueAsString(trinityModelRequest);

        final Request request = new Request.Builder().url(node)
                .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
        log.info("Triton Request URL : {} Question List size {}", node, questions.size());
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {


                return responseBody;
            } else {
                log.error("Error in the Triton trinity model response {}", responseBody);
                throw new HandymanException(responseBody);
            }
        } catch (Exception e) {
            log.error("Failed to execute the Triton rest api call {1}" , e);
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
                tritonRequest.setName(ARGON_VQA_START);
                log.info("Triton request set api call based on paper type : "+paperType+"api request: "+ARGON_VQA_START);
            } else if (Objects.equals(modelRegistry, ModelRegistry.XENON.name())) {
                tritonRequest.setName(XENON_VQA_START);
                log.info("Triton request set api call based on paper type : "+paperType+"api request: "+XENON_VQA_START);
            }
        } else if (Objects.equals(paperType, "Handwritten")) {
            tritonRequest.setShape(List.of(1, 1));
            tritonRequest.setName(XENON_VQA_START);
            tritonRequest.setDatatype(TritonDataTypes.BYTES.name());
            tritonRequest.setData(Collections.singletonList(jsonInputRequest));
            log.info("Triton request set api call based on paper type : "+paperType+"api request: "+XENON_VQA_START);

        }
        return tritonRequest;
    }

    @NotNull
    private TritonRequest getTritonRequestByModelRegistry(String modelRegistry, String jsonInputRequest) {
        TritonRequest tritonRequest = new TritonRequest();
        tritonRequest.setShape(List.of(1, 1));
        tritonRequest.setDatatype(TritonDataTypes.BYTES.name());
        tritonRequest.setData(Collections.singletonList(jsonInputRequest));

        if (Objects.equals(modelRegistry, ModelRegistry.ARGON.name())) {
            tritonRequest.setName(ARGON_VQA_START);
            log.info("Triton request set api call based on model registry : "+modelRegistry+"api request: "+ARGON_VQA_START);
        } else if (Objects.equals(modelRegistry, ModelRegistry.XENON.name())) {
            tritonRequest.setName(XENON_VQA_START);
            log.info("Triton request set api call based on model registry : "+modelRegistry+"api request: "+XENON_VQA_START);
        } else if (Objects.equals(modelRegistry, ModelRegistry.KRYPTON.name())) {
            tritonRequest.setName(KRYPTON_VQA_START);
            log.info("Triton request set api call based on model registry : "+modelRegistry+"api request: "+KRYPTON_VQA_START);
        } else if (Objects.equals(modelRegistry, ModelRegistry.BORON.name())) {
            tritonRequest.setName(BORON_VQA_START);
            log.info("Triton request set api call based on model registry : "+modelRegistry+"api request: "+BORON_VQA_START);
        }
        return tritonRequest;
    }

    public String computeCopro(final String inputPath, final String paperType, final List<String> questions, final String modelRegistry, final Long tenantId, ActionExecutionAudit action) throws JsonProcessingException {

        Long actionId = action.getActionId();
        Long rootPipelineId = action.getRootPipelineId();
        ObjectMapper objectMapper = new ObjectMapper();


        TrinityModelPayload trinityModelPayload = new TrinityModelPayload();
        trinityModelPayload.setActionId(actionId);
        trinityModelPayload.setProcess(VQA_VALUATION);
        trinityModelPayload.setRootPipelineId(rootPipelineId);
        trinityModelPayload.setPaperType(paperType);
        trinityModelPayload.setAttributes(questions);
        trinityModelPayload.setInputFilePath(inputPath);
        trinityModelPayload.setModelRegistry(modelRegistry);
        trinityModelPayload.setTenantId(tenantId);


        String jsonInputRequest = objectMapper.writeValueAsString(trinityModelPayload);

        final Request request = new Request.Builder().url(node)
                .post(RequestBody.create(jsonInputRequest, MediaTypeJSON)).build();
        log.info("Copro Request URL : {} Question List size {}", node, questions.size());
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
