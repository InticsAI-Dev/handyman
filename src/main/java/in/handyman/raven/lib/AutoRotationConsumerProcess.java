package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.autoRotation.AutoRotationInputTable;
import in.handyman.raven.lib.model.autoRotation.AutoRotationOutputTable;
import in.handyman.raven.util.ExceptionUtil;
import in.handyman.raven.util.UniqueID;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.awt.*;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private final int timeOut;


    public AutoRotationConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, String outputDir, AutoRotationAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.outputDir = outputDir;
        this.aAction = aAction;
        this.timeOut  = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder()
                .connectTimeout(this.timeOut, TimeUnit.MINUTES)
                .writeTimeout(this.timeOut, TimeUnit.MINUTES)
                .readTimeout(this.timeOut, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public List<AutoRotationOutputTable> process(URL endpoint, AutoRotationInputTable entity) throws JsonProcessingException {
        List<AutoRotationOutputTable> parentObj = new ArrayList<>();
        final ObjectNode objectNode = mapper.createObjectNode();
        String entityFilePath = entity.getFilePath();
        Long uniqueId= UniqueID.getId();
        Long actionId= action.getActionId();
        Long rootPipelineId=entity.getRootPipelineId();
        String uniqueIdStr=String.valueOf(uniqueId);
        final String autoRotationProcess="AUTO_ROTATION";

        String outputDirectory=outputDir.concat("/").concat(String.valueOf(entity.getRootPipelineId())).concat("/").concat(entity.getOriginId()).concat("/").concat(uniqueIdStr);
        objectNode.put("rootPipelineId",rootPipelineId);
        objectNode.put("actionId",actionId);
        objectNode.put("process",autoRotationProcess);
        objectNode.put("inputFilePath", entityFilePath);
        objectNode.put("outputDir", outputDirectory);
        log.info(aMarker, " Input variables id : {}", action.getActionId());
        Request request = new Request.Builder().url(endpoint)
                .post(RequestBody.create(objectNode.toString(), MEDIA_TYPE_JSON)).build();

        if(log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} and outputDir {}", endpoint, entityFilePath, outputDirectory);
        }
        Integer groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Integer paperNo = entity.getPaperNo();


        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                AutoRotationResponse autoRotationResponse = mapper.readValue(response.body().string(), new TypeReference<>() {
                });
                if (!(autoRotationResponse.getProcessedFilePaths() == null)) {
                    parentObj.add(
                            AutoRotationOutputTable
                                    .builder()
                                    .processedFilePath(autoRotationResponse.getProcessedFilePaths())
                                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                    .groupId(groupId)
                                    .processId(processId)
                                    .tenantId(tenantId)
                                    .templateId(templateId)
                                    .paperNo(paperNo)
                                    .status("COMPLETED")
                                    .stage("AUTO_ROTATION")
                                    .message("Auto rotation macro completed")
                                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                    .rootPipelineId(rootPipelineId)
                                    .build());

                } else {
                    parentObj.add(
                            AutoRotationOutputTable
                                    .builder()
                                    .processedFilePath(entityFilePath)
                                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                    .groupId(groupId)
                                    .processId(processId)
                                    .tenantId(tenantId)
                                    .templateId(templateId)
                                    .paperNo(paperNo)
                                    .status("COMPLETED")
                                    .stage("AUTO_ROTATION")
                                    .message("Auto rotation macro completed")
                                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                    .rootPipelineId(rootPipelineId)
                                    .build());
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
                                .status("FAILED")
                                .stage("AUTO_ROTATION")
                                .message(response.message())
                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                .rootPipelineId(rootPipelineId)
                                .build());
                log.info(aMarker, "Error in response {}", response.message());
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
                            .status("FAILED")
                            .stage("AUTO_ROTATION")
                            .message(ExceptionUtil.toString(e))
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .rootPipelineId(rootPipelineId)
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
