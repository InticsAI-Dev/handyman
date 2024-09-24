package in.handyman.raven.lib.model.bulletin.detection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.BulletInExtractionAction;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.bulletin.detection.triton.BulletinExtractionModelResponse;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class BulletinExtractionConsumerProcess implements CoproProcessor.ConsumerProcess<BulletinQueryInputTable, BulletinQueryOutputTable> {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.bulletin.extraction.activator";
    public static final String PROCESS_NAME = PipelineName.BULLETIN_EXTRACTION.getProcessName();
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private final String outputDir;

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final BulletInExtractionAction aAction;

    private final int timeOut;

    public BulletinExtractionConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, String outputDir, BulletInExtractionAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.outputDir = outputDir;
        this.aAction = aAction;
        this.timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder().connectTimeout(this.timeOut, TimeUnit.MINUTES).writeTimeout(this.timeOut, TimeUnit.MINUTES).readTimeout(this.timeOut, TimeUnit.MINUTES).build();
    }


    @Override
    public List<BulletinQueryOutputTable> process(URL endpoint, BulletinQueryInputTable entity) throws Exception {
        List<BulletinQueryOutputTable> parentObj = new ArrayList<>();
        String entityFilePath = entity.getFilePath();

        String filePath = entity.getFilePath();
        Long actionId = action.getActionId();


        //payload
        BulletinExtractionRequest bulletinExtractionRequest = new BulletinExtractionRequest();


        bulletinExtractionRequest.setRootPipelineId(action.getRootPipelineId());
        bulletinExtractionRequest.setActionId(actionId);
        bulletinExtractionRequest.setProcess(PROCESS_NAME);
        bulletinExtractionRequest.setInput(filePath);
        bulletinExtractionRequest.setTask("Bulletin");
        bulletinExtractionRequest.setOutputDir(entity.getOutputDir());
        List<BulletinExtractionLineItems> bulletinExtractionRequestLineItem=new ArrayList<>();
        BulletinExtractionLineItems bulletinExtractionLineItems1=new BulletinExtractionLineItems();
        bulletinExtractionLineItems1.setPrompt(entity.getPrompt());
        bulletinExtractionLineItems1.setSectionHeader(entity.getSectionHeader());

        bulletinExtractionRequestLineItem.add(bulletinExtractionLineItems1);

        bulletinExtractionRequest.setPrompt(bulletinExtractionRequestLineItem);

        String jsonInputRequest = mapper.writeValueAsString(bulletinExtractionRequest);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("BULLETIN EXTRACTION START");
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
            log.info("Triton request activator variable: {} value: {}, Copro API running in legacy mode and request {}", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator,jsonInputRequest);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
            coproResponseBuider(entity, request, parentObj, jsonInputRequest, endpoint);
        } else {
            log.info("Triton request activator variable: {} value: {}, Copro API running in Triton mode and request {}", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator,jsonRequest);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();
            tritonRequestBuilder(entity, request, parentObj, jsonRequest, endpoint);
        }


        return parentObj;
    }


    private void coproResponseBuider(BulletinQueryInputTable entity, Request request, List<BulletinQueryOutputTable> parentObj, String jsonInputRequest, URL endpoint) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                extractedCoproOutputResponse(entity, responseBody, parentObj, "", "", jsonInputRequest, responseBody, endpoint.toString());
            } else {
                parentObj.add(BulletinQueryOutputTable.builder()
                        .synonymId(entity.getSynonymId())
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .groupId(groupId)
                        .processId(processId)
                        .tenantId(tenantId)
                        .paperNo(paperNo)
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .message(response.message())
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .rootPipelineId(rootPipelineId)
                        .process(entity.getProcess())
                        .processId(entity.getProcessId())
                        .outputDir(entity.getOutputDir())
                        .request(jsonInputRequest)
                        .response(response.message())
                        .endpoint(String.valueOf(endpoint))
                        .build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(BulletinQueryOutputTable.builder()
                    .synonymId(entity.getSynonymId())
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .processId(processId)
                    .tenantId(tenantId)
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .request(jsonInputRequest)
                    .response("Error in response")
                    .endpoint(String.valueOf(endpoint))
                    .build());
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("bulletin detection consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }
    }

    private void tritonRequestBuilder(BulletinQueryInputTable entity, Request request, List<BulletinQueryOutputTable> parentObj, String jsonRequest, URL endpoint) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                BulletinExtractionModelResponse modelResponse = mapper.readValue(responseBody, BulletinExtractionModelResponse.class);

                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(bulletinDataItem -> {
                        extractTritonOutputDataResponse(entity, bulletinDataItem, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion(), jsonRequest, responseBody, endpoint.toString());
                    }));

                }

            } else {
                parentObj.add(BulletinQueryOutputTable.builder()
                        .synonymId(entity.getSynonymId())
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .groupId(groupId)
                        .processId(processId)
                        .tenantId(tenantId)
                        .paperNo(paperNo)
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .message(response.message())
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .rootPipelineId(rootPipelineId)
                        .request(jsonRequest)
                        .response(response.message())
                        .endpoint(String.valueOf(endpoint))
                        .build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(BulletinQueryOutputTable.builder()
                    .synonymId(entity.getSynonymId())
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .processId(processId)
                    .tenantId(tenantId)
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .request(jsonRequest)
                    .response("Error in response")
                    .endpoint(String.valueOf(endpoint))
                    .build());
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("bulletin detection consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }
    }

    private void extractTritonOutputDataResponse(BulletinQueryInputTable entity, String bulletinResponseDataItem, List<BulletinQueryOutputTable> parentObj, String modelName, String modelVersion, String request, String response, String endpoint) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getFilePath();
        String originId = entity.getOriginId();
        try {
            List<BulletinExtractionResponse> detectionDataItem = mapper.readValue(bulletinResponseDataItem, new TypeReference<>() {
            });
            detectionDataItem.forEach(bulletinExtractionResponse -> {
                try {
                    String bulletInLineItemsNode = mapper.writeValueAsString(bulletinExtractionResponse);
                    parentObj.add(BulletinQueryOutputTable.builder()
                            .synonymId(entity.getSynonymId())
                            .filePath(processedFilePaths)
                            .originId(originId)
                            .groupId(groupId)
                            .processId(processId)
                            .tenantId(tenantId)
                            .paperNo(paperNo)
                            .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                            .stage(PROCESS_NAME)
                            .message("bulletin Extraction macro completed")
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .rootPipelineId(rootPipelineId)
                            .bulletinSection(bulletinExtractionResponse.getSectionHeader())
                            .bulletinPoints(bulletInLineItemsNode)
                            .modelName(modelName)
                            .modelVersion(modelVersion)
                            .request(request)
                            .response(response)
                            .endpoint(endpoint)
                            .build());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }



            });


        } catch (JsonProcessingException e) {
            parentObj.add(BulletinQueryOutputTable.builder()
                    .synonymId(entity.getSynonymId())
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .processId(processId)
                    .tenantId(tenantId)
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .request(request)
                    .response(response)
                    .endpoint(endpoint)
                    .build());
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("bulletin Extraction consumer failed for batch/group " + groupId, handymanException, this.action);
        }
    }


    private void extractedCoproOutputResponse(BulletinQueryInputTable entity, String bulletinResponseDataItem, List<BulletinQueryOutputTable> parentObj, String modelName, String modelVersion, String request, String response, String endpoint) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getFilePath();
        String originId = entity.getOriginId();
        try {
            List<BulletinExtractionResponse> detectionDataItem = mapper.readValue(bulletinResponseDataItem, new TypeReference<>() {
            });
            detectionDataItem.forEach(bulletinExtractionResponse -> {
                try {
                    String bulletInLineItemsNode = mapper.writeValueAsString(bulletinExtractionResponse.getSectionPoints());
                    parentObj.add(BulletinQueryOutputTable.builder()
                            .synonymId(entity.getSynonymId())
                            .filePath(processedFilePaths)
                            .originId(originId)
                            .groupId(groupId)
                            .processId(processId)
                            .tenantId(tenantId)
                            .paperNo(paperNo)
                            .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                            .stage(PROCESS_NAME)
                            .message("bulletin Extraction macro completed")
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .rootPipelineId(rootPipelineId)
                            .bulletinSection(bulletinExtractionResponse.getSectionHeader())
                            .bulletinPoints(bulletInLineItemsNode)
                            .modelName(modelName)
                            .modelVersion(modelVersion)
                            .request(request)
                            .response(response)
                            .endpoint(endpoint)
                            .build());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }



            });

        } catch (JsonProcessingException e) {
            parentObj.add(BulletinQueryOutputTable.builder()
                    .synonymId(entity.getSynonymId())
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .processId(processId)
                    .tenantId(tenantId)
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .request(request)
                    .response(response)
                    .endpoint(endpoint)
                    .build());
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("bulletin Extraction consumer failed for batch/group " + groupId, handymanException, this.action);
        }
    }


}
