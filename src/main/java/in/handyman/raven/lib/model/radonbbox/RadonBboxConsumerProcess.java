package in.handyman.raven.lib.model.radonbbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;

import in.handyman.raven.lib.model.radonbbox.RadonBboxModelData;
import in.handyman.raven.lib.model.radonbbox.RadonBboxInputEntity;
import in.handyman.raven.lib.model.radonbbox.RadonBboxOutputEntity;
import in.handyman.raven.lib.model.radonbbox.RadonBboxResponse;

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


public class RadonBboxConsumerProcess implements CoproProcessor.ConsumerProcess<RadonBboxInputEntity, RadonBboxOutputEntity> {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    public static final String RADON_BBOX = PipelineName.RADON_BBOX.getProcessName();

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

    public RadonBboxConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.httpClientTimeout = action.getContext().get("okhttp.client.timeout");
    }


    @Override
    public List<RadonBboxOutputEntity> process(URL endpoint, RadonBboxInputEntity entity) throws Exception {
        log.info("triton consumer process started");
        List<RadonBboxOutputEntity> radonBboxOutputEntities = new ArrayList<>();
        final String filePath = entity.getInputFilePath();
        final Long rootPipelineId = entity.getRootPipelineId();
        final Long actionId = action.getActionId();
        final ObjectMapper objectMapper = new ObjectMapper();
        //payload
        final RadonBboxModelData RadonBboxModelData = new RadonBboxModelData();
        RadonBboxModelData.setRootPipelineId(rootPipelineId);
        RadonBboxModelData.setProcess(RADON_BBOX);
        RadonBboxModelData.setInputFilePath(filePath);
        RadonBboxModelData.setActionId(actionId);
        RadonBboxModelData.setProcessId(action.getProcessId());
        RadonBboxModelData.setOriginId(entity.getOriginId());
        RadonBboxModelData.setPaperNo(entity.getPaperNo());
        RadonBboxModelData.setGroupId(entity.getGroupId());
        RadonBboxModelData.setOutputDir(entity.getOutputDir());
        RadonBboxModelData.setTenantId(entity.getTenantId());
        final String jsonInputRequest = objectMapper.writeValueAsString(RadonBboxModelData);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("RADON BBOX START");
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
//            Request request = new Request.Builder().url(endpoint)
//                    .post(RequestBody.create(jsonInputRequest, MediaTypeJSON)).build();
//            coproRequestBuilder(entity, request, objectMapper, rootPipelineId, radonBboxOutputEntities);
            log.info("input object node in the consumer process");

        } else {
            final Request tritonRequest = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
            tritonRequestBuilder(entity, tritonRequest, objectMapper, radonBboxOutputEntities);
        }

        if (log.isInfoEnabled()) {
            log.info("input object node in the consumer process coproURL {}, inputFilePath {}", endpoint, filePath);
        }
        return radonBboxOutputEntities;
    }

    private void tritonRequestBuilder(RadonBboxInputEntity entity, Request request, ObjectMapper objectMapper, List<RadonBboxOutputEntity> parentObj) {
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        Integer groupId = entity.getGroupId();
        Long tenantId = entity.getTenantId();
        Long processId = action.getProcessId();


        try (Response response = httpclient.newCall(request).execute()) {

            if (response.isSuccessful()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                RadonBboxResponse radonBboxModelResponse = objectMapper.readValue(responseBody, RadonBboxResponse.class);
                if (radonBboxModelResponse.getOutputs() != null && !radonBboxModelResponse.getOutputs().isEmpty()) {
                    radonBboxModelResponse.getOutputs().forEach(o -> o.getData().forEach(noiseModelDataItem ->
                            extractedOutputRequest(entity, objectMapper, parentObj, radonBboxModelResponse.getModelName(), radonBboxModelResponse.getModelVersion(), noiseModelDataItem, paperNo)
                    ));
                }

            } else {
                parentObj.add(
                        RadonBboxOutputEntity
                                .builder()
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .groupId(Long.valueOf(groupId))
                                .processId(processId)
                                .tenantId(tenantId)
                                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                .stage(RADON_BBOX)
                                .message(response.message())
                                .createdOn(LocalDateTime.now())
                                .rootPipelineId(entity.getRootPipelineId())
                                .batchId(entity.getBatchId())
                                .build());
                log.error(aMarker, "Error in response {}", response.message());
            }

        } catch (Exception exception) {
            parentObj.add(
                    RadonBboxOutputEntity
                            .builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .groupId(Long.valueOf(groupId))
                            .processId(processId)
                            .tenantId(tenantId)
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .stage(RADON_BBOX)
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


    private void extractedOutputRequest(RadonBboxInputEntity entity, ObjectMapper objectMapper, List<RadonBboxOutputEntity> parentObj, String modelName, String modelVersion, String noiseModelDataItem, Integer paperNo) {
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        Long tenantId = entity.getTenantId();
        Long processId = action.getProcessId();

        try {
            JsonNode rootNode = objectMapper.readTree(noiseModelDataItem);


            if (rootNode != null && !rootNode.isEmpty()) {
                String inputFilePath = rootNode.path("inputFilePath").asText();
                String originId1 = rootNode.path("originId").asText();
                String paperNo1 = rootNode.path("paperNo").asText();
                int groupId1 = rootNode.path("groupId").asInt();

                parentObj.add(RadonBboxOutputEntity.builder()
                        .originId(originId1)
                        .paperNo(Long.valueOf(paperNo1))
                        .groupId(Long.valueOf(groupId1))
                        .processId(processId)
                        .tenantId(tenantId)
                        .inputFilePath(inputFilePath)
                        .createdOn(LocalDateTime.now())
                        .rootPipelineId(action.getRootPipelineId())
                        .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                        .stage(RADON_BBOX)
                        .message("radon bbox completed")
                        .batchId(entity.getBatchId())
                        .build());
            }

        } catch (JsonProcessingException e) {
            parentObj.add(
                    RadonBboxOutputEntity
                            .builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .paperNo(Long.valueOf(paperNo))
                            .groupId(Long.valueOf(groupId))
                            .processId(processId)
                            .tenantId(tenantId)
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .stage(RADON_BBOX)
                            .message(e.getMessage())
                            .createdOn(LocalDateTime.now())
                            .rootPipelineId(entity.getRootPipelineId())
                            .batchId(entity.getBatchId())
                            .build());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("RADON_BBOX_MODEL consumer failed for originId " + originId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in request {}", e.toString());
        }
    }

//    private void coproRequestBuilder(RadonBboxInputEntity entity, Request request, ObjectMapper objectMapper, Long rootPipelineId, List<RadonBboxOutputEntity> radonBboxOutputEntities) {
//        String originId = entity.getOriginId();
//        Integer paperNo = entity.getPaperNo();
//        Integer groupId = entity.getGroupId();
//        Long processId = action.getProcessId();
//        Long tenantId = entity.getTenantId();
//
//
//        try (Response response = httpclient.newCall(request).execute()) {
//            if (response.isSuccessful()) {
//                String responseBody = Objects.requireNonNull(response.body()).string();
//
//                JsonNode rootNode = objectMapper.readTree(responseBody);
//
//                if (rootNode != null && !rootNode.isEmpty()) {
//                    String inputFilePath = rootNode.path("inputFilePath").asText();
//
//
//                    radonBboxOutputEntities.add(RadonBboxOutputEntity.builder()
//                            .originId(originId)
//                            .paperNo(Long.valueOf(paperNo))
//                            .groupId(Long.valueOf(groupId))
//                            .processId(processId)
//                            .tenantId(tenantId)
//                            .inputFilePath(inputFilePath)
//                            .createdOn(LocalDateTime.now())
//                            .rootPipelineId(rootPipelineId)
//                            .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
//                            .stage(RADON_BBOX)
//                            .message("radon bbox completed")
//                            .batchId(entity.getBatchId())
//                            .build());
//
//
//                } else {
//                    radonBboxOutputEntities.add(RadonBboxOutputEntity.builder()
//                            .originId(originId)
//                            .paperNo(Long.valueOf(paperNo))
//                            .groupId(Long.valueOf(groupId))
//                            .createdOn(LocalDateTime.now())
//                            .rootPipelineId(rootPipelineId)
//                            .status(ConsumerProcessApiStatus.ABSENT.getStatusDescription())
//                            .stage(RADON_BBOX)
//                            .message("noise detection code absent in the given file")
//                            .batchId(entity.getBatchId())
//                            .build());
//                }
//
//            } else {
//                radonBboxOutputEntities.add(RadonBboxOutputEntity.builder()
//                        .originId(originId)
//                        .paperNo(Long.valueOf(paperNo))
//                        .groupId(Long.valueOf(groupId))
//                        .createdOn(LocalDateTime.now())
//                        .rootPipelineId(rootPipelineId)
//                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
//                        .stage(RADON_BBOX)
//                        .message(response.message())
//                        .batchId(entity.getBatchId())
//                        .build());
//                log.error(aMarker, "The Exception occurred in episode of coverage in response {}", response);
//            }
//
//        } catch (Exception e) {
//            radonBboxOutputEntities.add(RadonBboxOutputEntity.builder()
//                    .originId(originId)
//                    .paperNo(Long.valueOf(paperNo))
//                    .groupId(Long.valueOf(groupId))
//                    .createdOn(LocalDateTime.now())
//                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
//                    .rootPipelineId(rootPipelineId)
//                    .stage(RADON_BBOX)
//                    .message(e.getMessage())
//                    .batchId(entity.getBatchId())
//                    .build());
//            log.error("Error in the copro process api hit {}", request);
//            HandymanException handymanException = new HandymanException(e);
//            HandymanException.insertException("Error in noise detection action for group id - " + groupId, handymanException, this.action);
//        }
//
//
//    }
}


