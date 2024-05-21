package in.handyman.raven.lib.model.face.detection;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.FaceDetectionAction;
import in.handyman.raven.lib.model.face.detection.triton.FaceDetectionModelResponse;
import in.handyman.raven.lib.model.paragraph.detection.ParagraphExtractionResponse;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import jakarta.json.Json;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.DataInput;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class FaceDetectionConsumerProcess implements CoproProcessor.ConsumerProcess<FaceDetectionQueryInputTable, FaceDetectionQueryOutputTable> {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.face.detection.activator";
    public static final String PROCESS_NAME = PipelineName.FACE_DETECTION.getProcessName();
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private final String outputDir;

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final FaceDetectionAction aAction;
    private final int timeOut;

    public FaceDetectionConsumerProcess(Logger log, Marker aMarker, String outputDir, ActionExecutionAudit action, FaceDetectionAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.outputDir = outputDir;
        this.action = action;
        this.aAction = aAction;
        this.timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder().connectTimeout(this.timeOut, TimeUnit.MINUTES).writeTimeout(this.timeOut, TimeUnit.MINUTES).readTimeout(this.timeOut, TimeUnit.MINUTES).build();
    }

    @Override
    public List<FaceDetectionQueryOutputTable> process(URL endpoint, FaceDetectionQueryInputTable entity) throws Exception {
        List<FaceDetectionQueryOutputTable> parentObj = new ArrayList<>();
        String entityFilePath = entity.getInputFilePath();
        String rootPipelineId = String.valueOf(entity.getRootPipelineId());
        String filePath = String.valueOf(entity.getInputFilePath());
        Long actionId = action.getActionId();

        FaceDetectionExtractionRequest faceDetectionExtractionRequest = new FaceDetectionExtractionRequest();


        faceDetectionExtractionRequest.setInputFilePath(filePath);


        String jsonInputRequest = mapper.writeValueAsString(faceDetectionExtractionRequest);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("FACE DETECTION START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = mapper.writeValueAsString(tritonInputRequest);


        log.info(aMarker, " Input variables id : {}", action.getActionId());


        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} and outputDir {}", endpoint, entityFilePath, outputDir);
        }
        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);


        if (Objects.equals("false", tritonRequestActivator)) {
            log.info("Triton request activator variable: {} value: {}, Copro API running in legacy mode and json input {}", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator, jsonInputRequest);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
            coproResponseBuider(entity, request, parentObj);
        } else {
            log.info("Triton request activator variable: {} value: {}, Copro API running in Triton mode  and json input {} ", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator, jsonRequest);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();
            tritonRequestBuilder(entity, request, parentObj);
        }

        return parentObj;
    }


    private void coproResponseBuider(FaceDetectionQueryInputTable entity, Request request, List<FaceDetectionQueryOutputTable> parentObj) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                FaceDetectionExtractionResponse modelResponse = mapper.readValue(responseBody, FaceDetectionExtractionResponse.class);
                if (modelResponse.getPayload() != null && !modelResponse.getPayload().isEmpty()) {
                    modelResponse.getPayload().forEach(o -> o.getPredictions().forEach(faceDataItem -> {
                        extractedCoproOutputResponse(entity, faceDataItem, parentObj);
                    }));

                }
            } else {
                parentObj.add(FaceDetectionQueryOutputTable.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .filePath(entity.getInputFilePath())
                        .tenantId(tenantId)
                        .processId(processId)
                        .rootPipelineId(rootPipelineId)
                        .process(entity.getProcess())
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .message(response.message())
                        .build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (IOException e) {
            parentObj.add(FaceDetectionQueryOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .filePath(entity.getInputFilePath())
                    .tenantId(tenantId)
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .process(entity.getProcess())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .build());

            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("face detection consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }

    }

    private void extractedCoproOutputResponse(FaceDetectionQueryInputTable entity, FaceDetectionExtractionPrediction predictions, List<FaceDetectionQueryOutputTable> parentObj) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();

        parentObj.add(FaceDetectionQueryOutputTable.builder()
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .createdUserId(tenantId)
                        .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                        .lastUpdatedUserId(tenantId)
                        .originId(originId)
                        .paperNo(paperNo)
                        .predictedValue(predictions.getPredictedValue())
                        .precision(predictions.getPrecision())
                        .leftPos(predictions.getLeftPos())
                        .upperPos(predictions.getUpperPos())
                        .rightPos(predictions.getRightPos())
                        .lowerPos(predictions.getLowerPos())
                        .encode(predictions.getEncode())
                        .groupId(groupId)
                        .filePath(processedFilePaths)
                        .tenantId(tenantId)
                        .processId(processId)
                        .rootPipelineId(rootPipelineId)
                        .process(entity.getProcess())
                        .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .message("face detection macro completed")
                        .build()
        );
    }


    private void tritonRequestBuilder(FaceDetectionQueryInputTable entity, Request request, List<FaceDetectionQueryOutputTable> parentObj) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();


        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                FaceDetectionModelResponse modelResponse = mapper.readValue(responseBody, FaceDetectionModelResponse.class);

                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(faceDetectionDataItem -> {
                        try {
                            extractTritonOutputDataResponse(entity, faceDetectionDataItem, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion());
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));

                }

            } else {
                parentObj.add(FaceDetectionQueryOutputTable.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .filePath(entity.getInputFilePath())
                        .tenantId(tenantId)
                        .processId(processId)
                        .rootPipelineId(rootPipelineId)
                        .process(entity.getProcess())
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .message(response.message())
                        .build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (IOException e) {
            parentObj.add(FaceDetectionQueryOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .filePath(entity.getInputFilePath())
                    .tenantId(tenantId)
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .process(entity.getProcess())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .build());

            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("face detection consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }


    }

    private void extractTritonOutputDataResponse(FaceDetectionQueryInputTable entity, String faceDetectionDataItem, List<FaceDetectionQueryOutputTable> parentObj, String modelName, String modelVersion) throws JsonProcessingException
    {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();


        try {
            List<FaceDetectionExtractionPrediction> detectionDataItem = mapper.readValue(faceDetectionDataItem, new TypeReference<>() {
            });
            detectionDataItem.forEach(faceDetectionExtractionResponse -> {
                try {
                    String faceDetectionExtractionResponseStr = mapper.writeValueAsString(faceDetectionExtractionResponse.getEncode());
                    parentObj.add(new FaceDetectionQueryOutputTable().builder()
                            .originId(originId)
                            .paperNo(paperNo)
                            .predictedValue(faceDetectionExtractionResponse.getPredictedValue())
                            .precision(faceDetectionExtractionResponse.getPrecision())
                            .leftPos(faceDetectionExtractionResponse.getLeftPos())
                            .upperPos(faceDetectionExtractionResponse.getUpperPos())
                            .rightPos(faceDetectionExtractionResponse.getRightPos())
                            .lowerPos(faceDetectionExtractionResponse.getLowerPos())
                            .encode(faceDetectionExtractionResponseStr)
                            .groupId(groupId)
                            .filePath(processedFilePaths)
                            .tenantId(tenantId)
                            .processId(processId)
                            .rootPipelineId(rootPipelineId)
                            .process(entity.getProcess())
                            .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                            .stage(PROCESS_NAME)
                            .message("face detection macro completed")
                            .modelName(modelName)
                            .modelVersion(modelVersion)
                            .build());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (JsonProcessingException e) {
            parentObj.add(FaceDetectionQueryOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .filePath(processedFilePaths)
                    .tenantId(tenantId)
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .process(entity.getProcess())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .modelName(modelName)
                    .modelVersion(modelVersion)
                    .build());
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("face detection consumer failed for batch/group " + groupId, handymanException, this.action);

        }
    }
}