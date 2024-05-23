package in.handyman.raven.lib.model.figure.detection;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.lib.FigureDetectionAction;
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


public class FigureDetectionConsumerProcess implements CoproProcessor.ConsumerProcess<FigureDetectionQueryInputTable, FigureDetectionQueryOutputTable> {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.figure.detection.activator";
    public static final String PROCESS_NAME = PipelineName.FIGURE_DETECTION.getProcessName();
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private final String outputDir;

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final FigureDetectionAction aAction;
    private final int timeOut;

    public FigureDetectionConsumerProcess(Logger log, Marker aMarker, String outputDir, ActionExecutionAudit action, FigureDetectionAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.outputDir = outputDir;
        this.action = action;
        this.aAction = aAction;
        this.timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder().connectTimeout(this.timeOut, TimeUnit.MINUTES).writeTimeout(this.timeOut, TimeUnit.MINUTES).readTimeout(this.timeOut, TimeUnit.MINUTES).build();
    }

    @Override
    public List<FigureDetectionQueryOutputTable> process(URL endpoint, FigureDetectionQueryInputTable entity) throws Exception {
        List<FigureDetectionQueryOutputTable> parentObj = new ArrayList<>();
        String entityFilePath = entity.getInputFilePath();
        String filePath = String.valueOf(entity.getInputFilePath());
        Float threshold = entity.getThreshold();


        RequestBody emptyBody = RequestBody.create(new byte[0]);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("FIGURE DETECTION START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
//        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = mapper.writeValueAsString(tritonInputRequest);


        log.info(aMarker, " Input variables id : {}", action.getActionId());


        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} and threshold {}", endpoint, entityFilePath, threshold);
        }
        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);


        if (Objects.equals("false", tritonRequestActivator)) {
            log.info("Triton request activator variable: {} value: {}", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator);
            Request urlRequest = new Request.Builder().url(endpoint + "?inputFilePath=" + filePath + "&threshold=" + threshold)
                    .post(emptyBody)
                    .build();
            coproResponseBuilder(entity, urlRequest, parentObj);
        } else {
            log.info("Triton request activator variable: {} value: {}, Copro API running in Triton mode  and json input {} ", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator, jsonRequest);
            Request urlRequest = new Request.Builder().url(endpoint + "?inputFilePath=" + filePath + "&threshold=" + threshold)
                    .post(emptyBody)
                    .build();
            tritonRequestBuilder(entity, urlRequest, parentObj);
        }

        return parentObj;
    }

    private void tritonRequestBuilder(FigureDetectionQueryInputTable entity, Request request, List<FigureDetectionQueryOutputTable> parentObj) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                List<FigureDetectionExtractionEncode> modelResponse = mapper.readValue(responseBody, new TypeReference<>() {
                });
                if (modelResponse  != null && !modelResponse.isEmpty()) {
                    extractTritonOutputDataResponse(entity, modelResponse, parentObj);
                }
            } else {
                parentObj.add(FigureDetectionQueryOutputTable.builder()
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
                        .batchId(entity.getBatchId())
                        .build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (IOException e) {
            parentObj.add(FigureDetectionQueryOutputTable.builder()
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
                    .batchId(entity.getBatchId())
                    .build());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Figure detection consumer failed in batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response from triton server {}", ExceptionUtil.toString(e));
        }
    }

    private void extractTritonOutputDataResponse(FigureDetectionQueryInputTable entity, List<FigureDetectionExtractionEncode> modelResponse, List<FigureDetectionQueryOutputTable> parentObj) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();

        modelResponse.forEach(figureExtractionResponse -> {
            parentObj.add(FigureDetectionQueryOutputTable.builder()
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .createdUserId(tenantId)
                    .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                    .lastUpdatedUserId(tenantId)
                    .originId(originId)
                    .paperNo(paperNo)
                    .encode(figureExtractionResponse.getEncode())
                    .groupId(groupId)
                    .filePath(processedFilePaths)
                    .tenantId(tenantId)
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .process(entity.getProcess())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message("face detection macro completed")
                    .batchId(entity.getBatchId())
                    .build()
            );

        });
    }

    private void coproResponseBuilder(FigureDetectionQueryInputTable entity, Request request, List<FigureDetectionQueryOutputTable> parentObj) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                List<FigureDetectionExtractionEncode> modelResponse = mapper.readValue(responseBody, new TypeReference<>() {
                });
                if (modelResponse  != null && !modelResponse.isEmpty()) {
                        extractedCoproOutputResponse(entity, modelResponse, parentObj);
                }
            } else {
                parentObj.add(FigureDetectionQueryOutputTable.builder()
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
                        .batchId(entity.getBatchId())
                        .build());
                log.info(aMarker, "Error in converting response from copro server {}", response.message());
            }
        } catch (IOException e) {
            parentObj.add(FigureDetectionQueryOutputTable.builder()
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
                    .batchId(entity.getBatchId())
                    .build());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Figure detection consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response from copro server {}", ExceptionUtil.toString(e));
        }
    }

    private void extractedCoproOutputResponse(FigureDetectionQueryInputTable entity, List<FigureDetectionExtractionEncode> figureDataItem, List<FigureDetectionQueryOutputTable> parentObj) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();

        figureDataItem.forEach(figureExtractionResponse -> {
            parentObj.add(FigureDetectionQueryOutputTable.builder()
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .createdUserId(tenantId)
                    .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                    .lastUpdatedUserId(tenantId)
                    .originId(originId)
                    .paperNo(paperNo)
                    .encode(figureExtractionResponse.getEncode())
                    .groupId(groupId)
                    .filePath(processedFilePaths)
                    .tenantId(tenantId)
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .process(entity.getProcess())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .batchId(entity.getBatchId())
                    .message("face detection macro completed")
                    .build()
                );

            });
        }
    }
