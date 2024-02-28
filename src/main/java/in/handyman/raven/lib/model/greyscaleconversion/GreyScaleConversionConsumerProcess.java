package in.handyman.raven.lib.model.greyscaleconversion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.GreyScaleConversionAction;
import in.handyman.raven.lib.model.greyscaleconversion.triton.GreyScaleConversionModelResponse;
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

public class GreyScaleConversionConsumerProcess implements CoproProcessor.ConsumerProcess<GreyScaleConversionInputQuerySet, GreyScaleConversionOutputQuerySet> {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.grey.scale.conversion.request.activator";
    public static final String PROCESS_NAME = PipelineName.GREY_SCALE_CONVERSION.getProcessName();

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private final String outputDir;

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final GreyScaleConversionAction aAction;
    private final int timeOut;

    public GreyScaleConversionConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, String outputDir, GreyScaleConversionAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.outputDir = outputDir;
        this.aAction = aAction;
        this.timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder().connectTimeout(this.timeOut, TimeUnit.MINUTES).writeTimeout(this.timeOut, TimeUnit.MINUTES).readTimeout(this.timeOut, TimeUnit.MINUTES).build();
    }


    @Override
    public List<GreyScaleConversionOutputQuerySet> process(URL endpoint, GreyScaleConversionInputQuerySet entity) throws Exception {


        List<GreyScaleConversionOutputQuerySet> parentObj = new ArrayList<>();
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
        GreyScaleConversionRequest greyScaleConversionRequest = new GreyScaleConversionRequest();
        greyScaleConversionRequest.setProcessId(processId);
        greyScaleConversionRequest.setOriginId(originId);
        greyScaleConversionRequest.setGroupId(groupId);
        greyScaleConversionRequest.setTenantId(tenantId);
        greyScaleConversionRequest.setRootPipelineId(Long.valueOf(rootPipelineId));
        greyScaleConversionRequest.setActionId(actionId);
        greyScaleConversionRequest.setProcess(PROCESS_NAME);
        greyScaleConversionRequest.setInputFilePath(filePath);
        greyScaleConversionRequest.setOutputDir(outputDir);
        greyScaleConversionRequest.setPaperNo(paperNo);
        String jsonInputRequest = mapper.writeValueAsString(greyScaleConversionRequest);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("GREY SCALE CONVERSION START");
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
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
            coproRequestBuider(entity, request, parentObj);
        } else {
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();
            tritonRequestBuilder(entity, request, parentObj);
        }


        return parentObj;
    }

    private void coproRequestBuider(GreyScaleConversionInputQuerySet entity, Request request, List<GreyScaleConversionOutputQuerySet> parentObj) {
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
                parentObj.add(GreyScaleConversionOutputQuerySet.builder().originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).tenantId(tenantId).templateId(templateId).paperNo(paperNo).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).message(response.message()).createdOn(Timestamp.valueOf(LocalDateTime.now())).rootPipelineId(rootPipelineId).build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(GreyScaleConversionOutputQuerySet.builder().originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).tenantId(tenantId).templateId(templateId).paperNo(paperNo).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).message(ExceptionUtil.toString(e)).createdOn(Timestamp.valueOf(LocalDateTime.now())).rootPipelineId(rootPipelineId).build());
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Grey Scale Conversion consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }
    }

    private void tritonRequestBuilder(GreyScaleConversionInputQuerySet entity, Request request, List<GreyScaleConversionOutputQuerySet> parentObj) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                GreyScaleConversionModelResponse modelResponse = mapper.readValue(responseBody, GreyScaleConversionModelResponse.class);

                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(greyScaleConversionDataItem -> {
                        extractTritonOuputDataRequest(entity, greyScaleConversionDataItem, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion());
                    }));

                }

            } else {
                parentObj.add(GreyScaleConversionOutputQuerySet.builder().originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).tenantId(tenantId).templateId(templateId).paperNo(paperNo).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).message(response.message()).createdOn(Timestamp.valueOf(LocalDateTime.now())).rootPipelineId(rootPipelineId).build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(GreyScaleConversionOutputQuerySet.builder().originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).tenantId(tenantId).templateId(templateId).paperNo(paperNo).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).message(ExceptionUtil.toString(e)).createdOn(Timestamp.valueOf(LocalDateTime.now())).rootPipelineId(rootPipelineId).build());
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Grey Scale Conversion consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }
    }

    private void extractTritonOuputDataRequest(GreyScaleConversionInputQuerySet entity, String greyScaleConversionDataitemStr, List<GreyScaleConversionOutputQuerySet> parentObj, String modelName, String modelVersion) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try {
            GreyScaleConversionDataItem greyScaleConversionDataItem = mapper.readValue(greyScaleConversionDataitemStr, GreyScaleConversionDataItem.class);
            parentObj.add(GreyScaleConversionOutputQuerySet.builder()
                    .processedFilePath(greyScaleConversionDataItem.getProcessedFilePath())
                    .originId(greyScaleConversionDataItem.getOriginId())
                    .groupId(greyScaleConversionDataItem.getGroupId())
                    .processId(greyScaleConversionDataItem.getProcessId())
                    .tenantId(greyScaleConversionDataItem.getTenantId())
                    .templateId(templateId)
                    .paperNo(greyScaleConversionDataItem.getPaperNo())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message("Grey Scale Conversion macro completed")
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(greyScaleConversionDataItem.getRootPipelineId())
                    .modelName(modelName)
                    .modelVersion(modelVersion)
                    .build());
        } catch (JsonProcessingException e) {

            parentObj.add(GreyScaleConversionOutputQuerySet.builder().originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).tenantId(tenantId).templateId(templateId).paperNo(paperNo).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).message(ExceptionUtil.toString(e)).createdOn(Timestamp.valueOf(LocalDateTime.now())).rootPipelineId(rootPipelineId).build());
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Grey Scale Conversion consumer failed for batch/group " + groupId, handymanException, this.action);
        }
    }

    private void extractedCoproOutputResponse(GreyScaleConversionInputQuerySet entity, String greyScaleConversionDataItemStr, List<GreyScaleConversionOutputQuerySet> parentObj, String modelName, String modelVersion) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try {
            GreyScaleConversionDataItem greyScaleConversionDataItem = mapper.readValue(greyScaleConversionDataItemStr, GreyScaleConversionDataItem.class);
            parentObj.add(GreyScaleConversionOutputQuerySet.builder()
                    .processedFilePath(greyScaleConversionDataItem.getProcessedFilePath())
                    .originId(greyScaleConversionDataItem.getOriginId())
                    .groupId(greyScaleConversionDataItem.getGroupId())
                    .processId(greyScaleConversionDataItem.getProcessId())
                    .tenantId(greyScaleConversionDataItem.getTenantId())
                    .inputFilePath(greyScaleConversionDataItem.getInputFilePath())
                    .isColor(greyScaleConversionDataItem.getIsColor())
                    .templateId(templateId)
                    .paperNo(greyScaleConversionDataItem.getPaperNo())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message("Grey Scale Conversion macro completed")
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .modelName(modelName)
                    .modelVersion(modelVersion)
                    .build());
        } catch (JsonProcessingException e) {
            parentObj.add(GreyScaleConversionOutputQuerySet.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .processId(processId)
                    .tenantId(tenantId)
                    .templateId(templateId)
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .build());
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Grey Scale Conversion consumer failed for batch/group " + groupId, handymanException, this.action);
        }
    }


}
