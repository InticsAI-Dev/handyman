package in.handyman.raven.lib.model.paragraph.detection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.ParagraphExtractionAction;
import in.handyman.raven.lib.model.bulletin.detection.BulletinExtractionLineItems;
import in.handyman.raven.lib.model.paragraph.detection.triton.ParagraphExtractionModelResponse;
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


public class ParagraphExtractionConsumerProcess implements CoproProcessor.ConsumerProcess<ParagraphQueryInputTable, ParagraphQueryOutputTable> {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.paragraph.extraction.activator";
    public static final String PROCESS_NAME = PipelineName.PARAGRAPH_EXTRACTION.getProcessName();
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private final String outputDir;

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final ParagraphExtractionAction aAction;

    private final int timeOut;

    public ParagraphExtractionConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, String outputDir, ParagraphExtractionAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.outputDir = outputDir;
        this.aAction = aAction;
        this.timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder().connectTimeout(this.timeOut, TimeUnit.MINUTES).writeTimeout(this.timeOut, TimeUnit.MINUTES).readTimeout(this.timeOut, TimeUnit.MINUTES).build();
    }


    @Override
    public List<ParagraphQueryOutputTable> process(URL endpoint, ParagraphQueryInputTable entity) throws Exception {
        List<ParagraphQueryOutputTable> parentObj = new ArrayList<>();
        String entityFilePath = entity.getFilePath();
        String rootPipelineId = String.valueOf(entity.getRootPipelineId());
        String filePath = String.valueOf(entity.getFilePath());
        Long actionId = action.getActionId();


        //payload
        ParagraphExtractionRequest paragraphExtractionRequest = new ParagraphExtractionRequest();


        paragraphExtractionRequest.setRootPipelineId(Long.valueOf(rootPipelineId));
        paragraphExtractionRequest.setActionId(actionId);
        paragraphExtractionRequest.setProcess(PROCESS_NAME);
        paragraphExtractionRequest.setFilePath(filePath);
        paragraphExtractionRequest.setOutputDir(entity.getOutputDir());

        List<ParagraphExtractionLineItems> paragraphExtractionLineItems=new ArrayList<>();
        ParagraphExtractionLineItems  paragraphExtractionLineItems1=new ParagraphExtractionLineItems();
        paragraphExtractionLineItems1.setSectionHeader(entity.getSectionHeader());
        paragraphExtractionLineItems1.setPrompt(entity.getPrompt());

        paragraphExtractionLineItems.add(paragraphExtractionLineItems1);

        paragraphExtractionRequest.setPrompt(paragraphExtractionLineItems);

        String jsonInputRequest = mapper.writeValueAsString(paragraphExtractionRequest);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("PARAGRAPH EXTRACTION START");
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
            log.info("Triton request activator variable: {} value: {}, Copro API running in legacy mode and json input {}", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator,jsonInputRequest);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
            coproResponseBuider(entity, request, parentObj);
        } else {
            log.info("Triton request activator variable: {} value: {}, Copro API running in Triton mode  and json input {} ", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator,jsonRequest);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();
            tritonRequestBuilder(entity, request, parentObj);
        }


        return parentObj;
    }


    private void coproResponseBuider(ParagraphQueryInputTable entity, Request request, List<ParagraphQueryOutputTable> parentObj) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                extractedCoproOutputResponse(entity, responseBody, parentObj, "", "");
            } else {
                parentObj.add(ParagraphQueryOutputTable.builder()
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
                        .build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(ParagraphQueryOutputTable.builder()
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
                    .build());
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("paragraph detection consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }
    }

    private void tritonRequestBuilder(ParagraphQueryInputTable entity, Request request, List<ParagraphQueryOutputTable> parentObj) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                ParagraphExtractionModelResponse modelResponse = mapper.readValue(responseBody, ParagraphExtractionModelResponse.class);

                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(paragraphDataItem -> {
                        extractTritonOutputDataResponse(entity, paragraphDataItem, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion());
                    }));

                }

            } else {
                parentObj.add(ParagraphQueryOutputTable.builder()
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
                        .build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(ParagraphQueryOutputTable.builder()
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
                    .build());
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("paragraph detection consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }
    }

    private void extractTritonOutputDataResponse(ParagraphQueryInputTable entity, String paragraphResponseDataItem, List<ParagraphQueryOutputTable> parentObj, String modelName, String modelVersion) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getFilePath();
        String originId = entity.getOriginId();
        try {
            List<ParagraphExtractionResponse> detectionDataItem = mapper.readValue(paragraphResponseDataItem, new TypeReference<>() {
            });
            detectionDataItem.forEach(paragraphExtractionResponse -> {
                try {
                    String paragraphExtractionResponseStr=mapper.writeValueAsString(paragraphExtractionResponse.getSectionPoints());
                    parentObj.add(ParagraphQueryOutputTable.builder()
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
                            .modelName(modelName)
                            .modelVersion(modelVersion)
                            .paragraphSection(paragraphExtractionResponse.getSectionHeader())
                            .paragraphPoints(paragraphExtractionResponseStr)
                            .process(entity.getProcess())
                            .processId(entity.getProcessId())
                            .outputDir(entity.getOutputDir())
                            .build());


                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            });

        } catch (JsonProcessingException e) {
            parentObj.add(ParagraphQueryOutputTable.builder()
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
                    .build());
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("paragraph Extraction consumer failed for batch/group " + groupId, handymanException, this.action);
        }
    }


    private void extractedCoproOutputResponse(ParagraphQueryInputTable entity, String paragraphResponseDataItem, List<ParagraphQueryOutputTable> parentObj, String modelName, String modelVersion) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getFilePath();
        String originId = entity.getOriginId();
        try {
            List<ParagraphExtractionResponse> detectionDataItem = mapper.readValue(paragraphResponseDataItem, new TypeReference<>() {
            });
            detectionDataItem.forEach(paragraphExtractionResponse -> {
                try {
                    String paragraphExtractionResponseStr=mapper.writeValueAsString(paragraphExtractionResponse.getSectionPoints());
                    parentObj.add(ParagraphQueryOutputTable.builder()
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
                            .modelName(modelName)
                            .modelVersion(modelVersion)
                            .paragraphSection(paragraphExtractionResponse.getSectionHeader())
                            .paragraphPoints(paragraphExtractionResponseStr)
                            .process(entity.getProcess())
                            .processId(entity.getProcessId())
                            .outputDir(entity.getOutputDir())
                            .build());


                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            });


        } catch (JsonProcessingException e) {
            parentObj.add(ParagraphQueryOutputTable.builder()
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
                    .build());
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("paragraph Extraction consumer failed for batch/group " + groupId, handymanException, this.action);
        }
    }


}
