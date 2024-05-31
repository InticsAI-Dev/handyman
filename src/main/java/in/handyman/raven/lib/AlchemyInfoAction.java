package in.handyman.raven.lib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AlchemyInfo;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "AlchemyInfo"
)
public class AlchemyInfoAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final AlchemyInfo alchemyInfo;

    private final Marker aMarker;


    public AlchemyInfoAction(final ActionExecutionAudit action, final Logger log,
                             final Object alchemyInfo) {
        this.alchemyInfo = (AlchemyInfo) alchemyInfo;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" AlchemyInfo:" + this.alchemyInfo.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(alchemyInfo.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "Alchemy Info Action for {} has been started", alchemyInfo.getName());
            final String insertQuery = "INSERT INTO alchemy_migration.alchemy_papers (tenant_id, group_id, paper_no, pipeline_origin_id, alchemy_origin_id, origin_file_path, width, height, created_on, root_pipeline_id, batch_id)" +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?)";
            final List<URL> urls = Optional.ofNullable(action.getContext().get("alchemy.origin.upload.url")).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            final CoproProcessor<AlchemyInfoAction.AlchemyInfoInputTable, AlchemyInfoAction.AlchemyInfoOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            AlchemyInfoAction.AlchemyInfoOutputTable.class,
                            AlchemyInfoAction.AlchemyInfoInputTable.class,
                            jdbi, log,
                            new AlchemyInfoAction.AlchemyInfoInputTable(), urls, action);
            coproProcessor.startProducer(alchemyInfo.getQuerySet(), Integer.valueOf(action.getContext().get("read.batch.size")));
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, 1,  Integer.valueOf(action.getContext().get("write.batch.size")), new AlchemyInfoAction.AlchemyInfoConsumerProcess(log, aMarker, action));
            log.info(aMarker, "Alchemy Info has been completed {}  ", alchemyInfo.getName());
        } catch (Exception t) {
            action.getContext().put(alchemyInfo.getName() + ".isSuccessful", "false");
            log.error(aMarker, "Error at alchemy info execute method {}", ExceptionUtil.toString(t));
            throw new HandymanException("Error at alchemyInfo execute method ", t, action);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return alchemyInfo.getCondition();
    }

    public static class AlchemyInfoConsumerProcess implements CoproProcessor.ConsumerProcess<AlchemyInfoAction.AlchemyInfoInputTable, AlchemyInfoAction.AlchemyInfoOutputTable> {
        private final Logger log;
        private final Marker aMarker;
        private final ObjectMapper mapper = new ObjectMapper();

        public final ActionExecutionAudit action;

        private final Long tenantId;
        private final String authToken;
        final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        public AlchemyInfoConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
            this.log = log;
            this.aMarker = aMarker;
            this.action = action;
            this.tenantId = Long.valueOf(action.getContext().get("alchemyAuth.tenantId"));
            this.authToken = action.getContext().get("alchemyAuth.token");
        }

        @Override
        public List<AlchemyInfoAction.AlchemyInfoOutputTable> process(URL endpoint, AlchemyInfoAction.AlchemyInfoInputTable entity) throws Exception {

            List<AlchemyInfoAction.AlchemyInfoOutputTable> parentObj = new ArrayList<>();
            String inputFilePath = entity.getFilePath();

            File file = new File(inputFilePath);
            MediaType mediaType = MediaType.parse("application/pdf");

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), RequestBody.create(mediaType, file))
                    .build();

            URL url = new URL(endpoint.toString() + "/" + entity.getRootPipelineId() + "/?tenantId=" + this.tenantId);
            Request request = new Request.Builder().url(url)
                    .addHeader("accept", "*/*")
                    .addHeader("Authorization", "Bearer " + authToken)
                    .post(requestBody)
                    .build();

            if (log.isInfoEnabled()) {
                log.info(aMarker, "Request has been build with the parameters {} ,inputFilePath : {}", endpoint, inputFilePath);
            }

            Integer groupId = entity.getGroupId();
            String pipelineOriginId = entity.getOriginId();
            Long rootPipelineId = entity.getRootPipelineId();
            try (Response response = httpclient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.info("Response Details: {}", response);
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    JsonNode responseNode = mapper.readTree(responseBody);
                    JsonNode payload = responseNode.get("payload");
                    List<OriginUploadResponse> originUploadResponseList = mapper.readValue(payload.toString(), new TypeReference<>() {
                    });
                    originUploadResponseList.forEach(originUploadResponse -> parentObj.add(AlchemyInfoOutputTable.builder()
                            .tenantId(this.tenantId)
                            .groupId(groupId)
                            .paperNo(originUploadResponse.getPageNo())
                            .pipelineOriginId(pipelineOriginId)
                            .alchemyOriginId(originUploadResponse.getOriginId())
                            .originFilePath(inputFilePath)
                            .width(originUploadResponse.getWidth())
                            .height(originUploadResponse.getHeight())
                            .rootPipelineId(rootPipelineId)
                            .batchId(entity.getBatchId())
                            .build()));
                    log.info(aMarker, "Execute for alchemy Info {}", response);
                }
            } catch (Exception e) {
                log.error(aMarker, "The Exception occurred in alchemy info action", e);
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("Exception occurred in alchemy info action for group id - " + groupId + " and originId - " + pipelineOriginId, handymanException, this.action);
            }
            return parentObj;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class AlchemyInfoInputTable implements CoproProcessor.Entity {
        private String originId;
        private Long rootPipelineId;
        private Integer groupId;
        private String filePath;
        private String batchId;

        @Override
        public List<Object> getRowData() {
            return null;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class AlchemyInfoOutputTable implements CoproProcessor.Entity {
        private Long tenantId;
        private Integer groupId;
        private Integer paperNo;
        private String pipelineOriginId;
        private String alchemyOriginId;
        private String originFilePath;
        private Integer width;
        private Integer height;
        private Long rootPipelineId;
        private String batchId;


        @Override
        public List<Object> getRowData() {
            return Stream.of(this.tenantId, this.groupId, this.paperNo, this.pipelineOriginId,
                    this.alchemyOriginId, this.originFilePath, this.width, this.height, this.rootPipelineId, this.batchId).collect(Collectors.toList());
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class OriginUploadResponse {
        private Integer pageNo;
        private String encode;
        private Integer width;
        private Integer height;
        private String originId;
        private String content;
    }
}
