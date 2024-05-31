package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultipartUpload;
import in.handyman.raven.util.CommonQueryUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "MultipartUpload"
)
public class MultipartUploadAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final MultipartUpload multipartUpload;

    private final Marker aMarker;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public MultipartUploadAction(final ActionExecutionAudit action, final Logger log, final Object multipartUpload) {
        this.multipartUpload = (MultipartUpload) multipartUpload;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" MultipartUpload:" + this.multipartUpload.getName());
    }

    @Override
    public void execute() throws Exception {

        List<MultipartUploadInputTable> multipartUploadInputTables = new ArrayList<>();

        try {
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(multipartUpload.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "Multipart Upload Action for {} has been started", multipartUpload.getName());
            String endPoint = multipartUpload.getEndPoint();
            final List<URL> urls = Optional.ofNullable(endPoint).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(multipartUpload.getQuerySet());
            formattedQuery.forEach(sql -> jdbi.useTransaction(handle -> handle.createQuery(sql).mapToBean(MultipartUploadInputTable.class).forEach(multipartUploadInputTables::add)));

            if (!multipartUploadInputTables.isEmpty()) {

                if (!urls.isEmpty()) {
                    int endpointSize = urls.size();
                    log.info("Endpoints are not empty for multipart upload with nodes count {}", endpointSize);

                    int batchSize = Integer.parseInt(action.getContext().get("batch.processing.split.count"));
                    final ExecutorService executorService = Executors.newFixedThreadPool(batchSize);
                    int inputSize = multipartUploadInputTables.size();
                    final CountDownLatch countDownLatch = new CountDownLatch(inputSize * endpointSize);
                    log.info("Total consumers {}", countDownLatch.getCount());

                    log.info("Input files sizes {}", inputSize);

                    multipartUploadInputTables.parallelStream().forEach(multipartUploadInputTable -> {
                        urls.forEach(url -> executorService.submit(() -> {
                            String filepath = multipartUploadInputTable.getFilePath();
                            try {
                                uploadFile(url, multipartUploadInputTable, jdbi);
                            } catch (Exception e) {
                                log.error(aMarker, "The Exception occurred in multipart file upload for file {} with exception {}", filepath, e.getMessage());
                                HandymanException handymanException = new HandymanException(e);
                                HandymanException.insertException("Exception occurred in multipart upload for file - " + filepath, handymanException, this.action);
                            } finally {
                                log.info("Consumer {} completed the process for file {}", countDownLatch.getCount(), filepath);
                                countDownLatch.countDown();
                            }
                        }));
                    });
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        log.error("Consumer Interrupted with exception", e);
                        throw new HandymanException("Error in Multipart upload execute method for mapping query set", e, action);
                    } finally {
                        executorService.shutdown();
                    }
                } else {
                    log.error(aMarker, "Endpoints for multipart upload is empty");
                }
            } else {
                log.info("Multipart upload input request list is empty");
            }

        } catch (Exception e) {
            throw new HandymanException("Error in Multipart upload", e, action);
        }
    }

    public void uploadFile(final URL endpoint, final MultipartUploadInputTable entity, final Jdbi jdbi) throws Exception {

        // Retrieving input parameters
        final String inputFilePath = entity.getFilePath();
        final Integer groupId = entity.getGroupId();
        final Long processId = entity.getProcessId();
        final String templateId = entity.getTemplateId();
        final Long tenantId = entity.getTenantId();
        final Integer paperNo = entity.getPaperNo();
        final String originId = entity.getOriginId();
        final Long rootPipelineId = entity.getRootPipelineId();
        // Retrieving input parameters
        final String inputFilePath = entity.getFilePath();
        final Integer groupId = entity.getGroupId();
        final Long processId = entity.getProcessId();
        final String templateId = entity.getTemplateId();
        final Long tenantId = entity.getTenantId();
        final Integer paperNo = entity.getPaperNo();
        final String originId = entity.getOriginId();
        final Long rootPipelineId = entity.getRootPipelineId();
        final String batchId = entity.getBatchId();
        String outputDir;

        // Setting output directory
        if (entity.getOutputDir() != null) {
            outputDir = entity.getOutputDir();
        } else {
            final Path path = Paths.get(inputFilePath);
            final Path directory = path.getParent();
            outputDir = directory.toString();
        }

        // Creating file object
        final File file = new File(inputFilePath);

        // Defining media type
        final MediaType MEDIA_TYPE = MediaType.parse("application/*");

        // Building request body
        final RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(file, MEDIA_TYPE))
                .build();

        // Creating URL for the request
        final URL url = new URL(endpoint.toString() + "/?outputDir=" + outputDir);

        // Building request
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("accept", "*/*")
                .post(requestBody)
                .build();

        try (final Response response = httpclient.newCall(request).execute()) {
            final ObjectMapper objectMapper = new ObjectMapper();
            if (response.isSuccessful()) {
                // Handle successful response
                log.info("Response Details: {}", response);
                if (response.body() != null) {
                    final String responseBody = response.body().string();
                    final MultipartUploadOutputTable multipartUploadOutputTable = objectMapper.readValue(responseBody, MultipartUploadOutputTable.class);
                    handleResponse(jdbi, groupId, processId, templateId, tenantId, paperNo, originId, rootPipelineId, multipartUploadOutputTable);
                }
            } else {
                // Handle unsuccessful response
                log.error("Request was not successful. HTTP Status: {}", response.code());
                final MultipartUploadOutputTable multipartUploadOutputTable = new MultipartUploadOutputTable();
                handleResponse(jdbi, groupId, processId, templateId, tenantId, paperNo, originId, rootPipelineId, multipartUploadOutputTable);
                if (response.body() != null) {
                    final String responseBody = response.body().string();
                    final MultipartUploadOutputTable multipartUploadOutputTable = objectMapper.readValue(responseBody, MultipartUploadOutputTable.class);
                    handleResponse(jdbi, groupId, processId, templateId, tenantId, paperNo, originId, rootPipelineId, multipartUploadOutputTable, batchId);
                }
            } else {
                // Handle unsuccessful response
                log.error("Request was not successful. HTTP Status: {}", response.code());
                final MultipartUploadOutputTable multipartUploadOutputTable = new MultipartUploadOutputTable();
                handleResponse(jdbi, groupId, processId, templateId, tenantId, paperNo, originId, rootPipelineId, multipartUploadOutputTable, batchId);
            }
        } catch (final Exception e) {
            log.error(aMarker, "Exception occurred in multipart file upload for file {} with exception {}", inputFilePath, e.getMessage());
            final HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Exception occurred in multipart upload for file - " + inputFilePath, handymanException, this.action);
        }
    }


    private void handleResponse(Jdbi jdbi, Integer groupId, Long processId, String templateId, Long tenantId, Integer paperNo, String originId, Long rootPipelineId, MultipartUploadOutputTable multipartUploadOutputTable, String batchId) {
        try {
            multipartUploadOutputTable.setGroupId(groupId);
            multipartUploadOutputTable.setRootPipelineId(rootPipelineId);
            multipartUploadOutputTable.setPaperNo(paperNo);
            multipartUploadOutputTable.setOriginId(originId);
            multipartUploadOutputTable.setTemplateId(templateId);
            multipartUploadOutputTable.setProcessId(processId);
            multipartUploadOutputTable.setTenantId(tenantId);
            multipartUploadOutputTable.setUploadedTime(LocalDateTime.now());
            multipartUploadOutputTable.setBatchId(batchId);

            jdbi.useHandle(handle -> {
                String sql = "INSERT INTO multipart_info.multipart_upload(" +
                        "filepath, filename, message, status, template_id, origin_id, " +
                        "root_pipeline_id, process_id, group_id, tenant_id, paper_no, uploaded_time, batch_id) " +
                        "VALUES (:filepath, :filename, :message, :status, :templateId, :originId, " +
                        ":rootPipelineId, :processId, :groupId, :tenantId, :paperNo, :uploadedTime, :batchId)";

                handle.createUpdate(sql)
                        .bindBean(multipartUploadOutputTable)
                        .execute();
            });
        } catch (UnableToExecuteStatementException exception) {
            log.error(aMarker, "Exception occurred in multipart insert: {}", exception.getMessage(), exception);
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Exception occurred in multipart insert for the file -  " + multipartUploadOutputTable.getFilepath(), handymanException, this.action);
        }
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class MultipartUploadInputTable {
        private String originId;
        private Integer paperNo;
        private Integer groupId;
        private String filePath;
        private Long tenantId;
        private String templateId;
        private Long processId;
        private String outputDir;
        private Long rootPipelineId;
        private String batchId;
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class MultipartUploadOutputTable {
        private String filepath;
        private String filename;
        private String message;
        private String status;
        private String originId;
        private Integer paperNo;
        private Integer groupId;
        private Long tenantId;
        private String templateId;
        private Long processId;
        private Long rootPipelineId;
        private LocalDateTime uploadedTime;
        private String batchId;
    }


    @Override
    public boolean executeIf() throws Exception {
        return multipartUpload.getCondition();
    }

}
