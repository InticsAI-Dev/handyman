package in.handyman.raven.lib.model.currency.detection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.CurrencyDetectionAction;
import in.handyman.raven.lib.model.currency.detection.triton.CurrencyDetectionModelResponse;
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

public class CurrencyDetectionConsumerProcess implements CoproProcessor.ConsumerProcess<CurrencyDetectionInputQuerySet, CurrencyDetectionOutputQuerySet> {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.currency.detection.activator";
    public static final String PROCESS_NAME = PipelineName.CURRENCY_DETECTION.getProcessName();

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private final String outputDir;

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final CurrencyDetectionAction aAction;

    private final int timeOut;

    public CurrencyDetectionConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, String outputDir, CurrencyDetectionAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.outputDir = outputDir;
        this.aAction = aAction;
        this.timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder().connectTimeout(this.timeOut, TimeUnit.MINUTES).writeTimeout(this.timeOut, TimeUnit.MINUTES).readTimeout(this.timeOut, TimeUnit.MINUTES).build();
    }


    @Override
    public List<CurrencyDetectionOutputQuerySet> process(URL endpoint, CurrencyDetectionInputQuerySet entity) throws Exception {


        List<CurrencyDetectionOutputQuerySet> parentObj = new ArrayList<>();
        String entityFilePath = entity.getFilePath();
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        String rootPipelineId = String.valueOf(entity.getRootPipelineId());
        Integer paperNo = entity.getPaperNo();
        String filePath = String.valueOf(entity.getFilePath());
        Long actionId = action.getActionId();


        //payload
        CurrencyDetectionRequest currencyDetectionRequest = new CurrencyDetectionRequest();


        currencyDetectionRequest.setRootPipelineId(Long.valueOf(rootPipelineId));
        currencyDetectionRequest.setActionId(actionId);
        currencyDetectionRequest.setProcess(PROCESS_NAME);
        currencyDetectionRequest.setInputFilePath(filePath);


        String jsonInputRequest = mapper.writeValueAsString(currencyDetectionRequest);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("CURRENCY DETECTION START");
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
            log.info("Triton request activator variable: {} value: {}, Copro API running in legacy mode", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
            coproResponseBuider(entity, request, parentObj);
        } else {
            log.info("Triton request activator variable: {} value: {}, Copro API running in Triton mode", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();
            tritonRequestBuilder(entity, request, parentObj);
        }


        return parentObj;
    }


    private void coproResponseBuider(CurrencyDetectionInputQuerySet entity, Request request, List<CurrencyDetectionOutputQuerySet> parentObj) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                extractedCoproOutputResponse(entity, responseBody, parentObj, "", "");
            } else {
                parentObj.add(CurrencyDetectionOutputQuerySet.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .groupId(groupId)
                        .processId(processId)
                        .tenantId(tenantId)
                        .templateId(templateId)
                        .paperNo(paperNo)
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME)
                        .message(response.message())
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .rootPipelineId(rootPipelineId)
                                .batchId(entity.getBatchId())
                        .build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(CurrencyDetectionOutputQuerySet.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .processId(processId)
                    .tenantId(tenantId)
                    .templateId(templateId)
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e)).createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .batchId(entity.getBatchId())
                    .build());
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Currency detection consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }
    }

    private void tritonRequestBuilder(CurrencyDetectionInputQuerySet entity, Request request, List<CurrencyDetectionOutputQuerySet> parentObj) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                CurrencyDetectionModelResponse modelResponse = mapper.readValue(responseBody, CurrencyDetectionModelResponse.class);

                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(currencyDetectionDataItem -> {
                        extractTritonOutputDataResponse(entity, currencyDetectionDataItem, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion());
                    }));

                }

            } else {
                parentObj.add(CurrencyDetectionOutputQuerySet.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .groupId(groupId)
                        .processId(processId)
                        .tenantId(tenantId)
                        .templateId(templateId)
                        .paperNo(paperNo)
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .message(response.message())
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .rootPipelineId(rootPipelineId)
                        .batchId(entity.getBatchId())
                        .build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(CurrencyDetectionOutputQuerySet.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .processId(processId)
                    .tenantId(tenantId)
                    .templateId(templateId)
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .batchId(entity.getBatchId())
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId).build());
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Currency detection consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }
    }

    private void extractTritonOutputDataResponse(CurrencyDetectionInputQuerySet entity, String currencyDetectionDataItem, List<CurrencyDetectionOutputQuerySet> parentObj, String modelName, String modelVersion) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getFilePath();
        String originId = entity.getOriginId();
        try {
            List<CurrencyDetectionDataItem> detectionDataItem = mapper.readValue(currencyDetectionDataItem, new TypeReference<>() {
            });
            detectionDataItem.forEach(currencyDetectionDataItem1 -> parentObj.add(CurrencyDetectionOutputQuerySet.builder()
                    .inputFilePath(processedFilePaths)
                    .originId(originId)
                    .groupId(groupId)
                    .processId(processId)
                    .detectedValue(currencyDetectionDataItem1.getClassValue())
                    .detectedAsciiValue(currencyDetectionDataItem1.getAsciiValue())
                    .tenantId(tenantId)
                    .templateId(templateId)
                    .confidenceScore(currencyDetectionDataItem1.getConfidenceScore())
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message("Currency detection macro completed")
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .modelName(modelName)
                    .modelVersion(modelVersion)
                    .batchId(entity.getBatchId())
                    .build()));
        } catch (JsonProcessingException e) {

            parentObj.add(CurrencyDetectionOutputQuerySet.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .processId(processId)
                    .tenantId(tenantId)
                    .templateId(templateId)
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME).message(ExceptionUtil.toString(e))
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .batchId(entity.getBatchId())
                    .build());
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Currency detection consumer failed for batch/group " + groupId, handymanException, this.action);
        }
    }

    private void extractedCoproOutputResponse(CurrencyDetectionInputQuerySet entity, String currencyDetectionDataItem, List<CurrencyDetectionOutputQuerySet> parentObj, String modelName, String modelVersion) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getFilePath();
        String originId = entity.getOriginId();
        try {
            List<CurrencyDetectionDataItem> detectionDataItem = mapper.readValue(currencyDetectionDataItem, new TypeReference<>() {
            });
            detectionDataItem.forEach(currencyDetectionDataItem1 -> {
                parentObj.add(CurrencyDetectionOutputQuerySet.builder()
                        .inputFilePath(processedFilePaths)
                        .originId(originId)
                        .groupId(groupId)
                        .processId(processId)
                        .detectedValue(currencyDetectionDataItem1.getClassValue())
                        .detectedAsciiValue(currencyDetectionDataItem1.getAsciiValue())
                        .tenantId(tenantId)
                        .templateId(templateId)
                        .confidenceScore(currencyDetectionDataItem1.getConfidenceScore())
                        .paperNo(paperNo)
                        .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .message("Currency detection macro completed")
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .rootPipelineId(rootPipelineId)
                        .modelName(modelName)
                        .modelVersion(modelVersion)
                        .batchId(entity.getBatchId())
                        .build());
            });

        } catch (JsonProcessingException e) {
            parentObj.add(CurrencyDetectionOutputQuerySet.builder().originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).tenantId(tenantId).templateId(templateId).paperNo(paperNo).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).message(ExceptionUtil.toString(e)).createdOn(Timestamp.valueOf(LocalDateTime.now())).rootPipelineId(rootPipelineId).build());
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Currency detection consumer failed for batch/group " + groupId, handymanException, this.action);
        }
    }


}
