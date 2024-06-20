package in.handyman.raven.lib.model.utModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.lib.UrgencyTriageModelAction.urgencyTriageModel;

public class UrgencyTriageConsumerProcess implements CoproProcessor.ConsumerProcess<UrgencyTriageInputTable, UrgencyTriageOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private final MediaType mediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public UrgencyTriageConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }

    @Override
    public List<UrgencyTriageOutputTable> process(URL endpoint, UrgencyTriageInputTable entity) throws Exception {
        List<UrgencyTriageOutputTable> parentObj = new ArrayList<>();

        String inputFilePath = entity.getInputFilePath();
        String outputDir = urgencyTriageModel.getOutputDir();

        ObjectMapper objectMapper = new ObjectMapper();

//payload
        UrgencyTriageModelPayload urgencyTriageModelPayload = new UrgencyTriageModelPayload();
        urgencyTriageModelPayload.setRootPipelineId(Long.valueOf(String.valueOf(entity.getRootPipelineId())));
        urgencyTriageModelPayload.setProcess("QR");
        urgencyTriageModelPayload.setInputFilePath(entity.getInputFilePath());
        urgencyTriageModelPayload.setActionId(action.getActionId());
        String jsonInputRequest = objectMapper.writeValueAsString(urgencyTriageModelPayload);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("UT START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest=new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);


        String tritonRequestActivator = action.getContext().get("triton.request.activator");


        if (Objects.equals("true", tritonRequestActivator)) {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(urgencyTriageModelPayload.toString(), mediaTypeJSON)).build();
            coproRequestBuider(entity, request,objectMapper, parentObj);
        } else {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonRequest, mediaTypeJSON)).build();
            tritonRequestBuilder(entity, request,objectMapper, parentObj);
        }

        if(log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n coproUrl  {} ,inputFilePath : {} ,outputDir {} ", endpoint,inputFilePath,outputDir);
        }

        return parentObj;
    }

    private void tritonRequestBuilder(UrgencyTriageInputTable entity, Request request, ObjectMapper objectMapper, List<UrgencyTriageOutputTable> parentObj) {
        String createdUserId = entity.getCreatedUserId();
        String lastUpdatedUserId = entity.getLastUpdatedUserId();
        Long tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();
        Integer groupId = entity.getGroupId();
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();
        try (Response response = httpclient.newCall(request).execute()) {
            final String responseBody = response.body().string();
            if (response.isSuccessful()) {
                log.info("Response Details: {}",response);

                UrgencyTriageModelResponse modelResponse = objectMapper.readValue(responseBody, UrgencyTriageModelResponse.class);

                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(urgencyTriageModelDataItem -> {

                            extractedOutputRequest(entity, urgencyTriageModelDataItem, objectMapper, parentObj, originId, templateId);

                            log.info(aMarker, "Execute for urgency triage {}", response);
                        });
                    });
                }
            } else {
                parentObj.add(UrgencyTriageOutputTable.builder()
                        .createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                        .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                        .tenantId(Optional.ofNullable(tenantId).map(String::valueOf).map(Long::valueOf).orElse(null))
                        .processId(Optional.ofNullable(processId).map(String::valueOf).map(Long::parseLong).orElse(null))
                        .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                        .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                        .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                        .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                        .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                        .status("FAILED")
                        .stage("URGENCY_TRIAGE_MODEL")
                        .message(response.message())
                        .rootPipelineId(entity.getRootPipelineId())
                        .build());
                log.error(aMarker, "The Exception occurred in urgency triage {}",response);
            }
        } catch (Exception e) {
            parentObj.add(UrgencyTriageOutputTable.builder()
                    .createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                    .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                    .tenantId(Optional.ofNullable(tenantId).map(String::valueOf).map(Long::valueOf).orElse(null))
                    .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                    .processId(Optional.ofNullable(processId).map(String::valueOf).map(Long::parseLong).orElse(null))
                    .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                    .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                    .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                    .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                    .status("FAILED")
                    .stage("URGENCY_TRIAGE_MODEL")
                    .message(ExceptionUtil.toString(e))
                    .rootPipelineId(entity.getRootPipelineId())
                    .build());
            log.error(aMarker, "The Exception occurred in urgency triage", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Exception occurred in urgency triage model action for group id - "+ groupId + " and originId - "+ originId, handymanException, this.action);
        }
    }

    private static void extractedOutputRequest(UrgencyTriageInputTable entity, String urgencyTriageModelDataItem, ObjectMapper objectMapper, List<UrgencyTriageOutputTable> parentObj,String modelName,String modelVersion) {
        String createdUserId = entity.getCreatedUserId();
        String lastUpdatedUserId = entity.getLastUpdatedUserId();
        Long tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();
        Integer groupId = entity.getGroupId();
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();
        try {
            UrgencyTriageModelDataItem urgencyTriageModelDataItem1 = objectMapper.readValue(urgencyTriageModelDataItem,UrgencyTriageModelDataItem.class);

            parentObj.add(UrgencyTriageOutputTable.builder()
                    .createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                    .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                    .tenantId(Optional.ofNullable(tenantId).map(Long::valueOf).orElse(null))
                    .processId(Optional.ofNullable(processId).map(String::valueOf).map(Long::parseLong).orElse(null))
                    .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                    .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                    .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                    .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                    .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                    .utResult(urgencyTriageModelDataItem1.getPaperType())
                    .confScore(urgencyTriageModelDataItem1.getConfidenceScore())
                    .bbox(String.valueOf(urgencyTriageModelDataItem1.getBboxes()))
                    .status("COMPLETED")
                    .stage("URGENCY_TRIAGE_MODEL")
                    .message("Urgency Triage Finished")
                    .rootPipelineId(entity.getRootPipelineId())
                    .modelName(modelName)
                    .modelVersion(modelVersion)
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private void coproRequestBuider(UrgencyTriageInputTable entity, Request request, ObjectMapper objectMapper, List<UrgencyTriageOutputTable> parentObj) {
        try (Response response = httpclient.newCall(request).execute()) {
            final String responseBody = response.body().string();
            String createdUserId = entity.getCreatedUserId();
            String lastUpdatedUserId = entity.getLastUpdatedUserId();
            Long tenantId = entity.getTenantId();
            Long processId = entity.getProcessId();
            Integer groupId = entity.getGroupId();
            String originId = entity.getOriginId();
            Integer paperNo = entity.getPaperNo();
            String templateId = entity.getTemplateId();
            Long modelId = entity.getModelId();
            if (response.isSuccessful()) {
                log.info("Response Details: {}",response);
                extractedOutputRequest(entity, responseBody, objectMapper, parentObj, originId,templateId);

            } else {
                parentObj.add(UrgencyTriageOutputTable.builder()
                        .createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                        .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                        .tenantId(Optional.ofNullable(tenantId).map(String::valueOf).map(Long::valueOf).orElse(null))
                        .processId(Optional.ofNullable(processId).map(String::valueOf).map(Long::parseLong).orElse(null))
                        .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                        .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                        .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                        .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                        .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                        .status("FAILED")
                        .stage("URGENCY_TRIAGE_MODEL")
                        .message(response.message())
                        .rootPipelineId(entity.getRootPipelineId())
                        .build());
                log.error(aMarker, "The Exception occurred in urgency triage {}",response);
            }
        } catch (Exception e) {
            String createdUserId = entity.getCreatedUserId();
            String lastUpdatedUserId = entity.getLastUpdatedUserId();
            Long tenantId = entity.getTenantId();
            Long processId = entity.getProcessId();
            Integer groupId = entity.getGroupId();
            String originId = entity.getOriginId();
            Integer paperNo = entity.getPaperNo();
            String templateId = entity.getTemplateId();
            Long modelId = entity.getModelId();

            parentObj.add(UrgencyTriageOutputTable.builder()

                    .createdUserId(Optional.ofNullable(createdUserId).map(String::valueOf).orElse(null))
                    .lastUpdatedUserId(Optional.ofNullable(lastUpdatedUserId).map(String::valueOf).orElse(null))
                    .tenantId(Optional.ofNullable(tenantId).map(String::valueOf).map(Long::valueOf).orElse(null))
                    .groupId(Optional.ofNullable(groupId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                    .processId(Optional.ofNullable(processId).map(String::valueOf).map(Long::parseLong).orElse(null))
                    .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                    .paperNo(Optional.ofNullable(paperNo).map(String::valueOf).map(Integer::parseInt).orElse(null))
                    .templateId(Optional.ofNullable(templateId).map(String::valueOf).orElse(null))
                    .modelId(Optional.ofNullable(modelId).map(String::valueOf).map(Integer::parseInt).orElse(null))
                    .status("FAILED")
                    .stage("URGENCY_TRIAGE_MODEL")
                    .message(ExceptionUtil.toString(e))
                    .rootPipelineId(entity.getRootPipelineId())
                    .build());
            log.error(aMarker, "The Exception occurred in urgency triage", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Exception occurred in urgency triage model action for group id - "+ groupId + " and originId - "+ originId, handymanException, this.action);
        }
    }
}



