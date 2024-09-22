package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AssetInfo;
import in.handyman.raven.util.CommonQueryUtil;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.Math.random;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "AssetInfo"
)
public class AssetInfoAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final AssetInfo assetInfo;

    private final Marker aMarker;

    private final Integer writeBatchSize = 1000;

    public AssetInfoAction(final ActionExecutionAudit action, final Logger log,
                           final Object assetInfo) {
        this.assetInfo = (AssetInfo) assetInfo;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" AssetInfo:" + this.assetInfo.getName());
    }

    @Override
    public void execute() throws Exception {

        Long tenantId = Long.valueOf(action.getContext().get("tenant_id"));
        try {
            log.info(aMarker, "Asset Info Action for {} has been started", assetInfo.getName());

            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(assetInfo.getResourceConn());
            final List<inputResult> tableInfos = new ArrayList<>();

            jdbi.useTransaction(handle -> {
                final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(assetInfo.getValues());
                AtomicInteger i = new AtomicInteger(0);
                formattedQuery.forEach(sqlToExecute -> {
                    log.info(aMarker, "executing  query {} from index {}", sqlToExecute, i.getAndIncrement());
                    Query query = handle.createQuery(sqlToExecute);
                    ResultIterable<inputResult> resultIterable = query.mapToBean(inputResult.class);
                    List<inputResult> detailList = resultIterable.stream().collect(Collectors.toList());
                    tableInfos.addAll(detailList);
                    log.info(aMarker, "executed query from index {}", i.get());
                });
            });
            List<Path> pathList = new ArrayList<>();
            List<FileInfo> fileInfos = new ArrayList<>();
            tableInfos.forEach(tableInfo -> {
                try {
                    log.info(aMarker, "executing  for the file {}", tableInfo);
                    final String filePathString = Optional.ofNullable(tableInfo.getFilePath()).map(String::valueOf).orElse("[]");
                    final String batchId = Optional.ofNullable(tableInfo.getBatchId()).map(String::valueOf).orElse("");
                    log.info(aMarker, "file path string {}", filePathString);
                    File file = new File(filePathString);
                    log.info(aMarker, "created file {}", file);
                    log.info("check file is directory {}", file.isDirectory());
                    log.info("check file is a file path {}", file.isFile());
                    if (file.isDirectory()) {
                        log.info(aMarker, "File is a directory {}", file);
                        try (var files = Files.walk(Path.of(filePathString)).filter(Files::isRegularFile)) {
                            log.info(aMarker, "Iterating each file in directory {}", files);
                            files.forEach(pathList::add);
                        } catch (IOException e) {
                            log.error(aMarker, "Exception occurred in directory iteration {}", ExceptionUtil.toString(e));
                            throw new HandymanException("Exception occurred in directory iteration", e, action);
                        }
                        pathList.forEach(path -> {
                            log.info(aMarker, "insert query for each file from dir {}", path);
                            fileInfos.add(insertQuery(path.toFile(), tenantId, batchId));
                        });
                    } else if (file.isFile()) {
                        log.info(aMarker, "insert query for file {}", file);
                        fileInfos.add(insertQuery(file, tenantId, batchId));
                    }

                    if (fileInfos.size() == this.writeBatchSize) {
                        log.info(aMarker, "executing  batch {}", fileInfos.size());
                        consumerBatch(jdbi, fileInfos);
                        log.info(aMarker, "executed  batch {}", fileInfos.size());
                        insertSummaryAudit(jdbi, tableInfos.size(), fileInfos.size(), 0, "batch inserted", tenantId);
                        fileInfos.clear();
                        log.info(aMarker, "cleared batch {}", fileInfos.size());
                    }
                } catch (Exception e) {
                    log.error(aMarker, "Error in file info for {}", tableInfo, e);
                    HandymanException handymanException = new HandymanException(e);
                    HandymanException.insertException("Error in file info for " + tableInfo, handymanException, action);
                }
            });
            if (!fileInfos.isEmpty()) {
                log.info(aMarker, "executing final batch {}", fileInfos.size());
                consumerBatch(jdbi, fileInfos);
                log.info(aMarker, "executed final batch {}", fileInfos.size());
                insertSummaryAudit(jdbi, tableInfos.size(), fileInfos.size(), 0, "final batch inserted", tenantId);
                fileInfos.clear();
                log.info(aMarker, "cleared final batch {}", fileInfos.size());
            }

        } catch (Exception e) {
            action.getContext().put(assetInfo.getName().concat(".error"), "true");
            log.error(aMarker, "The Exception occurred ", e);
            throw new HandymanException("Exception occurred in asset info execute", e, action);
        }
        log.info(aMarker, "Asset Info Action for {} has been completed", assetInfo.getName());
    }

    public FileInfo insertQuery(File file, Long tenantId, String batchId) {
        FileInfo fileInfoBuilder = new FileInfo();
        try {
            log.info(aMarker, "insert query main caller for the file {}", file);
            String sha1Hex;
            try (InputStream is = Files.newInputStream(Path.of(file.getPath()))) {
                sha1Hex = org.apache.commons.codec.digest.DigestUtils.sha1Hex(is);
            } catch (IOException e) {
                log.error("Error in reading input stream {}", ExceptionUtil.toString(e));
                throw new HandymanException("Error in reading input stream", e, action);
            }
            log.info(aMarker, "checksum for file {} and its {}", file, sha1Hex);
            var fileSize = file.length() / 1024;
            String fileExtension = FilenameUtils.getExtension(file.getName());
            String fileAbsolutePath = file.getAbsolutePath();
            String base64ForPathValue = getBase64ForPath(fileAbsolutePath, fileExtension);
            float pageWidth = 0f;
            float pageHeight = 0f;
            int dpi = 0;
            if (fileExtension.equalsIgnoreCase("pdf")) {
                try (PDDocument document = PDDocument.load(file)) {

                    PDPage firstPage = document.getPage(0);
                    pageWidth = firstPage.getMediaBox().getWidth();
                    pageHeight = firstPage.getMediaBox().getHeight();

                    float width_inches = pageWidth / 72;
                    dpi = (int) (pageWidth / width_inches);
                    log.info("Page width: {}, height: {}, dpi {}", pageWidth, pageHeight, dpi);
                } catch (IOException e) {
                    log.error("Error in calculating width, height, dpi for pdf file with exception {}", e.getMessage());
                }
            } else {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    pageWidth = image.getWidth();
                    pageHeight = image.getHeight();
                    float width_inches = pageWidth / 72;
                    dpi = (int) (pageWidth / width_inches);
                } else {
                    log.error("Error in calculating width, height, dpi for image");
                }
            }
            fileInfoBuilder = FileInfo.builder()
                    .fileId(FilenameUtils.removeExtension(file.getName()) + "_" + ((int) (900000 * random() + 100000)))
                    .tenantId(tenantId)
                    .fileChecksum(sha1Hex)
                    .fileExtension(fileExtension)
                    .fileName(FilenameUtils.removeExtension(file.getName()))
                    .filePath(fileAbsolutePath)
                    .fileSize(String.valueOf(fileSize))
                    .rootPipelineId(Long.valueOf(action.getContext().get("pipeline-id")))
                    .processId(Long.valueOf(action.getContext().get("process-id")))
                    .encode(base64ForPathValue)
                    .width(pageWidth)
                    .height(pageHeight)
                    .dpi(dpi)
                    .batchId(batchId)
                    .build();
            log.info(aMarker, "File Info Builder {}", fileInfoBuilder.getFileName());
        } catch (Exception ex) {
            log.error(aMarker, "error occurred in builder {}", ExceptionUtil.toString(ex));
            throw new HandymanException("Failed to execute {} ", ex, action);
        }
        return fileInfoBuilder;
    }

    void consumerBatch(final Jdbi jdbi, List<FileInfo> resultQueue) {
        Long tenantId = Long.valueOf(action.getContext().get("tenant_id"));
        try {
            resultQueue.forEach(insert -> {
                        jdbi.useTransaction(handle -> {
                            try {
                                handle.createUpdate("INSERT INTO " + assetInfo.getAssetTable() + "(file_id,process_id,root_pipeline_id, file_checksum, file_extension, file_name, file_path, file_size,encode,tenant_id, height, width, dpi, batch_id)" +
                                                "VALUES(:fileId,:processId, :rootPipelineId, :fileChecksum, :fileExtension, :fileName, :filePath, :fileSize,:encode,:tenantId, :height, :width, :dpi, :batchId);")
                                        .bindBean(insert).execute();
                                log.info(aMarker, "inserted {} into source of origin ",insert.filePath);
                            } catch (Throwable t) {
                                insertSummaryAudit(jdbi, 0, 0, 1, "failed in batch for " + insert.getFileName(), tenantId);
                                log.error(aMarker, "error inserting result {}", resultQueue, t);
                            }
                        });
                    }
            );
        } catch (Exception e) {
            insertSummaryAudit(jdbi, 0, 0, resultQueue.size(), "failed in batch insert", tenantId);
            log.error(aMarker, "error inserting result {}", resultQueue, e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("error inserting result" + resultQueue, handymanException, action);
        }
    }

    void insertSummaryAudit(final Jdbi jdbi, int rowCount, int executeCount, int errorCount, String comments, Long tenantId) {
        try {
            String batchId=action.getContext().get("batch_id");
            SanitarySummary summary = new SanitarySummary().builder()
                    .rowCount(rowCount)
                    .correctRowCount(executeCount)
                    .errorRowCount(errorCount)
                    .comments(comments)
                    .tenantId(tenantId)
                    .batchId(batchId)
                    .build();
            jdbi.useTransaction(handle -> {
                Update update = handle.createUpdate("  INSERT INTO " + assetInfo.getAuditTable() +
                        " ( row_count, correct_row_count, error_row_count,comments, created_at,tenant_id,batch_id) " +
                        " VALUES(:rowCount, :correctRowCount, :errorRowCount, :comments, NOW(),:tenantId,:batchId);");
                Update bindBean = update.bindBean(summary);
                bindBean.execute();
            });
        } catch (Exception exception) {
            log.error(aMarker, "error inserting into batch insert audit  {}", ExceptionUtil.toString(exception));
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("error inserting into batch insert audit", handymanException, action);

        }
    }

    public String getBase64ForPath(String imagePath, String fileExtension) throws IOException {
        String base64Image = new String();
        try {
            if (!Objects.equals(fileExtension, "pdf")) {

                // Read the image file into a byte array
                byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));

                // Encode the byte array to Base64
                base64Image = Base64.getEncoder().encodeToString(imageBytes);

                // Print the Base64 encoded string
                log.info(aMarker, "base 64 created for this file {}", imagePath);
            }
        } catch (Exception e) {
            log.error(aMarker, "error occurred in creating base 64 {}", ExceptionUtil.toString(e));
            throw new HandymanException("error occurred in creating base 64 {} ", e, action);
        }

        return base64Image;
    }


    @Override
    public boolean executeIf() throws Exception {
        return assetInfo.getCondition();
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FileInfo {
        private String fileId;
        private Long processId;
        private Long tenantId;
        private Long rootPipelineId;
        private String fileChecksum;
        private String fileExtension;
        private String fileName;
        private String filePath;
        private String fileSize;
        private String encode;
        private Float width;
        private Float height;
        private Integer dpi;
        private String batchId;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SanitarySummary {
        private int rowCount;
        private int correctRowCount;
        private int errorRowCount;
        private String comments;
        private Long tenantId;
        private String batchId;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class inputResult {
        private int inboundId;
        private String createdOn;
        private String createdUserId;
        private String lastUpdatedOn;
        private String lastUpdatedUserId;
        private Long tenantId;
        private String filePath;
        private String documentId;
        private String batchId;
    }
}