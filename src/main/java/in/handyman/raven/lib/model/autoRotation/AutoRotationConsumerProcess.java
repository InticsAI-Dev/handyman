package in.handyman.raven.lib.model.autoRotation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.*;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
public class AutoRotationConsumerProcess implements CoproProcessor.ConsumerProcess<AutoRotationInputTable, AutoRotationOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType
            .parse("application/json; charset=utf-8");
    private final String outputDir;

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final AutoRotationAction aAction;
    private final String  AUTO_ROTATION="AUTO_ROTATION";

    private final int timeOut;


    public AutoRotationConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, String outputDir, AutoRotationAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.outputDir = outputDir;
        this.aAction = aAction;
        this.timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder()
                .connectTimeout(this.timeOut, TimeUnit.MINUTES)
                .writeTimeout(this.timeOut, TimeUnit.MINUTES)
                .readTimeout(this.timeOut, TimeUnit.MINUTES)
                .build();
    }



    @Override
    public List<AutoRotationOutputTable> process(URL endpoint, AutoRotationInputTable entity) throws IOException {

        List<AutoRotationOutputTable> parentObj = new ArrayList<>();
        String entityFilePath = entity.getFilePath();


        String rootpipelineId = String.valueOf(entity.getRootPipelineId());
        String process = String.valueOf(entity.process);
        String filePath = String.valueOf(entity.getFilePath());
        Long actionId = action.getActionId();


        //payload
        AutoRotationData autoRotationRequest = new AutoRotationData();
        autoRotationRequest.setRootPipelineId(Long.valueOf(rootpipelineId));
        autoRotationRequest.setActionId(actionId);
        autoRotationRequest.setProcess(process);
        autoRotationRequest.setInputFilePath(filePath);
        autoRotationRequest.setOutputDir(outputDir);
        String jsonInputRequest = mapper.writeValueAsString(autoRotationRequest);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("AUTO ROTATOR START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = mapper.writeValueAsString(tritonInputRequest);


        log.info(aMarker, " Input variables id : {}", action.getActionId());
        Request request = new Request.Builder().url(endpoint)
                .post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();

        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} and outputDir {}", endpoint, entityFilePath, outputDir);
        }
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Integer paperNo = entity.getPaperNo();
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                AutoRotationModelResponse modelResponse = mapper.readValue(responseBody, AutoRotationModelResponse.class);

                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(autoRotationDataItem -> {
                        try {
                            AutoRotationDataItem autoRotationFilePath = mapper.readValue(autoRotationDataItem, AutoRotationDataItem.class);

                            parentObj.add(AutoRotationOutputTable
                                    .builder()
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
                                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                    .rootPipelineId(Long.valueOf(rootpipelineId))
                                    .modelName(modelResponse.getModelName())
                                    .modelVersion(modelResponse.getModelVersion())
                                    .build()
                        );
                        } catch (JsonProcessingException e) {

                            parentObj.add(
                                    AutoRotationOutputTable
                                            .builder()
                                            .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                            .groupId(groupId)
                                            .processId(processId)
                                            .tenantId(tenantId)
                                            .templateId(templateId)
                                            .paperNo(paperNo)
                                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                            .stage(AUTO_ROTATION)
                                            .message(ExceptionUtil.toString(e))
                                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                            .rootPipelineId(Long.valueOf(rootpipelineId))
                                            .build());
                            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
                            HandymanException handymanException = new HandymanException(e);
                            HandymanException.insertException("AutoRotation consumer failed for batch/group " + groupId,
                                    handymanException,
                                    this.action);
                        }


                    }));

                }

            } else {
                parentObj.add(
                        AutoRotationOutputTable
                                .builder()
                                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                .groupId(groupId)
                                .processId(processId)
                                .tenantId(tenantId)
                                .templateId(templateId)
                                .paperNo(paperNo)
                                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                .stage(AUTO_ROTATION)
                                .message(response.message())
                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                .rootPipelineId(Long.valueOf(rootpipelineId))
                                .build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(
                    AutoRotationOutputTable
                            .builder()
                            .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                            .groupId(groupId)
                            .processId(processId)
                            .tenantId(tenantId)
                            .templateId(templateId)
                            .paperNo(paperNo)
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .stage(AUTO_ROTATION)
                            .message(ExceptionUtil.toString(e))
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .rootPipelineId(Long.valueOf(rootpipelineId))
                            .build());
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("AutoRotation consumer failed for batch/group " + groupId,
                    handymanException,
                    this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }

        return parentObj;
    }

}


