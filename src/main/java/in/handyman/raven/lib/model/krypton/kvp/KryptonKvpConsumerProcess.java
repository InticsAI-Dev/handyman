package in.handyman.raven.lib.model.krypton.kvp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.KryptonKvpAction;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
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

public class KryptonKvpConsumerProcess implements CoproProcessor.ConsumerProcess<KryptonQueryInputTable, KryptonQueryOutputTable>  {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.krypton.kvp.activator";
    public static final String PROCESS_NAME = PipelineName.KRYPTON_KVP_ACTION.getProcessName();
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;

    public KryptonKvpConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action,  KryptonKvpAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        int timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder().connectTimeout(timeOut, TimeUnit.MINUTES).writeTimeout(timeOut, TimeUnit.MINUTES).readTimeout(timeOut, TimeUnit.MINUTES).build();
    }



    @Override
    public List<KryptonQueryOutputTable> process(URL endpoint, KryptonQueryInputTable entity) throws Exception {
        List<KryptonQueryOutputTable> parentObj = new ArrayList<>();
        String rootPipelineId = String.valueOf(entity.getRootPipelineId());
        String filePath = String.valueOf(entity.getInputFilePath());
        Long actionId = action.getActionId();
        Long groupId = entity.getGroupId();
        String prompt = entity.getPrompt();
        String textModel = entity.getTextModel();
        String paperType = entity.getPaperType();
        String modelRegistry =entity.getModelRegistry();
        Integer paperNo = entity.getPaperNo();
        String originId = entity.getOriginId();
        Long processId = entity.getProcessId();
        Long tenantId  = entity.getTenantId();
        String responseFormat = entity.getResponseFormat();



        //payload
        KryptonKvpExtractionRequest kryptonKvpExtractionRequest = new KryptonKvpExtractionRequest();


        kryptonKvpExtractionRequest.setRootPipelineId(Long.valueOf(rootPipelineId));
        kryptonKvpExtractionRequest.setActionId(actionId);
        kryptonKvpExtractionRequest.setProcess(PROCESS_NAME);
        kryptonKvpExtractionRequest.setInputFilePath(filePath);
        kryptonKvpExtractionRequest.setGroupId(groupId);
        kryptonKvpExtractionRequest.setPrompt(prompt);
        kryptonKvpExtractionRequest.setProcessId(processId);
        kryptonKvpExtractionRequest.setModelRegistry(modelRegistry);
        kryptonKvpExtractionRequest.setPaperNo(paperNo);
        kryptonKvpExtractionRequest.setTextModel(textModel);
        kryptonKvpExtractionRequest.setTenantId(tenantId);
        kryptonKvpExtractionRequest.setResponseFormat(responseFormat);
        kryptonKvpExtractionRequest.setOriginId(originId);
        kryptonKvpExtractionRequest.setPaperType(paperType);


        String jsonInputRequest = mapper.writeValueAsString(kryptonKvpExtractionRequest);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("KRYPTON MODEL START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = mapper.writeValueAsString(tritonInputRequest);


        log.info(aMarker, " Input variables id : {}", action.getActionId());


        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} and prompt {}", endpoint,filePath, prompt);
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

    private void tritonRequestBuilder(KryptonQueryInputTable entity, Request request, List<KryptonQueryOutputTable> parentObj) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();


        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                String responseBody = response.body().string();
                KryptonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, KryptonKvpExtractionResponse.class);
                if (modelResponse.getOutputs()  != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(kryptonDataItem -> {
                        try {
                            extractTritonOutputDataResponse(entity, kryptonDataItem, parentObj);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));

                }
            } else {
                parentObj.add(KryptonQueryOutputTable.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .inputFilePath(entity.getInputFilePath())
                        .tenantId(tenantId)
                        .actionId(action.getActionId())
                        .processId(processId)
                        .rootPipelineId(rootPipelineId)
                        .process(entity.getProcess())
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .message(response.message())
                        .batchId(entity.getBatchId())
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .createdUserId(tenantId)
                        .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                        .lastUpdatedUserId(tenantId)
                        .build());
                log.info(aMarker, "Error in getting response from triton response {}", response.message());
            }
        } catch (IOException e) {
            parentObj.add(KryptonQueryOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .inputFilePath(entity.getInputFilePath())
                    .tenantId(tenantId)
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .actionId(action.getActionId())
                    .process(entity.getProcess())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .batchId(entity.getBatchId())
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .createdUserId(tenantId)
                    .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                    .lastUpdatedUserId(tenantId)
                    .build());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("krypton kvp consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response  from triton server {}", ExceptionUtil.toString(e));
        }

    }

    private void extractTritonOutputDataResponse(KryptonQueryInputTable entity, String kryptonDataItem, List<KryptonQueryOutputTable> parentObj) throws JsonProcessingException {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();
        KryptonKvpLineItem modelResponse = mapper.readValue(kryptonDataItem, KryptonKvpLineItem.class);


        parentObj.add(KryptonQueryOutputTable.builder()
                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                .createdUserId(tenantId)
                .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                .lastUpdatedUserId(tenantId)
                .originId(originId)
                .paperNo(paperNo)
                .totalResponseJson(mapper.writeValueAsString(modelResponse.getResponse()))
                .groupId(groupId)
                .inputFilePath(processedFilePaths)
                .actionId(action.getActionId())
                .tenantId(tenantId)
                .processId(processId)
                .rootPipelineId(rootPipelineId)
                .process(entity.getProcess())
                .imageDPI(modelResponse.getImageDPI())
                .imageWidth(modelResponse.getImageWidth())
                .extractedImageUnit(modelResponse.getExtractedImageUnit())
                .imageHeight(modelResponse.getImageHeight())
                .responseFormat(modelResponse.getResponseFormat())
                .textModel(modelResponse.getTextModel())
                .batchId(entity.getBatchId())
                .modelRegistry(entity.getModelRegistry())
                .paperType(entity.getPaperType())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(PROCESS_NAME)
                .batchId(entity.getBatchId())
                .message("krypton kvp action macro completed")
                .build()
        );

    }


    private void coproResponseBuider(KryptonQueryInputTable entity, Request request, List<KryptonQueryOutputTable> parentObj) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                KryptonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, KryptonKvpExtractionResponse.class);
                if (modelResponse.getOutputs()  != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(kryptonDataItem -> {
                        try {
                            extractedCoproOutputResponse(entity, kryptonDataItem, parentObj);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));

                }
            } else {
                parentObj.add(KryptonQueryOutputTable.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .inputFilePath(entity.getInputFilePath())
                        .actionId(action.getActionId())
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
            parentObj.add(KryptonQueryOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .inputFilePath(entity.getInputFilePath())
                    .tenantId(tenantId)
                    .actionId(action.getActionId())
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .process(entity.getProcess())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .batchId(entity.getBatchId())
                    .build());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("krypton kvp action consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response from copro server {}", ExceptionUtil.toString(e));
        }
    }

    private void extractedCoproOutputResponse(KryptonQueryInputTable entity, String kryptonDataItem, List<KryptonQueryOutputTable> parentObj) throws JsonProcessingException {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();

        KryptonKvpLineItem modelResponse = mapper.readValue(kryptonDataItem, KryptonKvpLineItem.class);

        parentObj.add(KryptonQueryOutputTable.builder()
                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                .createdUserId(tenantId)
                .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                .lastUpdatedUserId(tenantId)
                .originId(originId)
                .paperNo(paperNo)
                .totalResponseJson(mapper.writeValueAsString(modelResponse.getResponse()))
                .groupId(groupId)
                .inputFilePath(processedFilePaths)
                .actionId(action.getActionId())
                .tenantId(tenantId)
                .processId(processId)
                .rootPipelineId(rootPipelineId)
                .paperType(entity.getPaperType())
                .modelRegistry(entity.getModelRegistry())
                .process(entity.getProcess())
                .imageDPI(modelResponse.getImageDPI())
                .imageWidth(modelResponse.getImageWidth())
                .extractedImageUnit(modelResponse.getExtractedImageUnit())
                .imageHeight(modelResponse.getImageHeight())
                .responseFormat(modelResponse.getResponseFormat())
                .textModel(modelResponse.getTextModel())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(PROCESS_NAME)
                .batchId(entity.getBatchId())
                .message("krypton kvp action macro completed")
                .build()
        );
    }
}
