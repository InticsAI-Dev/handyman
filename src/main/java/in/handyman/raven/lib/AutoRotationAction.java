package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AutoRotation;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "AutoRotation"
)
public class AutoRotationAction implements IActionExecution {
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    private final ActionExecutionAudit action;
    private final Logger log;
    private final AutoRotation autoRotation;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String URI;

    public AutoRotationAction(final ActionExecutionAudit action, final Logger log,
                              final Object autoRotation) {
        this.autoRotation = (AutoRotation) autoRotation;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" AutoRotation:" + this.autoRotation.getName());
        this.URI = action.getContext().get("copro.autorotation.url");

    }

    @Override
    public void execute() throws Exception {
        try{
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(autoRotation.getResourceConn());

            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "Auto Rotation Action for {} has been started", autoRotation.getName());
            final String outputDir = Optional.ofNullable(autoRotation.getOutputDir()).map(String::valueOf).orElse(null);
            final String insertQuery = "INSERT INTO info.auto_rotation(origin_id,group_id,tenant_id,template_id,process_id, processed_file_path,paper_no, status,stage,message,created_on) " +
                    " VALUES(?,?, ?,?,?, ?,?, ?,?,?,?)";
            final List<URL> urls = Optional.ofNullable(action.getContext().get("copro.autorotation.url")).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new HandymanException("Error in processing the URL", e, action);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            final CoproProcessor<AutoRotationAction.AutoRotationInputTable, AutoRotationAction.AutoRotationOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            AutoRotationAction.AutoRotationOutputTable.class,
                            AutoRotationAction.AutoRotationInputTable.class,
                            jdbi, log,
                            new AutoRotationAction.AutoRotationInputTable(), urls, action);
            coproProcessor.startProducer(autoRotation.getQuerySet(), Integer.valueOf(action.getContext().get("read.batch.size")));
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, Integer.valueOf(action.getContext().get("consumer.API.count")), Integer.valueOf(action.getContext().get("write.batch.size")), new AutoRotationAction.AutoRotationConsumerProcess(log, aMarker, action, outputDir));
            log.info(aMarker, " Auto Rotation Action has been completed {}  ", autoRotation.getName());
        }catch(Exception e){
            action.getContext().put(autoRotation.getName() + ".isSuccessful", "false");
            log.error(aMarker,"error in execute method for auto rotation ",e);
            throw new HandymanException("error in execute method for auto rotation", e, action);
        }
    }

    public static class AutoRotationConsumerProcess implements CoproProcessor.ConsumerProcess<AutoRotationAction.AutoRotationInputTable, AutoRotationAction.AutoRotationOutputTable> {
        private final Logger log;
        private final Marker aMarker;
        private final ObjectMapper mapper = new ObjectMapper();
        private static final MediaType MediaTypeJSON = MediaType
                .parse("application/json; charset=utf-8");
        private final String outputDir;

        public final ActionExecutionAudit action;
        final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        public AutoRotationConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, String outputDir) {
            this.log = log;
            this.aMarker = aMarker;
            this.action = action;
            this.outputDir = outputDir;
        }

        @Override
        public List<AutoRotationAction.AutoRotationOutputTable> process(URL endpoint, AutoRotationAction.AutoRotationInputTable entity) throws JsonProcessingException {
            List<AutoRotationAction.AutoRotationOutputTable> parentObj = new ArrayList<>();
            final ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("inputFilePath", entity.filePath);
            objectNode.put("outputDir", outputDir);
            log.info(aMarker, " Input variables id : {}", action.getActionId());
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();
            log.debug(aMarker, "Request has been build with the parameters \n URI : {} ",endpoint);
            log.debug(aMarker, "The Request Details: {}", request);

            try (Response response = httpclient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    AutoRotationResponse autoRotationResponse=mapper.readValue(response.body().string(), new TypeReference<>() {
                    });
                    if(!(autoRotationResponse.getProcessedFilePaths()==null)){
                            parentObj.add(
                                    AutoRotationAction.AutoRotationOutputTable
                                            .builder()
                                            .processedFilePath(autoRotationResponse.getProcessedFilePaths())
                                            .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                            .groupId(entity.getGroupId())
                                            .processId(entity.processId)
                                            .tenantId(entity.tenantId)
                                            .templateId(entity.templateId)
                                            .paperNo(entity.paperNo)
                                            .status("COMPLETED")
                                            .stage("AUTO_ROTATION")
                                            .message("Auto rotation macro completed")
                                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                            .build());

                    }else{
                            parentObj.add(
                                    AutoRotationAction.AutoRotationOutputTable
                                            .builder()
                                            .processedFilePath(entity.filePath)
                                            .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                            .groupId(entity.getGroupId())
                                            .processId(entity.processId)
                                            .tenantId(entity.tenantId)
                                            .templateId(entity.templateId)
                                            .paperNo(entity.paperNo)
                                            .status("COMPLETED")
                                            .stage("AUTO_ROTATION")
                                            .message("Auto rotation macro completed")
                                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                            .build());
                    }

                }else{
                    parentObj.add(
                            AutoRotationAction.AutoRotationOutputTable
                                    .builder()
                                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                    .groupId(entity.getGroupId())
                                    .processId(entity.processId)
                                    .tenantId(entity.tenantId)
                                    .templateId(entity.templateId)
                                    .paperNo(entity.paperNo)
                                    .status("FAILED")
                                    .stage("AUTO_ROTATION")
                                    .message(response.message())
                                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                    .build());
                    log.info(aMarker, "Error in response {}", response.message());
                }
            } catch (Exception e) {
                parentObj.add(
                        AutoRotationOutputTable
                                .builder()
                                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                .groupId(entity.getGroupId())
                                .processId(entity.processId)
                                .tenantId(entity.tenantId)
                                .templateId(entity.templateId)
                                .paperNo(entity.paperNo)
                                .status("FAILED")
                                .stage("AUTO_ROTATION")
                                .message(ExceptionUtil.toString(e))
                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                .build());
                log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            }

            return parentObj;
        }

    }

    @Override
    public boolean executeIf() throws Exception {
        return autoRotation.getCondition();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AutoRotationInputTable implements CoproProcessor.Entity {
        private String originId;
        private Integer paperNo;
        private Integer groupId;
        private String filePath;
        private String tenantId;
        private String templateId;
        private Long processId;
        private String outputDir;

        @Override
        public List<Object> getRowData() {
            return null;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AutoRotationResponse {

        private String processedFilePaths;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AutoRotationOutputTable implements CoproProcessor.Entity {

        private String originId;
        private Integer groupId;
        private String tenantId;
        private String templateId;
        private Long processId;
        private String processedFilePath;
        private Integer paperNo;
        private String status;
        private String stage;
        private String message;
        private Timestamp createdOn;

        @Override
        public List<Object> getRowData() {
            return Stream.of(this.originId, this.groupId,this.tenantId,this.templateId,this.processId, this.processedFilePath, this.paperNo,this.status,this.stage,this.message,this.createdOn).collect(Collectors.toList());
        }
    }


}