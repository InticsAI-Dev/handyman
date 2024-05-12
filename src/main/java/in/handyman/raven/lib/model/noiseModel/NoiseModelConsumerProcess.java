package in.handyman.raven.lib.model.noiseModel;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


public class NoiseModelConsumerProcess implements CoproProcessor.ConsumerProcess<NoiseModelInputEntity, NoiseModelOutputEntity> {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    public static final String NOISE_DETECTION = PipelineName.NOISE_DETECTION.getProcessName();

    private final Logger log;
    private final Marker aMarker;
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    public final String httpClientTimeout;
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
    public List<NoiseModelOutputEntity> process(URL endpoint, NoiseModelInputEntity entity) throws Exception {
        log.info("copro consumer process started");
        List<NoiseModelOutputEntity> noiseOutputEntities = new ArrayList<>();
        final String filePath = entity.getInputFilePath();
        final Long rootPipelineId = entity.getRootPipelineId();
        final Long actionId = action.getActionId();
        final ObjectMapper objectMapper = new ObjectMapper();
        //payload
        final NoiseModelData NoiseModelData = new NoiseModelData();
        NoiseModelData.setRootPipelineId(rootPipelineId);
        NoiseModelData.setProcess(NOISE_DETECTION);
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
        requestBody.setName("NOISE MODEL START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        final String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        if (log.isInfoEnabled()) {
            log.info("input object node in the consumer process  inputFilePath {}", filePath);
        }
        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);

        if (Objects.equals("false", tritonRequestActivator)) {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonInputRequest, MediaTypeJSON)).build();
            coproRequestBuilder(entity, request, objectMapper, rootPipelineId, noiseOutputEntities);
        } else {
            final Request tritonRequest = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
            tritonRequestBuilder(entity, tritonRequest, objectMapper, noiseOutputEntities);
        }

        if (log.isInfoEnabled()) {
            log.info("input object node in the consumer process coproURL {}, inputFilePath {}", endpoint, filePath);
        }
        return noiseOutputEntities;
    }

    private void tritonRequestBuilder(NoiseModelInputEntity entity, Request request, ObjectMapper objectMapper, List<NoiseModelOutputEntity> parentObj) {
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        Integer groupId = entity.getGroupId();
        Long tenantId = entity.getTenantId();
        Long processId = action.getProcessId();


        try (Response response = httpclient.newCall(request).execute()) {

            if (response.isSuccessful()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                NoiseModelResponse noiseModelResponse = objectMapper.readValue(responseBody, NoiseModelResponse.class);
                if (noiseModelResponse.getOutputs() != null && !noiseModelResponse.getOutputs().isEmpty()) {
                    noiseModelResponse.getOutputs().forEach(o -> o.getData().forEach(noiseModelDataItem ->
                            extractedOutputRequest(entity, objectMapper, parentObj, noiseModelResponse.getModelName(), noiseModelResponse.getModelVersion(), noiseModelDataItem, paperNo)
                    ));
                }

            } else {
                parentObj.add(
                        NoiseModelOutputEntity
                                .builder()
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .groupId(Long.valueOf(groupId))
                                .processId(processId)
                                .tenantId(tenantId)
                                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                .stage(NOISE_DETECTION)
                                .message(response.message())
                                .createdOn(LocalDateTime.now())
                                .rootPipelineId(entity.getRootPipelineId())
                                .batchId(entity.getBatchId())
                                .build());
                log.error(aMarker, "Error in response {}", response.message());
            }

        } catch (Exception exception) {
            parentObj.add(
                    NoiseModelOutputEntity
                            .builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .groupId(Long.valueOf(groupId))
                            .processId(processId)
                            .tenantId(tenantId)
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .stage(NOISE_DETECTION)
                            .message(exception.getMessage())
                            .createdOn(LocalDateTime.now())
                            .rootPipelineId(entity.getRootPipelineId())
                            .batchId(entity.getBatchId())
                            .build());
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("NOISE_DETECTION_MODEL  consumer failed for originId " + originId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in request {}", request, exception);
        }
    }


    private void extractedOutputRequest(NoiseModelInputEntity entity, ObjectMapper objectMapper, List<NoiseModelOutputEntity> parentObj, String modelName, String modelVersion, String noiseModelDataItem, Integer paperNo) {
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        Long tenantId = entity.getTenantId();
        Long processId = action.getProcessId();

        try {
            JsonNode rootNode = objectMapper.readTree(noiseModelDataItem);


            if (rootNode != null && !rootNode.isEmpty()) {
                String inputFilePath = rootNode.path("inputFilePath").asText();
                String consolidatedClass = rootNode.path("consolidatedClass").asText();
                Double consolidatedConfidenceScore = rootNode.path("consolidatedConfidenceScore").asDouble();
                String extractedValue = rootNode.path("noiseModelsResult").toString();
                String originId1 = rootNode.path("originId").asText();
                String paperNo1 = rootNode.path("paperNo").asText();
                int groupId1 = rootNode.path("groupId").asInt();


                String hwClass = rootNode
                        .path("noiseModelsResult")
                        .path("hwNoiseDetectionOutput").toString();


                String checkBoxClass = rootNode
                        .path("noiseModelsResult")
                        .path("checkNoiseDetectionOutput").toString();
                String tickNoiseClass = rootNode
                        .path("noiseModelsResult")
                        .path("checkboxMarkDetectionOutput").toString();
                String speckleClass = rootNode
                        .path("noiseModelsResult")
                        .path("speckleNoiseDetectionOutput").toString();

                parentObj.add(NoiseModelOutputEntity.builder()
                        .originId(originId1)
                        .paperNo(Long.valueOf(paperNo1))
                        .groupId(Long.valueOf(groupId1))
                        .processId(processId)
                        .tenantId(tenantId)
                        .inputFilePath(inputFilePath)
                        .consolidatedConfidenceScore(consolidatedConfidenceScore)
                        .consolidatedClass(consolidatedClass)
                        .noiseModelsResult(extractedValue)
                        .hwNoiseDetectionOutput(hwClass)
                        .checkNoiseDetectionOutput(checkBoxClass)
                        .checkboxMarkDetectionOutput(tickNoiseClass)
                        .speckleNoiseDetectionOutput(speckleClass)
                        .createdOn(LocalDateTime.now())
                        .rootPipelineId(action.getRootPipelineId())
                        .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                        .stage(NOISE_DETECTION)
                        .message("noise detection completed")
                        .modelName(modelName)
                        .modelVersion(modelVersion)
                        .batchId(entity.getBatchId())
                        .build());
            }

        } catch (JsonProcessingException e) {
            parentObj.add(
                    NoiseModelOutputEntity
                            .builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .paperNo(Long.valueOf(paperNo))
                            .groupId(Long.valueOf(groupId))
                            .processId(processId)
                            .tenantId(tenantId)
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .stage(NOISE_DETECTION)
                            .message(e.getMessage())
                            .createdOn(LocalDateTime.now())
                            .rootPipelineId(entity.getRootPipelineId())
                            .batchId(entity.getBatchId())
                            .build());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("NOISE_DETECTION_MODEL consumer failed for originId " + originId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in request {}", e.toString());
        }
    }

    private void coproRequestBuilder(NoiseModelInputEntity entity, Request request, ObjectMapper objectMapper, Long rootPipelineId, List<NoiseModelOutputEntity> noiseOutputEntities) {
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        Integer groupId = entity.getGroupId();
        Long processId = action.getProcessId();
        Long tenantId = entity.getTenantId();


        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = Objects.requireNonNull(response.body()).string();

                JsonNode rootNode = objectMapper.readTree(responseBody);

                if (rootNode != null && !rootNode.isEmpty()) {
                    String inputFilePath = rootNode.path("inputFilePath").asText();
                    String consolidatedClass = rootNode.path("consolidatedClass").asText();
                    Double consolidatedConfidenceScore = rootNode.path("consolidatedConfidenceScore").asDouble();
                    String extractedValue = rootNode.path("noiseModelsResult").toString();

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

                    noiseOutputEntities.add(NoiseModelOutputEntity.builder()
                            .originId(originId)
                            .paperNo(Long.valueOf(paperNo))
                            .groupId(Long.valueOf(groupId))
                            .processId(processId)
                            .tenantId(tenantId)
                            .inputFilePath(inputFilePath)
                            .consolidatedConfidenceScore(consolidatedConfidenceScore)
                            .consolidatedClass(consolidatedClass)
                            .noiseModelsResult(extractedValue)
                            .hwNoiseDetectionOutput(hwClass)
                            .checkNoiseDetectionOutput(checkBoxClass)
                            .checkboxMarkDetectionOutput(tickNoiseClass)
                            .speckleNoiseDetectionOutput(speckleClass)
                            .createdOn(LocalDateTime.now())
                            .rootPipelineId(rootPipelineId)
                            .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                            .stage(NOISE_DETECTION)
                            .message("noise detection completed")
                            .batchId(entity.getBatchId())
                            .build());


                } else {
                    noiseOutputEntities.add(NoiseModelOutputEntity.builder()
                            .originId(originId)
                            .paperNo(Long.valueOf(paperNo))
                            .groupId(Long.valueOf(groupId))
                            .createdOn(LocalDateTime.now())
                            .rootPipelineId(rootPipelineId)
                            .status(ConsumerProcessApiStatus.ABSENT.getStatusDescription())
                            .stage(NOISE_DETECTION)
                            .message("noise detection code absent in the given file")
                            .batchId(entity.getBatchId())
                            .build());
                }

            } else {
                noiseOutputEntities.add(NoiseModelOutputEntity.builder()
                        .originId(originId)
                        .paperNo(Long.valueOf(paperNo))
                        .groupId(Long.valueOf(groupId))
                        .createdOn(LocalDateTime.now())
                        .rootPipelineId(rootPipelineId)
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(NOISE_DETECTION)
                        .message(response.message())
                        .batchId(entity.getBatchId())
                        .build());
                log.error(aMarker, "The Exception occurred in episode of coverage in response {}", response);
            }

        } catch (Exception e) {
            noiseOutputEntities.add(NoiseModelOutputEntity.builder()
                    .originId(originId)
                    .paperNo(Long.valueOf(paperNo))
                    .groupId(Long.valueOf(groupId))
                    .createdOn(LocalDateTime.now())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .rootPipelineId(rootPipelineId)
                    .stage(NOISE_DETECTION)
                    .message(e.getMessage())
                    .batchId(entity.getBatchId())
                    .build());
            log.error("Error in the copro process api hit {}", request);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in noise detection action for group id - " + groupId, handymanException, this.action);
        }


    }
}