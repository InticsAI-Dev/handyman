package in.handyman.raven.lib.model.integratedNoiseModel;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;

import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class NoiseModelConsumerProcess implements CoproProcessor.ConsumerProcess<NoiseModelInputEntity,NoiseModelOutputEnitity>{
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";

    private final Logger log;
    private final Marker aMarker;

    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    public static String httpClientTimeout = new String();
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public NoiseModelConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.httpClientTimeout = action.getContext().get("okhttp.client.timeout");
    }





    @Override
    public List<NoiseModelOutputEnitity> process(URL endpoint, NoiseModelInputEntity entity) throws Exception {
        log.info("copro consumer process started");
       List<NoiseModelOutputEnitity> noiseOutputEntities = new ArrayList<>();
       // List<NoiseModelOutputEnitity> noiseOutputEntity = new ArrayList<>();
        final String filePath = entity.getFilePath();
        final String noiseDetectionModel = "NOISE_DETECTION_MODEL";
        final Long rootPipelineId = action.getRootPipelineId();
        final Long actionId = Long.valueOf(action.getContext().get("actionId"));

        ObjectMapper objectMapper = new ObjectMapper();
        //payload
        NoiseModelData NoiseModelData = new NoiseModelData();
        NoiseModelData.setRootPipelineId(1L);
        NoiseModelData.setProcess(noiseDetectionModel);
        NoiseModelData.setInputFilePath(filePath);
        NoiseModelData.setActionId(actionId);
        NoiseModelData.setProcessId(action.getProcessId());
        NoiseModelData.setOriginId(entity.getOriginId());
        NoiseModelData.setPaperNo(entity.getPaperNo());
        NoiseModelData.setGroupId(entity.getGroupId());
        NoiseModelData.setOutputDir(entity.getOutputDir());
        NoiseModelData.setTenantId(entity.getTenantId());
        final String jsonInputRequest = objectMapper.writeValueAsString(NoiseModelData);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("NOISE-DETECTION");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        final String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        if (log.isInfoEnabled()) {
            log.info("input object node in the consumer process  inputFilePath {}", filePath);
        }
        final String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);

        final Request Requests = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
        if (Objects.equals("false", tritonRequestActivator)) {
            final Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonInputRequest, MediaTypeJSON)).build();
            coproRequestBuilder(entity, request,  objectMapper, rootPipelineId,noiseOutputEntities);
        }
        else {
            final Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
            tritonRequestBuilder(entity, request, objectMapper,  rootPipelineId,noiseOutputEntities);
        }

        if (log.isInfoEnabled()) {
            log.info("input object node in the consumer process coproURL {}, inputFilePath {}", endpoint, filePath);
        }
        return noiseOutputEntities;
    }


    private void tritonRequestBuilder(NoiseModelInputEntity entity, Request request, ObjectMapper objectMapper,Long rootPipelineId ,List<NoiseModelOutputEnitity> noiseOutputEntities) throws IOException {
        final String originId = entity.getOriginId();
        final Integer paperNo = entity.getPaperNo();
        final Integer groupId = entity.getGroupId();
        final String filePath = entity.getFilePath();

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                log.info("Response Details: {}", response);

                final String responseBody = Objects.requireNonNull(response.body()).string();
                // Parse the JSON string
                final JsonNode dataNode = objectMapper.readTree(responseBody);

                if (dataNode != null && !dataNode.isEmpty()) {
                    final JsonNode rootNode = dataNode
                            .path("outputs")
                            .path(0)
                            .path("data")
                            .path(0);
                    final Integer processId = rootNode.path("processId").asInt();
//                    Integer groupId = rootNode.path("groupId").asInt();
                    final Integer tenantId = rootNode.path("tenantId").asInt();
                    final String inputFilePath = rootNode.path("inputFilePath").asText();
                    final String consolidatedClass = rootNode.path("consolidatedClass").asText();
                    final Double consolidatedConfidenceScore = rootNode.path("consolidatedConfidenceScore").asDouble();
                    final String extractedValue = rootNode.path("noiseModelsResult").toString();

                    //              getting the output of hwnoise model

                    final String hwClass = rootNode
                            .path("noiseModelsResult")
                            .path("hwNoiseDetectionOutput").toString();


                    final String checkBoxClass = rootNode
                            .path("noiseModelsResult")
                            .path("checkNoiseDetectionOutput").toString();
                    final String tickNoiseClass = rootNode
                            .path("noiseModelsResult")
                            .path("tickNoiseDetectionOutput").toString();
                    final String speckleClass = rootNode
                            .path("noiseModelsResult")
                            .path("speckleNoiseDetection").toString();

                    noiseOutputEntities.add(NoiseModelOutputEnitity.builder()
                            .originId(originId)
                            .paperNo(paperNo)
                            .groupId(groupId)
                            .processId(processId)
                            .tenantId(tenantId)
                            .inputFilePath(filePath)
                            .consolidatedConfidenceScore(consolidatedConfidenceScore)
                            .consolidatedClass(consolidatedClass)
                            .extractedValue(extractedValue)
                            .model1NoiseDetectionOutput(hwClass)
                            .model2NoiseDetectionOutput(checkBoxClass)
                            .model3NoiseDetectionOutput(tickNoiseClass)
                            .model4NoiseDetectionOutput(speckleClass)
                            .createdOn(LocalDateTime.now())
                            .rootPipelineId(rootPipelineId)
                            .status("COMPLETED")
                            .stage("NOISE_DETECTION_MODEL")
                            .message("noise detection completed")
                            .build());


                } else {
                    noiseOutputEntities.add(NoiseModelOutputEnitity.builder()
                            .originId(originId)
                            .paperNo(paperNo)
                            .groupId(groupId)
                            .createdOn(LocalDateTime.now())
                            .rootPipelineId(rootPipelineId)
                            .status("ABSENT")
                            .stage("NOISE_DETECTION")
                            .message("noise detection code absent in the given file")
                            .build());
                }


            } else {
                noiseOutputEntities.add(NoiseModelOutputEnitity.builder()
                        .originId(originId)
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .createdOn(LocalDateTime.now())
                        .rootPipelineId(rootPipelineId)
                        .status("ABSENT")
                        .stage("NOISE_DETECTION")
                        .message("noise detection code absent in the given file")
                        .build());

            }
        }
        catch(Exception e){
            noiseOutputEntities.add(NoiseModelOutputEnitity.builder()
                    .originId(originId)
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .createdOn(LocalDateTime.now())
                    .status("FAILED")
                    .rootPipelineId(rootPipelineId)
                    .stage("NOISE_DETECTION")
                    .message(e.getMessage())
                    .build());
            log.error("Error in the copro process api hit {}", request);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in noise detection action for group id - " + groupId, handymanException, this.action);
        }
    }










    private void coproRequestBuilder (NoiseModelInputEntity entity, Request request, ObjectMapper objectMapper, Long rootPipelineId ,List<NoiseModelOutputEnitity> noiseOutputEntities) {
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        Integer groupId = entity.getGroupId();
        String fileId = entity.getFileId();
        String filePath=entity.getFilePath();

        // exectution is after getting resopnse

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                // Parse the JSON string
                JsonNode rootNode = objectMapper.readTree(responseBody);

                if (rootNode != null && !rootNode.isEmpty()) {
                    Integer processId = rootNode.path("processId").asInt();
                    Integer tenantId = rootNode.path("tenantId").asInt();
                    String inputFilePath = rootNode.path("inputFilePath").asText();
                    String consolidatedClass = rootNode.path("consolidatedClass").asText();
                    Double consolidatedConfidenceScore = rootNode.path("consolidatedConfidenceScore").asDouble();
                    String extractedValue = rootNode.path("noiseModelsResult").toString();

                    //              getting the output of hwnoise model

                    String hwClass = rootNode
                            .path("noiseModelsResult")
                            .path("hwNoiseDetectionOutput").toString();


                    String checkBoxClass = rootNode
                            .path("noiseModelsResult")
                            .path("checkNoiseDetectionOutput").toString();
                    String tickNoiseClass = rootNode
                            .path("noiseModelsResult")
                            .path("tickNoiseDetectionOutput").toString();
                    String speckleClass = rootNode
                            .path("noiseModelsResult")
                            .path("speckleNoiseDetection").toString();

                    noiseOutputEntities.add(NoiseModelOutputEnitity.builder()
                            .fileId(fileId)
                            .originId(originId)
                            .paperNo(paperNo)
                            .groupId(groupId)
                            .processId(processId)
                            .tenantId(tenantId)
                            .inputFilePath(filePath)
                            .consolidatedConfidenceScore(consolidatedConfidenceScore)
                            .consolidatedClass(consolidatedClass)
                            .extractedValue(extractedValue)
                            .model1NoiseDetectionOutput(hwClass)
                            .model2NoiseDetectionOutput(checkBoxClass)
                            .model3NoiseDetectionOutput(tickNoiseClass)
                            .model4NoiseDetectionOutput(speckleClass)
                            .createdOn(LocalDateTime.now())
                            .rootPipelineId(rootPipelineId)
                            .status("COMPLETED")
                            .stage("NOISE_DETECTION_MODEL")
                            .message("noise detection completed")
                            .build());


                } else {
                    noiseOutputEntities.add(NoiseModelOutputEnitity.builder()
                            .originId(originId)
                            .paperNo(paperNo)
                            .groupId(groupId)
                            .fileId(fileId)
                            .createdOn(LocalDateTime.now())
                            .rootPipelineId(rootPipelineId)
                            .status("ABSENT")
                            .stage("NOISE_DETECTION")
                            .message("noise detection code absent in the given file")
                            .build());
                }

            } else {
                noiseOutputEntities.add(NoiseModelOutputEnitity.builder()
                        .originId(originId)
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .fileId(fileId)
                        .createdOn(LocalDateTime.now())
                        .rootPipelineId(rootPipelineId)
                        .status("FAILED")
                        .stage("NOISE_DETECTION")
                        .message(response.message())
                        .build());
                log.error(aMarker, "The Exception occurred in episode of coverage in response {}", response);
            }

        } catch (Exception e) {
            noiseOutputEntities.add(NoiseModelOutputEnitity.builder()
                    .originId(originId)
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .fileId(fileId)
                    .createdOn(LocalDateTime.now())
                    .status("FAILED")
                    .rootPipelineId(rootPipelineId)
                    .stage("NOISE_DETECTION")
                    .message(e.getMessage())
                    .build());
            log.error("Error in the copro process api hit {}", request);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in noise detection action for group id - " + groupId, handymanException, this.action);
        }


    }
    }
