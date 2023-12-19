package in.handyman.raven.lib.model.common;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
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
import java.util.concurrent.atomic.AtomicInteger;


public class IntellimatchConsumerProcess implements CoproProcessor.ConsumerProcess<IntellimatchInputTable,IntellimatchOutputTable> {
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
        AtomicInteger atomicInteger = new AtomicInteger();

        if (result.getActualValue() != null) {
            List<String> sentence = Arrays.asList(result.getExtractedValue());
            final String process = "CONTROL_DATA";
            Long actionId = action.getActionId();
            Long rootpipelineId = result.getRootPipelineId();
            String inputSentence = result.getExtractedValue();
            ObjectMapper objectMapper = new ObjectMapper();


            ComparisonPayload Comparisonpayload = new ComparisonPayload();
            Comparisonpayload.setRootPipelineId(rootpipelineId);
            Comparisonpayload.setActionId(actionId);
            Comparisonpayload.setProcessId(action.getProcessId());
            Comparisonpayload.setOriginId(result.getOriginId());
            Comparisonpayload.setTenantId();
            Comparisonpayload.setProcess(process);
            Comparisonpayload.setInputSentence(inputSentence);
            Comparisonpayload.setSentence(sentence);
            String jsonInputRequest = objectMapper.writeValueAsString(Comparisonpayload);

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
                            status("completed").
                            stage("control data").
                            message("data insertion is completed").
                            build()
                    );
        }
        return parentObj;
    }

        private void coproRequestBuider (IntellimatchInputTable result, Request request, List < IntellimatchOutputTable > parentObj) throws
        IOException {

            try (Response response = httpclient.newCall(request).execute()) {
                log.info("intelliMatch data comparison response body {}", response.body());
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    extractOuputDataRequest(result, parentObj, responseBody, "", "");
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
                            status("completed").
                            stage("control data").
                            message("data insertion is completed").
                            build()
                    );


                }
            }
        }


        private void tritonRequestBuilder (IntellimatchInputTable result, Request request, List < IntellimatchOutputTable > parentObj){
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
                            status("failed").
                            stage("control data").
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
                        status("failed").
                        stage("control data").
                        message("data insertion is failed").
                        build()
                );

                log.error(aMarker, "Exception occurred in copro api for intelliMatch data comparison - {} ", ExceptionUtil.toString(exception));
                HandymanException handymanException = new HandymanException(exception);
                HandymanException.insertException("Paper classification (hw-detection) consumer failed for originId " + originId, handymanException, this.action);
            }
        }


        private static void extractOuputDataRequest (IntellimatchInputTable result, List < IntellimatchOutputTable > parentObj, String comparisonDataItem, String modelName, String modelVersion){

            try {
                List<ComparisonDataItem> comparisonDataItem1 = mapper.readValue(comparisonDataItem, new TypeReference<>() {
                });
                for (ComparisonDataItem item : comparisonDataItem1) {
                    parentObj.add(IntellimatchOutputTable.builder().
                            fileName(result.getFileName()).
                            originId(item.getOriginId()).
                            groupId(item.getGroupId()).
                            createdOn(Timestamp.valueOf(LocalDateTime.now())).
                            rootPipelineId(item.getRootPipelineId()).
                            actualValue(item.getInputSentence()).
                            extractedValue(item.getSentence()).
                            similarity(result.getSimilarity()).
                            confidenceScore(result.getConfidenceScore()).
                            intelliMatch(item.getSimilarityPercent()).
                            status("completed").
                            stage("control data").
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
                        status("failed").
                        stage("control data").
                        message("data insertion is failed").
                        build()
                );
            }
        }
    }


