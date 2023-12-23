package in.handyman.raven.lib.model.trinitymodel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.TrinityModelAction;
import in.handyman.raven.lib.model.triton.TritonRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

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


    private final String node;

    public TrinityModelApiCaller(TrinityModelAction aAction, final String node) {
        this.aAction = aAction;
        this.node = node;
        this.httpclient = new OkHttpClient.Builder()
                .connectTimeout(Long.parseLong(aAction.getHttpClientTimeout()), TimeUnit.MINUTES)
                .writeTimeout(Long.parseLong(aAction.getHttpClientTimeout()), TimeUnit.MINUTES)
                .readTimeout(Long.parseLong(aAction.getHttpClientTimeout()), TimeUnit.MINUTES)
                .build();
    }

    public String computeTriton(final String inputPath, final String paperType, final List<String> questions, ActionExecutionAudit action) throws JsonProcessingException {

        Long actionId = action.getActionId();
        Long rootpipelineId = action.getRootPipelineId();
        final String trinityProcessName = "VQA_VALUATION";
        ObjectMapper objectMapper = new ObjectMapper();


        TrinityModelPayload trinityModelPayload = new TrinityModelPayload();
        trinityModelPayload.setActionId(actionId);
        trinityModelPayload.setProcess(trinityProcessName);
        trinityModelPayload.setRootPipelineId(rootpipelineId);
        trinityModelPayload.setPaperType(paperType);
        trinityModelPayload.setAttributes(questions);
        trinityModelPayload.setInputFilePath(inputPath);

        String jsonInputRequest = objectMapper.writeValueAsString(trinityModelPayload);

        TritonRequest tritonRequest = getTritonRequestPaperType(paperType, jsonInputRequest);


        TrinityModelRequest trinityModelRequest = new TrinityModelRequest();
        trinityModelRequest.setInputs(Collections.singletonList(tritonRequest));

        String jsonRequest = objectMapper.writeValueAsString(trinityModelRequest);

        final Request request = new Request.Builder().url(node)
                .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
        log.info("Request URL : {} Question List size {}", node, questions.size());
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {


                return responseBody;
            } else {
                log.error("Error in the trinity model response {}", responseBody);
                throw new HandymanException(responseBody);
            }
        } catch (Exception e) {
            log.error("Failed to execute the rest api call");
            throw new HandymanException("Failed to execute the rest api call " + node, e);
        }
    }

    @NotNull
    private static TritonRequest getTritonRequestPaperType(String paperType, String jsonInputRequest) {
        TritonRequest tritonRequest = new TritonRequest();

        if(Objects.equals(paperType,"Printed")){
            tritonRequest.setShape(List.of(1, 1));
            tritonRequest.setName("ARGON VQA START");
            tritonRequest.setDatatype("BYTES");
            tritonRequest.setData(Collections.singletonList(jsonInputRequest));
        } else if (Objects.equals(paperType,"Handwritten")) {
            tritonRequest.setShape(List.of(1, 1));
            tritonRequest.setName("XENON VQA START");
            tritonRequest.setDatatype("BYTES");
            tritonRequest.setData(Collections.singletonList(jsonInputRequest));

        }
        return tritonRequest;
    }

    public String computeCopro(final String inputPath, final String paperType, final List<String> questions, ActionExecutionAudit action) throws JsonProcessingException {

        Long actionId = action.getActionId();
        Long rootpipelineId = action.getRootPipelineId();
        final String trinityProcessName = "VQA_VALUATION";
        ObjectMapper objectMapper = new ObjectMapper();


        TrinityModelPayload trinityModelPayload = new TrinityModelPayload();
        trinityModelPayload.setActionId(actionId);
        trinityModelPayload.setProcess(trinityProcessName);
        trinityModelPayload.setRootPipelineId(rootpipelineId);
        trinityModelPayload.setPaperType(paperType);
        trinityModelPayload.setAttributes(questions);
        trinityModelPayload.setInputFilePath(inputPath);


        String jsonInputRequest = objectMapper.writeValueAsString(trinityModelPayload);

        final Request request = new Request.Builder().url(node)
                .post(RequestBody.create(jsonInputRequest, MediaTypeJSON)).build();
        log.info("Request URL : {} Question List size {}", node, questions.size());
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {


                return responseBody;
            } else {
                log.error("Error in the trinity model response {}", responseBody);
                throw new HandymanException(responseBody);
            }
        } catch (Exception e) {
            log.error("Failed to execute the rest api call");
            throw new HandymanException("Failed to execute the rest api call " + node, e);
        }
    }


}
