package in.handyman.raven.lib.model.radonbbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.RadonKvpBbox;

import in.handyman.raven.lib.model.radonbbox.query.input.RadonBboxInputEntity;
import in.handyman.raven.lib.model.radonbbox.query.output.RadonBboxOutputEntity;
import in.handyman.raven.lib.model.radonbbox.request.RadonBboxRequest;
import in.handyman.raven.lib.model.radonbbox.request.RadonBboxRequestLineItem;
import in.handyman.raven.lib.model.radonbbox.response.RadonBboxResponse;
import in.handyman.raven.lib.model.radonbbox.response.RadonBboxResponseData;
import in.handyman.raven.lib.model.radonbbox.response.RadonResponsReplicate;
import in.handyman.raven.lib.model.radonbbox.response.RadonResponseBboxLineItem;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.lib.replicate.ReplicateRequest;
import in.handyman.raven.lib.replicate.ReplicateResponse;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.awt.desktop.SystemEventListener;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class RadonBboxConsumerProcess implements CoproProcessor.ConsumerProcess<RadonBboxInputEntity, RadonBboxOutputEntity> {
    public static final String RADON_BBOX = PipelineName.RADON_KVP_BBOX.getProcessName();
    public static final String RADON_BBOX_START = "RADON BBOX START";
    public static final String OKHTTP_CLIENT_TIMEOUT = "okhttp.client.timeout";
    public static final String SOR_ITEM_NAME = "sor_item_name";
    public static final String ANSWER = "answer";
    public static final String PAPER_TYPE = "paper_type";
    public static final String RADON_KVP_BBOX = "RADON_KVP_BBOX";

    public static final String REQUEST_ACTIVATOR_HANDLER_NAME = "copro.request.activator.handler.name";
    public static final String REPLICATE_API_TOKEN_CONTEXT = "replicate.request.api.token";
    public static final String REPLICATE_BBOX_VERSION = "replicate.bbox.version";

    private final Logger log;
    private final Marker aMarker;
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");

    private final ObjectMapper objectMapper;
    public final ActionExecutionAudit action;
    public final String httpClientTimeout;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public final RadonKvpBbox radonKvpBbox;

    public RadonBboxConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action, RadonKvpBbox radonKvpBbox, ObjectMapper objectMapper) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.httpClientTimeout = action.getContext().get(OKHTTP_CLIENT_TIMEOUT);
        this.radonKvpBbox = radonKvpBbox;
        this.objectMapper = objectMapper;
    }


    @Override
    public List<RadonBboxOutputEntity> process(URL endpoint, RadonBboxInputEntity entity) throws Exception {
        log.info("triton consumer process started");
        List<RadonBboxOutputEntity> radonBboxOutputEntities = new ArrayList<>();

        String coproHandlerName = action.getContext().get(REQUEST_ACTIVATOR_HANDLER_NAME);
        String replicateApiToken = action.getContext().get(REPLICATE_API_TOKEN_CONTEXT);
        String replicatePaperClassificationVersion = action.getContext().get(REPLICATE_BBOX_VERSION);

        final RadonBboxRequest radonBboxRequestData = getRadonBboxRequestData(entity);

        final String jsonInputRequest = objectMapper.writeValueAsString(radonBboxRequestData);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName(RADON_BBOX_START);
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype(TritonDataTypes.BYTES.name());
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        final String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        String tritonRequestActivator = radonKvpBbox.getTritonActivator();

        if (Objects.equals("TRITON", coproHandlerName)) {
            final Request tritonRequest = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
            tritonRequestBuilder(entity, tritonRequest, objectMapper, radonBboxOutputEntities);

        } else if (Objects.equals("REPLICATE", coproHandlerName)) {
            ReplicateRequest replicateRequest=new ReplicateRequest();
//            replicateRequest.setVersion(replicatePaperClassificationVersion);
            replicateRequest.setInput(radonBboxRequestData);
            String replicateJsonRequest = objectMapper.writeValueAsString(replicateRequest);
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(replicateJsonRequest, MediaTypeJSON))
                    .addHeader("Authorization", "Bearer " + replicateApiToken)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "wait")
                    .build();

            replicateRequestBuilder(entity, request, radonBboxOutputEntities, replicateJsonRequest, endpoint );
        }

        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n coproUrl:  {} ,inputFilePath: {} originId: {} paperNo: {} ", endpoint, entity.getInputFilePath(), entity.getOriginId(), entity.getPaperNo() );
        }
        return radonBboxOutputEntities;
    }

    private void replicateRequestBuilder(RadonBboxInputEntity entity, Request request, List<RadonBboxOutputEntity> parentObj, String jsonRequest, URL endpoint) {
        Long tenantId = entity.getTenantId();
        String originId = entity.getOriginId();
        Long paperNo = entity.getPaperNo();
        Long groupId = entity.getGroupId();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                ObjectMapper objectMappers = new ObjectMapper();
                ReplicateResponse radonBBoxResponse = objectMappers.readValue(responseBody, ReplicateResponse.class);
                try {

                    extractReplicateOutputDataRequest(entity, radonBBoxResponse.getOutput().toString(), parentObj, radonBBoxResponse.getModel(), radonBBoxResponse.getVersion(), jsonRequest, responseBody, endpoint.toString());
                } catch (JsonProcessingException e) {
                    throw new HandymanException("Handwritten classification failed in processing replicate response ", e);
                }
            } else {
                parentObj.add(RadonBboxOutputEntity.builder()
                        .tenantId(Optional.ofNullable(tenantId).map(String::valueOf).map(Long::valueOf).orElse(null))
                        .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                        .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Long::valueOf).orElse(null))
                        .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Long::valueOf).orElse(null))
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(RADON_KVP_BBOX)
                        .message(responseBody)
                        .groupId(entity.getGroupId())
                        .rootPipelineId(entity.getRootPipelineId())
                        .batchId(entity.getBatchId())
                        .request(jsonRequest)
                        .response(responseBody)
                        .endpoint(String.valueOf(endpoint))
                        .build());
                log.info(aMarker, "The Exception occurred in paper classification replicate response");
            }
        } catch (Exception e) {
            parentObj.add(RadonBboxOutputEntity.builder()
                    .tenantId(Optional.ofNullable(tenantId).map(String::valueOf).map(Long::valueOf).orElse(null))
                    .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                    .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Long::valueOf).orElse(null))
                    .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Long::valueOf).orElse(null))
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(RADON_KVP_BBOX)
                    .message(ExceptionUtil.toString(e))
                    .groupId(entity.getGroupId())
                    .rootPipelineId(entity.getRootPipelineId())
                    .batchId(entity.getBatchId())
                    .request(jsonRequest)
                    .response("Error in getting replicate Response")
                    .endpoint(String.valueOf(endpoint))
                    .build());
            log.error(aMarker, "The Exception occurred in paper classification replicate request", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Radon Bbox consumer replicate failed for batch/group " + groupId, handymanException, this.action);

        }
    }

    private void extractReplicateOutputDataRequest(RadonBboxInputEntity entity, String responseLineItems, List<RadonBboxOutputEntity> parentObj, String modelName, String modelVersion, String request, String response, String endpoint) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        RadonResponsReplicate radonBboxResponse = mapper.readValue(responseLineItems, RadonResponsReplicate.class);

        buildOutputParentObjectReplicate( parentObj,  entity, true, "Completed the radon kvp bbox api for replicate", radonBboxResponse, modelName, modelVersion, request, response, endpoint);
    }

    @NotNull
    private RadonBboxRequest getRadonBboxRequestData(RadonBboxInputEntity entity) throws JsonProcessingException {

        final RadonBboxRequest radonBboxRequestData = new RadonBboxRequest();
        radonBboxRequestData.setOriginId(entity.getOriginId());
        radonBboxRequestData.setPaperNumber(entity.getPaperNo());
        radonBboxRequestData.setProcessId(action.getProcessId());
        radonBboxRequestData.setGroupId(entity.getGroupId());
        radonBboxRequestData.setTenantId(entity.getTenantId());
        radonBboxRequestData.setRootPipelineId(entity.getRootPipelineId());
        radonBboxRequestData.setActionId(action.getActionId());
        radonBboxRequestData.setProcess(RADON_BBOX);
        radonBboxRequestData.setInputFilePath(entity.getInputFilePath());
        radonBboxRequestData.setOutputDir(radonKvpBbox.getOutputDir());
        radonBboxRequestData.setBatchId(entity.getBatchId());
        radonBboxRequestData.setBase64image(entity.getBase64img());
        List<RadonBboxRequestLineItem> items = objectMapper.readValue(entity.getRadonOutput(), new TypeReference<>() {
        });
//        radonBboxRequestData.setRadonBboxLineItems(items.toString());
        String bboxToString = objectMapper.writeValueAsString(items);
        radonBboxRequestData.setRadonBboxLineItems(bboxToString);

        System.out.println(objectMapper.writeValueAsString(radonBboxRequestData));
        return radonBboxRequestData;
    }

    private void tritonRequestBuilder(RadonBboxInputEntity entity, Request request, ObjectMapper objectMapper, List<RadonBboxOutputEntity> parentObj) {

        try (Response response = httpclient.newCall(request).execute()) {

            if (response.isSuccessful()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                RadonBboxResponse radonBboxModelResponse = objectMapper.readValue(responseBody, RadonBboxResponse.class);

                if (radonBboxModelResponse.getOutputs() != null && !radonBboxModelResponse.getOutputs().isEmpty()) {
                    radonBboxModelResponse.getOutputs().forEach(o -> o.getData().forEach(noiseModelDataItem ->
                            extractedOutputRequest(entity, objectMapper, parentObj, radonBboxModelResponse.getModelName(), radonBboxModelResponse.getModelVersion(), noiseModelDataItem)
                    ));
                }

            } else {
                buildOutputParentObject(parentObj, entity, false, "Error in response status code " + response.message(), new RadonBboxResponseData());
            }

        } catch (Exception exception) {
            buildOutputParentObject(parentObj, entity, false, "Error in processing the request " + ExceptionUtil.toString(exception), new RadonBboxResponseData());

            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("NOISE_DETECTION_MODEL  consumer failed for originId " + entity.getOriginId(), handymanException, this.action);
            log.error(aMarker, "The Exception occurred in request {}", request, exception);
        }
    }


    private void extractedOutputRequest(RadonBboxInputEntity entity, ObjectMapper objectMapper, List<RadonBboxOutputEntity> parentObj, String modelName, String modelVersion, String radonKvpBboxDataItem) {

        try {

            RadonBboxResponseData radonBboxResponse = objectMapper.readValue(radonKvpBboxDataItem, RadonBboxResponseData.class);
            buildOutputParentObject(parentObj, entity, true, "Completed the radon kvp bbox api", radonBboxResponse);

        } catch (JsonProcessingException e) {
            buildOutputParentObject(parentObj, entity, false, "Error in processing the output json " + ExceptionUtil.toString(e), new RadonBboxResponseData());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("RADON_BBOX_MODEL consumer failed for originId " + entity.getOriginId(), handymanException, this.action);
            log.error(aMarker, "The Exception occurred in request {}", e.toString());
        }
    }


    private void buildOutputParentObject(List<RadonBboxOutputEntity> parentObj, RadonBboxInputEntity entity, Boolean status, String message, RadonBboxResponseData radonBboxResponse) {


        if (Boolean.TRUE.equals(status)) {
            radonBboxResponse.getRadonBboxLineItems().forEach(radonResponseBboxLineItem -> {
                try {
                    parentObj.add(RadonBboxOutputEntity.builder()
                            .modelRegistry(entity.getModelRegistry())
                            .inputFilePath(entity.getInputFilePath())
                            .sorContainerName(entity.getSorContainerName())
                            .batchId(radonBboxResponse.getBatchId())
                            .paperNo(radonBboxResponse.getPaperNumber())
                            .originId(radonBboxResponse.getOriginId())
                            .groupId(radonBboxResponse.getGroupId())
                            .tenantId(radonBboxResponse.getTenantId())
                            .rootPipelineId(radonBboxResponse.getRootPipelineId())
                            .bBox(objectMapper.writeValueAsString(radonResponseBboxLineItem.getBBox()))
                            .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                            .stage(RADON_KVP_BBOX)
                            .message(message)
                            .sorItemName(radonResponseBboxLineItem.getSorItemName())
                            .answer(radonResponseBboxLineItem.getAnswer())
                            .valueType(radonResponseBboxLineItem.getValueType())
                            .build());
                } catch (JsonProcessingException e) {
                    parentObj.add(RadonBboxOutputEntity.builder()
                            .modelRegistry(entity.getModelRegistry())
                            .inputFilePath(entity.getInputFilePath())
                            .sorContainerName(entity.getSorContainerName())
                            .batchId(entity.getBatchId())
                            .paperNo(entity.getPaperNo())
                            .originId(entity.getOriginId())
                            .groupId(entity.getGroupId())
                            .tenantId(entity.getTenantId())
                            .rootPipelineId(entity.getRootPipelineId())
                            .message(message)
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .stage(RADON_KVP_BBOX)
//                    .sorItemName(entity.getRadonOutput().get(SOR_ITEM_NAME))
//                    .answer(entity.getRadonOutput().get(ANSWER))
//                    .paperType(entity.getRadonOutput().get(PAPER_TYPE))
//                    .bBox(new BoundingBoxObject())
                            .build());
                }
            });

        } else {
            parentObj.add(RadonBboxOutputEntity.builder()
                    .modelRegistry(entity.getModelRegistry())
                    .inputFilePath(entity.getInputFilePath())
                    .sorContainerName(entity.getSorContainerName())
                    .batchId(entity.getBatchId())
                    .paperNo(entity.getPaperNo())
                    .originId(entity.getOriginId())
                    .groupId(entity.getGroupId())
                    .tenantId(entity.getTenantId())
                    .rootPipelineId(entity.getRootPipelineId())
                    .message(message)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(RADON_KVP_BBOX)
//                    .sorItemName(entity.getRadonOutput().get(SOR_ITEM_NAME))
//                    .answer(entity.getRadonOutput().get(ANSWER))
//                    .paperType(entity.getRadonOutput().get(PAPER_TYPE))
//                    .bBox(new BoundingBoxObject())
                    .build())
            ;
        }

    }



    private void buildOutputParentObjectReplicate(List<RadonBboxOutputEntity> parentObj, RadonBboxInputEntity entity, Boolean status, String message, RadonResponsReplicate radonBboxResponse, String modelName, String modelVersion, String request, String response, String endpoint) {


        if (Boolean.TRUE.equals(status)) {
            radonBboxResponse.getRadonBboxLineItems().forEach(radonResponseBboxLineItem -> {
                try {
                    parentObj.add(RadonBboxOutputEntity.builder()
                            .modelRegistry(entity.getModelRegistry())
                            .inputFilePath(entity.getInputFilePath())
                            .sorContainerName(entity.getSorContainerName())
                            .batchId(radonBboxResponse.getBatchId())
                            .paperNo(radonBboxResponse.getPaperNumber())
                            .originId(radonBboxResponse.getOriginId())
                            .groupId(Long.valueOf(radonBboxResponse.getGroupId()))
                            .tenantId(radonBboxResponse.getTenantId())
                            .rootPipelineId(radonBboxResponse.getRootPipelineId())
                            .bBox(objectMapper.writeValueAsString(radonResponseBboxLineItem.getBBox()))
                            .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                            .stage(RADON_KVP_BBOX)
                            .message(message)
                            .sorItemName(radonResponseBboxLineItem.getSorItemName())
                            .answer(radonResponseBboxLineItem.getAnswer())
                            .valueType(radonResponseBboxLineItem.getValueType())
                            .endpoint(endpoint)
                            .response(response)
                            .request(request)
                            .build());
                } catch (JsonProcessingException e) {
                    parentObj.add(RadonBboxOutputEntity.builder()
                            .modelRegistry(entity.getModelRegistry())
                            .inputFilePath(entity.getInputFilePath())
                            .sorContainerName(entity.getSorContainerName())
                            .batchId(entity.getBatchId())
                            .paperNo(entity.getPaperNo())
                            .originId(entity.getOriginId())
                            .groupId(entity.getGroupId())
                            .tenantId(entity.getTenantId())
                            .rootPipelineId(entity.getRootPipelineId())
                            .message(message)
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .stage(RADON_KVP_BBOX)
//                    .sorItemName(entity.getRadonOutput().get(SOR_ITEM_NAME))
//                    .answer(entity.getRadonOutput().get(ANSWER))
//                    .paperType(entity.getRadonOutput().get(PAPER_TYPE))
//                    .bBox(new BoundingBoxObject())
                            .build());
                }
            });

        } else {
            parentObj.add(RadonBboxOutputEntity.builder()
                    .modelRegistry(entity.getModelRegistry())
                    .inputFilePath(entity.getInputFilePath())
                    .sorContainerName(entity.getSorContainerName())
                    .batchId(entity.getBatchId())
                    .paperNo(entity.getPaperNo())
                    .originId(entity.getOriginId())
                    .groupId(entity.getGroupId())
                    .tenantId(entity.getTenantId())
                    .rootPipelineId(entity.getRootPipelineId())
                    .message(message)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(RADON_KVP_BBOX)
//                    .sorItemName(entity.getRadonOutput().get(SOR_ITEM_NAME))
//                    .answer(entity.getRadonOutput().get(ANSWER))
//                    .paperType(entity.getRadonOutput().get(PAPER_TYPE))
//                    .bBox(new BoundingBoxObject())
                    .build())
            ;
        }

    }

}


