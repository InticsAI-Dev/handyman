package in.handyman.raven.lib.model.autorotation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.AutoRotationAction;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.lib.model.autorotation.copro.AutoRotationDataItemCopro;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.core.utils.ProcessFileFormatE;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class AutoRotationConsumerProcess implements CoproProcessor.ConsumerProcess<AutoRotationInputTable, AutoRotationOutputTable> {

    public static final String PROCESS_NAME = PipelineName.AUTO_ROTATION.getProcessName();

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private final String outputDir;

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final AutoRotationAction aAction;
    private final String AUTO_ROTATION = "AUTO_ROTATION";
    private final int timeOut;
    private final FileProcessingUtils fileProcessingUtils;
    private final String tritonRequestActivator;
    private final String processBase64;


    public AutoRotationConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, String outputDir, AutoRotationAction aAction, FileProcessingUtils fileProcessingUtils, String tritonRequestActivator, String processBase64) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.outputDir = outputDir;
        this.aAction = aAction;
        this.timeOut = aAction.getTimeOut();
        this.fileProcessingUtils = fileProcessingUtils;
        this.tritonRequestActivator = tritonRequestActivator;
        this.processBase64 = processBase64;
        this.httpclient = new OkHttpClient.Builder().connectTimeout(this.timeOut, TimeUnit.MINUTES).writeTimeout(this.timeOut, TimeUnit.MINUTES).readTimeout(this.timeOut, TimeUnit.MINUTES).build();
    }


    @Override
    public List<AutoRotationOutputTable> process(URL endpoint, AutoRotationInputTable entity) throws IOException {

        List<AutoRotationOutputTable> parentObj = new ArrayList<>();
        String entityFilePath = entity.getFilePath();
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        String rootPipelineId = String.valueOf(entity.getRootPipelineId());
        Integer paperNo = entity.getPaperNo();
        String filePath = String.valueOf(entity.getFilePath());
        String batchId = entity.getBatchId();
        Long actionId = action.getActionId();


        //payload
        AutoRotationData autoRotationRequest = new AutoRotationData();
        autoRotationRequest.setProcessId(processId);
        autoRotationRequest.setOriginId(originId);
        autoRotationRequest.setGroupId(groupId);
        autoRotationRequest.setTenantId(tenantId);
        autoRotationRequest.setRootPipelineId(Long.valueOf(rootPipelineId));
        autoRotationRequest.setActionId(actionId);
        autoRotationRequest.setProcess(PROCESS_NAME);
        autoRotationRequest.setInputFilePath(filePath);
        autoRotationRequest.setOutputDir(outputDir);
        autoRotationRequest.setPaperNo(paperNo);
        autoRotationRequest.setBatchId(batchId);


        log.info(aMarker, " Input variables id : {}", action.getActionId());


        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} and outputDir {}", endpoint, entityFilePath, outputDir);
        }


        if (Objects.equals("false", tritonRequestActivator)) {
            String jsonInputRequest = mapper.writeValueAsString(autoRotationRequest);

            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
            coproRequestBuilder(entity, request, parentObj, jsonInputRequest, endpoint);
        } else {
            if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
                autoRotationRequest.setBase64Img(fileProcessingUtils.convertFileToBase64(filePath));
            }

            String jsonInputRequest = mapper.writeValueAsString(autoRotationRequest);


            TritonRequest requestBody = new TritonRequest();
            requestBody.setName("AUTO ROTATOR START");
            requestBody.setShape(List.of(1, 1));
            requestBody.setDatatype("BYTES");
            requestBody.setData(Collections.singletonList(jsonInputRequest));


            TritonInputRequest tritonInputRequest = new TritonInputRequest();
            tritonInputRequest.setInputs(Collections.singletonList(requestBody));

            String jsonRequest = mapper.writeValueAsString(tritonInputRequest);

            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();
            tritonRequestBuilder(entity, request, parentObj, jsonRequest, endpoint);
        }


        return parentObj;
    }

    private void coproRequestBuilder(AutoRotationInputTable entity, Request request, List<AutoRotationOutputTable> parentObj, String jsonInputRequest, URL endpoint) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                extractedCoproOutputResponse(entity, responseBody, parentObj, "", "", jsonInputRequest, responseBody, String.valueOf(endpoint));
            } else {
                String errorMessage = "Unsuccessful response in consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo() + " code : " + response.code() + "\n message : " + response.message();
                parentObj.add(AutoRotationOutputTable.builder().originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).tenantId(tenantId).templateId(templateId).paperNo(paperNo).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(AUTO_ROTATION).message(errorMessage).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).request(encryptRequestResponse(jsonInputRequest)).response(encryptRequestResponse(response.message())).endpoint(String.valueOf(endpoint)).rootPipelineId(rootPipelineId).build());
                HandymanException handymanException = new HandymanException("Unsuccessful response code : "+ response.code() +" message : " + response.message());
                HandymanException.insertException("Auto rotation consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);

                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(AutoRotationOutputTable.builder().originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).tenantId(tenantId).templateId(templateId).paperNo(paperNo).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(AUTO_ROTATION).message(ExceptionUtil.toString(e)).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).request(encryptRequestResponse(jsonInputRequest)).response("Error in getting response").endpoint(String.valueOf(endpoint)).rootPipelineId(rootPipelineId).build());
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("AutoRotation consumer failed for batch/group " + groupId + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));

        }
    }

    private void tritonRequestBuilder(AutoRotationInputTable entity, Request request, List<AutoRotationOutputTable> parentObj, String jsonInputRequest, URL endpoint) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                AutoRotationModelResponse modelResponse = mapper.readValue(responseBody, AutoRotationModelResponse.class);

                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(autoRotationDataItem -> {
                        extractOuputDataRequest(entity, autoRotationDataItem, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion(), jsonInputRequest, responseBody, endpoint.toString());
                    }));

                }

            } else {
                parentObj.add(AutoRotationOutputTable.builder().originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).tenantId(tenantId).templateId(templateId).paperNo(paperNo).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(AUTO_ROTATION).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(rootPipelineId).request(encryptRequestResponse(jsonInputRequest)).response(encryptRequestResponse(response.message())).endpoint(String.valueOf(endpoint)).build());
                HandymanException handymanException = new HandymanException("Unsuccessful response code : "+ response.code() +" message : " + response.message());
                HandymanException.insertException("Auto rotation consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);

                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(AutoRotationOutputTable.builder().originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).tenantId(tenantId).templateId(templateId).paperNo(paperNo).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(AUTO_ROTATION).message(ExceptionUtil.toString(e)).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(rootPipelineId).request(encryptRequestResponse(jsonInputRequest)).response("Error In Response").endpoint(String.valueOf(endpoint)).build());
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("AutoRotation consumer failed for batch/group " + groupId + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }
    }

    private void extractOuputDataRequest(AutoRotationInputTable entity, String autoRotationDataItem, List<AutoRotationOutputTable> parentObj, String modelName, String modelVersion, String request, String response, String endpoint) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try {
            AutoRotationDataItem autoRotationFilePath = mapper.readValue(autoRotationDataItem, AutoRotationDataItem.class);
            if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
                fileProcessingUtils.convertBase64ToFile(autoRotationFilePath.getBase64Img(), autoRotationFilePath.getProcessedFilePaths());
            }
            parentObj.add(AutoRotationOutputTable.builder()
                    .processedFilePath(autoRotationFilePath.getProcessedFilePaths())
                    .originId(autoRotationFilePath.getOriginId())
                    .groupId(autoRotationFilePath.getGroupId())
                    .processId(autoRotationFilePath.getProcessId())
                    .tenantId(autoRotationFilePath.getTenantId())
                    .templateId(templateId)
                    .paperNo(autoRotationFilePath.getPaperNo())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(AUTO_ROTATION).message("Auto rotation macro completed")
                    .createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .rootPipelineId(autoRotationFilePath.getRootPipelineId())
                    .modelName(modelName)
                    .modelVersion(modelVersion)
                    .batchId(autoRotationFilePath.getBatchId())
                    .request(encryptRequestResponse(request))
                    .response(encryptRequestResponse(response))
                    .endpoint(endpoint)
                    .build());

        } catch (JsonProcessingException e) {

            parentObj.add(AutoRotationOutputTable.builder().originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).tenantId(tenantId).templateId(templateId).paperNo(paperNo).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(AUTO_ROTATION).message(ExceptionUtil.toString(e)).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(rootPipelineId).batchId(entity.getBatchId()).request(encryptRequestResponse(request)).response(encryptRequestResponse(response)).endpoint(String.valueOf(endpoint)).build());
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("AutoRotation consumer failed for batch/group " + groupId + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
        } catch (IOException e) {
            parentObj.add(AutoRotationOutputTable.builder().originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).tenantId(tenantId).templateId(templateId).paperNo(paperNo).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(AUTO_ROTATION).message(ExceptionUtil.toString(e)).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(rootPipelineId).batchId(entity.getBatchId()).request(encryptRequestResponse(request)).response(encryptRequestResponse(response)).endpoint(String.valueOf(endpoint)).build());
            log.error(aMarker, "The Exception occurred in processing base64 {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("AutoRotation consumer failed in converting base64 batch/group " + groupId + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
        }
    }

    private void extractedCoproOutputResponse(AutoRotationInputTable entity, String autoRotationDataItem, List<AutoRotationOutputTable> parentObj, String modelName, String modelVersion, String request, String response, String endpoint) {
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try {
            AutoRotationDataItemCopro autoRotationFilePath = mapper.readValue(autoRotationDataItem, AutoRotationDataItemCopro.class);
            parentObj.add(AutoRotationOutputTable.builder()
                    .processedFilePath(autoRotationFilePath.getProcessedFilePaths())
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .processId(processId)
                    .tenantId(tenantId)
                    .templateId(templateId)
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(AUTO_ROTATION)
                    .message("Auto rotation macro completed")
                    .createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .rootPipelineId(rootPipelineId)
                    .modelName(modelName)
                    .modelVersion(modelVersion)
                    .request(encryptRequestResponse(request))
                    .response(encryptRequestResponse(response))
                    .endpoint(endpoint)
                    .batchId(entity.getBatchId())
                    .build());
        } catch (JsonProcessingException e) {
            parentObj.add(AutoRotationOutputTable.builder().originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).tenantId(tenantId).templateId(templateId).paperNo(paperNo).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(AUTO_ROTATION).message(ExceptionUtil.toString(e)).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(rootPipelineId).response(encryptRequestResponse(response)).request(encryptRequestResponse(request)).endpoint(endpoint).build());
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("AutoRotation consumer failed for batch/group " + groupId + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
        }
    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        String requestStr;
        if ("true".equals(encryptReqRes)) {
            String encryptedRequest = SecurityEngine.getInticsIntegrityMethod(action,log).encrypt(request, "AES256", "COPRO_REQUEST");
            requestStr = encryptedRequest;
        } else {
            requestStr = request;
        }
        return requestStr;
    }

}


