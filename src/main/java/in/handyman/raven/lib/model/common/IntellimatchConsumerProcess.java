package in.handyman.raven.lib.model.common;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.common.copro.ComparisonDataItemCopro;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class IntellimatchConsumerProcess implements CoproProcessor.ConsumerProcess<IntellimatchInputTable, IntellimatchOutputTable> {
    public static final String CONTROL_DATA_PROCESS_NAME = PipelineName.CONTROL_DATA.getProcessName();
    private final Logger log;
    private final Marker aMarker;
    public final ActionExecutionAudit action;
    private static ObjectMapper mapper = new ObjectMapper();
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";


    private static final MediaType MEDIA_TYPE_JSON = MediaType
            .parse("application/json; charset=utf-8");
    private final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.MINUTES)
            .writeTimeout(100, TimeUnit.MINUTES)
            .readTimeout(100, TimeUnit.MINUTES)
            .build();


    public IntellimatchConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.mapper = new ObjectMapper();
        this.action = action;


    }

    @Override
    public List<IntellimatchOutputTable> process(URL endpoint, IntellimatchInputTable result) throws Exception {
        log.info(aMarker, "coproProcessor consumer process started with endpoint {} and entity {}", endpoint, result);
        List<IntellimatchOutputTable> parentObj = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        if (result.getActualValue() != null) {
            ComparisonPayload comparisonPayload = getComparisonPayload(result);
            String jsonInputRequest = objectMapper.writeValueAsString(comparisonPayload);

            TritonRequest requestBody = new TritonRequest();
            requestBody.setName("COS START");
            requestBody.setShape(List.of(1, 1));
            requestBody.setDatatype("BYTES");
            requestBody.setData(Collections.singletonList(jsonInputRequest));

            TritonInputRequest tritonInputRequest = new TritonInputRequest();
            tritonInputRequest.setInputs(Collections.singletonList(requestBody));

            String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);
            String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);
            if (Objects.equals("false", tritonRequestActivator)) {
                Request request = new Request.Builder().url(endpoint)
                        .post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
                coproRequestBuider(result, request, parentObj);
            } else {
                Request request = new Request.Builder().url(endpoint)
                        .post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();
                tritonRequestBuilder(result, request, parentObj);
            }

            log.info(aMarker, "coproProcessor consumer process with empty actual value entity {}", result);

        } else {
            parentObj.add(IntellimatchOutputTable.builder().
                    fileName(result.getFileName()).
                    originId(result.getOriginId()).
                    groupId(result.getGroupId()).
                    createdOn(Timestamp.valueOf(LocalDateTime.now())).
                    rootPipelineId(result.getRootPipelineId()).
                    actualValue(result.getActualValue()).
                    extractedValue(result.getExtractedValue()).
                    similarity(result.getSimilarity()).
                    intelliMatch(0.0000).
                    status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).
                    stage(CONTROL_DATA_PROCESS_NAME).
                    message("data insertion is completed").
                    build()
            );
        }
        return parentObj;
    }

    @NotNull
    private ComparisonPayload getComparisonPayload(IntellimatchInputTable result) {
        List<String> sentence = Collections.singletonList(result.getExtractedValue());
        final String process = "CONTROL_DATA";
        Long actionId = action.getActionId();
        Long rootpipelineId = result.getRootPipelineId();
        String inputSentence = result.getActualValue();


        ComparisonPayload comparisonPayload = new ComparisonPayload();
        comparisonPayload.setRootPipelineId(rootpipelineId);
        comparisonPayload.setActionId(actionId);
        comparisonPayload.setProcessId(action.getProcessId());
        comparisonPayload.setOriginId(result.getOriginId());
        comparisonPayload.setProcess(process);
        comparisonPayload.setInputSentence(inputSentence);
        comparisonPayload.setSentences(sentence);
        comparisonPayload.setGroupId(result.getGroupId());
        comparisonPayload.setTenantId(result.getTenantId());
        return comparisonPayload;
    }

    private void coproRequestBuider(IntellimatchInputTable result, Request request, List<IntellimatchOutputTable> parentObj) throws
            IOException {

        try (Response response = httpclient.newCall(request).execute()) {
            log.info("intelliMatch data comparison response body {}", response.body());
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                extractedCoproOutputResponse(result, parentObj, responseBody, "", "");
            } else {
                parentObj.add(IntellimatchOutputTable.builder().
                        fileName(result.getFileName()).
                        originId(result.getOriginId()).
                        groupId(result.getGroupId()).
                        createdOn(Timestamp.valueOf(LocalDateTime.now())).
                        rootPipelineId(result.getRootPipelineId()).
                        actualValue(result.getActualValue()).
                        extractedValue(result.getExtractedValue()).
                        similarity(result.getSimilarity()).
                        intelliMatch(0.0000).
                        status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).
                        stage(CONTROL_DATA_PROCESS_NAME).
                        message("data insertion is completed").
                        build()
                );


            }
        }
    }


    private void tritonRequestBuilder(IntellimatchInputTable result, Request request, List<IntellimatchOutputTable> parentObj) {
        String originId = result.getOriginId();
        try (Response response = httpclient.newCall(request).execute()) {
            log.info("intelliMatch data comparison response body {}", response.body());
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                ObjectMapper objectMappers = new ObjectMapper();
                ComparisonResponse Response = objectMappers.readValue(responseBody, ComparisonResponse.class);
                if (Response.getOutputs() != null && !Response.getOutputs().isEmpty()) {
                    Response.getOutputs().forEach(o -> {
                        o.getData().forEach(comparisonDataItem -> {
                            extractOuputDataRequest(result, parentObj, comparisonDataItem, Response.getModelName(), Response.getModelVersion());
                        });
                    });
                }
            } else {
                parentObj.add(IntellimatchOutputTable.builder().
                        fileName(result.getFileName()).
                        originId(result.getOriginId()).
                        groupId(result.getGroupId()).
                        createdOn(Timestamp.valueOf(LocalDateTime.now())).
                        rootPipelineId(result.getRootPipelineId()).
                        actualValue(result.getActualValue()).
                        extractedValue(result.getExtractedValue()).
                        similarity(result.getSimilarity()).
                        intelliMatch(0.00).
                        status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).
                        stage(CONTROL_DATA_PROCESS_NAME).
                        message("data insertion is failed").
                        build()
                );
                log.error(aMarker, "The Exception occurred in intelliMatch data comparison by {} ", response);
                throw new HandymanException(responseBody);
            }
        } catch (Exception exception) {
            parentObj.add(IntellimatchOutputTable.builder().
                    fileName(result.getFileName()).
                    originId(result.getOriginId()).
                    groupId(result.getGroupId()).
                    createdOn(Timestamp.valueOf(LocalDateTime.now())).
                    rootPipelineId(result.getRootPipelineId()).
                    actualValue(result.getActualValue()).
                    extractedValue(result.getExtractedValue()).
                    similarity(result.getSimilarity()).
                    intelliMatch(0.00).
                    status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).
                    stage(CONTROL_DATA_PROCESS_NAME).
                    message("data insertion is failed").
                    build()
            );

            log.error(aMarker, "Exception occurred in copro api for intelliMatch data comparison - {} ", ExceptionUtil.toString(exception));
            throw new HandymanException("Paper classification (hw-detection) consumer failed for originId " + originId, exception);
        }
    }


    private static void extractOuputDataRequest(IntellimatchInputTable result, List<IntellimatchOutputTable> parentObj, String comparisonDataItem, String modelName, String modelVersion) {

        try {
            List<ComparisonDataItem> comparisonDataItem1 = mapper.readValue(comparisonDataItem, new TypeReference<>() {
            });
            for (ComparisonDataItem item : comparisonDataItem1) {

                // Add square brackets to make it a valid JSON array
                String sentenceJson = formatToJsonArray(item.getSentence());

                JsonNode sentenceNode = mapper.readTree(sentenceJson);

                // Assuming the JSON array contains strings
                if (sentenceNode.isArray()) {
                    for (JsonNode wordNode : sentenceNode) {
                        String word = wordNode.asText().trim();
                        parentObj.add(IntellimatchOutputTable.builder().
                                fileName(result.getFileName()).
                                originId(item.getOriginId()).
                                groupId(item.getGroupId()).
                                createdOn(Timestamp.valueOf(LocalDateTime.now())).
                                rootPipelineId(item.getRootPipelineId()).
                                actualValue(item.getInputSentence()).
                                extractedValue(word.trim()).
                                similarity(result.getSimilarity()).
                                confidenceScore(result.getConfidenceScore()).
                                intelliMatch(item.getSimilarityPercent()).
                                status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).
                                stage(CONTROL_DATA_PROCESS_NAME).
                                message("data insertion is completed").
                                modelName(modelName).
                                modelVersion(modelVersion).
                                build());
                    }
                }

            }
        } catch (Exception exception) {
            parentObj.add(IntellimatchOutputTable.builder().
                    fileName(result.getFileName()).
                    originId(result.getOriginId()).
                    groupId(result.getGroupId()).
                    createdOn(Timestamp.valueOf(LocalDateTime.now())).
                    rootPipelineId(result.getRootPipelineId()).
                    actualValue(result.getActualValue()).
                    extractedValue(result.getExtractedValue()).
                    similarity(result.getSimilarity()).
                    intelliMatch(0.00).
                    status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).
                    stage(CONTROL_DATA_PROCESS_NAME).
                    message("data insertion is failed").
                    build()
            );
        }
    }

    private static void extractedCoproOutputResponse(IntellimatchInputTable result, List<IntellimatchOutputTable> parentObj, String comparisonDataItem, String modelName, String
            modelVersion) {

        try {
            List<ComparisonDataItemCopro> comparisonDataItemCopros = mapper.readValue(
                    comparisonDataItem, new TypeReference<List<ComparisonDataItemCopro>>() {
                    });

            for (ComparisonDataItemCopro itemCopro : comparisonDataItemCopros) {
                parentObj.add(IntellimatchOutputTable.builder().
                        fileName(result.getFileName()).
                        createdOn(Timestamp.valueOf(LocalDateTime.now())).
                        extractedValue(itemCopro.getSentence()).
                        similarity(result.getSimilarity()).
                        confidenceScore(result.getConfidenceScore()).
                        intelliMatch(itemCopro.getSimilarityPercent()).
                        status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).
                        stage(CONTROL_DATA_PROCESS_NAME).
                        message("data insertion is completed").
                        modelName(modelName).
                        modelVersion(modelVersion).
                        build());
            }


        } catch (Exception exception) {
            parentObj.add(IntellimatchOutputTable.builder().
                    fileName(result.getFileName()).
                    originId(result.getOriginId()).
                    groupId(result.getGroupId()).
                    createdOn(Timestamp.valueOf(LocalDateTime.now())).
                    rootPipelineId(result.getRootPipelineId()).
                    actualValue(result.getActualValue()).
                    extractedValue(result.getExtractedValue()).
                    similarity(result.getSimilarity()).
                    intelliMatch(0.00).
                    status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).
                    stage(CONTROL_DATA_PROCESS_NAME).
                    message("data insertion is failed").
                    build()
            );
        }
    }


    private static String formatToJsonArray(String input) {
        // Remove enclosing square brackets and escape backslashes
        input = input.substring(1, input.length() - 1).replace("\\", "");

        // Split the input into words
        String[] words = input.split("\",\"");

        // Enclose each word in double quotes
        for (int i = 0; i < words.length; i++) {
            words[i] = "\"" + words[i].trim() + "\"";
        }

        // Join the words with commas to form a JSON array
        return "[" + String.join(",", words) + "]";
    }
}
