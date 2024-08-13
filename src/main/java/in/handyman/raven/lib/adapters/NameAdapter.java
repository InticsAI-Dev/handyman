package in.handyman.raven.lib.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.interfaces.AdapterInterface;
import in.handyman.raven.lib.model.neradaptors.NerAdapterDataItem;
import in.handyman.raven.lib.model.neradaptors.NerAdapterPayload;
import in.handyman.raven.lib.model.neradaptors.NerAdapterResponse;
import in.handyman.raven.lib.model.neradaptors.copro.NerAdapterDataItemCopro;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class NameAdapter implements AdapterInterface {


    private boolean isPredictedLabel = false;
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";

    @Override
    public boolean getValidationModel(String sentence, String uri, ActionExecutionAudit audit) throws Exception {

        try {


            MediaType mediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
            String[] patientName = new String[]{sentence};
            final String trinityProcessName = "VQA_VALUATION";
            final String tritonRequestActivator = audit.getContext().get(TRITON_REQUEST_ACTIVATOR);
            OkHttpClient httpclient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.MINUTES)
                    .writeTimeout(10, TimeUnit.MINUTES)
                    .readTimeout(10, TimeUnit.MINUTES).build();

            Long rootPipelineId = audit.getRootPipelineId();
            Long actionId = audit.getActionId();

            String process = "NER";
            ObjectMapper objectMapper = new ObjectMapper();

            //payload
            NerAdapterPayload nerAdapterPayload = new NerAdapterPayload();
            nerAdapterPayload.setRootPipelineId(rootPipelineId);
            nerAdapterPayload.setProcess(process);
            nerAdapterPayload.setActionId(actionId);
            nerAdapterPayload.setInputString(List.of(patientName));
            nerAdapterPayload.setProcessId(audit.getProcessId());


            String jsonInputRequest = objectMapper.writeValueAsString(nerAdapterPayload);

            TritonRequest requestBody = new TritonRequest();
            requestBody.setName("NER START");
            requestBody.setShape(List.of(1, 1));
            requestBody.setDatatype("BYTES");
            requestBody.setData(Collections.singletonList(jsonInputRequest));

            TritonInputRequest tritonInputRequest = new TritonInputRequest();
            tritonInputRequest.setInputs(Collections.singletonList(requestBody));

            String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

            if (Objects.equals("false", tritonRequestActivator)) {
                Request request = new Request.Builder().url(uri)
                        .post(RequestBody.create(jsonInputRequest, mediaTypeJSON)).build();
                extractedCoproOutput(httpclient, objectMapper, request);
            } else {
                Request request = new Request.Builder().url(uri)
                        .post(RequestBody.create(jsonRequest, mediaTypeJSON)).build();
                extractedTritonOutput(httpclient, objectMapper, request);
            }


        } catch (Exception ex) {
            throw new HandymanException("Failed to execute", ex);
        }
        return isPredictedLabel;
    }

    private void extractedTritonOutput(OkHttpClient httpclient, ObjectMapper objectMapper, Request request) throws IOException {
        try (Response response = httpclient.newCall(request).execute()) {

            if (response.isSuccessful()) {
                NerAdapterResponse nerAdapterResponse = objectMapper.readValue(response.body().string(), NerAdapterResponse.class);
                nerAdapterResponse.getOutputs().forEach(nerAdapterOutput -> {
                    nerAdapterOutput.getData().forEach(nerAdapterDataItem -> {
                        try {
                            NerAdapterDataItem nerAdapterDataItem1 = objectMapper.readValue(nerAdapterDataItem, new TypeReference<>() {
                            });
                            nerAdapterDataItem1.getPrediction().forEach(nerAdapterPrediction -> {
                                isPredictedLabel = nerAdapterPrediction.isPredictedLabel();
                            });

                        } catch (JsonProcessingException e) {
                            throw new HandymanException("Exception in handling the response body of NER adapter", e);
                        }
                    });
                });
            }
        }
    }

    private void extractedCoproOutput(OkHttpClient httpclient, ObjectMapper objectMapper, Request request) throws IOException {
        try (Response response = httpclient.newCall(request).execute()) {

            if (response.isSuccessful()) {
                try {
                    if (response.body() != null) {
                        NerAdapterDataItemCopro nerAdapterDataItem1 = objectMapper.readValue(response.body().string(), new TypeReference<>() {
                        });
                        nerAdapterDataItem1.getPrediction().forEach(nerAdapterPrediction -> {
                            isPredictedLabel = nerAdapterPrediction.isPredictedLabel();
                        });
                    }

                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }


        }
    }

    @Override
    public boolean getDateValidationModel(String sentence, int comparableYear, String[] dateFormats) throws Exception {
        return false;
    }

    @Override
    public int getThresholdScore(String sentence) throws Exception {
        return 0;
    }
}
