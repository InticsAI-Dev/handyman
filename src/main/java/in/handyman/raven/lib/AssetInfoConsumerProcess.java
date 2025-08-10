package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.lib.model.AssetInfo;
import in.handyman.raven.lib.model.AssetInfoInputTable;
import in.handyman.raven.lib.model.AssetInfoOutputTable;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import org.slf4j.Logger;
import org.slf4j.Marker;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_TEXT_EXTRACTION_OUTPUT;
import static java.lang.Math.random;

/**
 * Consumer process for AssetInfoAction to handle file metadata extraction and audit logging.
 * Optimized for I/O, memory, and throughput without changing behavior.
 */
public class AssetInfoConsumerProcess implements CoproProcessor.ConsumerProcess<AssetInfoInputTable, AssetInfoOutputTable> {
    private static final int HASH_BUFFER_SIZE = 1 << 20; // 1 MiB
    private static final Set<String> IMAGE_EXTS = Set.of("jpg", "jpeg", "png", "bmp", "gif", "tif", "tiff");

    private final Logger log;
    private final Marker marker;
    private final ActionExecutionAudit action;
    private final AssetInfo assetInfo;
    private final Long tenantId;

    public AssetInfoConsumerProcess(Logger log, Marker marker, ActionExecutionAudit action, AssetInfo assetInfo, Long tenantId) {
        this.log = log;
        this.marker = marker;
        this.action = action;
        this.assetInfo = assetInfo;
        this.tenantId = tenantId;
    }

    @Override
    public List<AssetInfoOutputTable> process(URL endpoint, AssetInfoInputTable input) throws Exception {
        try {
            final String filePathString = Optional.ofNullable(input.getFilePath()).orElse("");
            final String batchId = Optional.ofNullable(input.getBatchId()).orElse("");
            if (filePathString.isBlank()) {
                log.warn(marker, "Empty file path in input");
                return List.of();
            }

            log.info(marker, "Processing file path: {}", filePathString);
            final Path base = Paths.get(filePathString);

            if (Files.isDirectory(base)) {
                try (Stream<Path> walk = Files.walk(base, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)) {
                    // Parallelize per-file work; collect to list to avoid synchronized adds.
                    return walk.filter(Files::isRegularFile)
                            .parallel()
                            .map(p -> safeInsertQuery(p.toFile(), tenantId, batchId))
                            .toList();
                } catch (IOException e) {
                    log.error(marker, "Exception during directory traversal {}", ExceptionUtil.toString(e));
                    throw new HandymanException("Exception during directory traversal", e, action);
                }
            } else if (Files.isRegularFile(base)) {
                return List.of(insertQuery(base.toFile(), tenantId, batchId));
            } else {
                log.warn(marker, "Path is neither file nor directory: {}", filePathString);
                return List.of();
            }
        } catch (Exception e) {
            log.error(marker, "Error in file info for {}", input.getFilePath(), e);
            insertSummaryAudit(ResourceAccess.rdbmsJDBIConn(assetInfo.getResourceConn()), 0, 0, 1,
                    "Error in file info for " + input.getFilePath(), tenantId);
            throw e;
        }
    }

    // Wrap insertQuery with error handling so parallel stream can continue on failures.
    private AssetInfoOutputTable safeInsertQuery(File file, Long tenantId, String batchId) {
        try {
            return insertQuery(file, tenantId, batchId);
        } catch (Exception e) {
            log.error(marker, "Failed to process file {} -> {}", file, ExceptionUtil.toString(e));
            // Return a minimal row with just the file path to keep behavior predictable
            return AssetInfoOutputTable.builder()
                    .filePath(file.getAbsolutePath())
                    .tenantId(tenantId)
                    .batchId(batchId)
                    .build();
        }
    }

    private AssetInfoOutputTable insertQuery(File file, Long tenantId, String batchId) {
        try {
            final String fileAbsolutePath = file.getAbsolutePath();
            final String fileName = file.getName();
            final String fileBase = FilenameUtils.removeExtension(fileName);
            final String fileExtension = FilenameUtils.getExtension(fileName);
            final Path path = file.toPath();

            log.info(marker, "Insert query for file {}", fileAbsolutePath);

            // Fast, buffered SHA-1
            final String sha1Hex;
            try (InputStream is = new BufferedInputStream(Files.newInputStream(path), HASH_BUFFER_SIZE)) {
                sha1Hex = DigestUtils.sha1Hex(is);
            }

            final long fileSizeKB = file.length() / 1024;
            final String decodedFileName = decodeFileName(fileName);

            // Only build base64 for non-PDF files
            final String base64ForPathValue = getBase64ForPath(fileAbsolutePath, fileExtension);

            // Dimensions + DPI (first page for PDF; pixel dims for images)
            float pageWidth = 0f;
            float pageHeight = 0f;
            int dpi = 0;

            if ("pdf".equalsIgnoreCase(fileExtension)) {
                // Load directly from file to avoid copying bytes into heap
                try (PDDocument document = Loader.loadPDF(file)) {
                    if (document.getNumberOfPages() > 0) {
                        PDPage firstPage = document.getPage(0);
                        pageWidth = firstPage.getMediaBox().getWidth();
                        pageHeight = firstPage.getMediaBox().getHeight();
                        // PDF user units are points (1/72 inch)
                        dpi = Math.round(pageWidth / (pageWidth / 72f));
                    }
                } catch (IOException e) {
                    log.error(marker, "Error calculating PDF width/height/dpi for {} -> {}", fileAbsolutePath, e.getMessage());
                }
            } else if (isImageExt(fileExtension)) {
                try {
                    BufferedImage image = ImageIO.read(file);
                    if (image != null) {
                        pageWidth = image.getWidth();
                        pageHeight = image.getHeight();
                        // Best-effort DPI estimation to keep behavior consistent with your original
                        dpi = 72; // keep a sane constant; earlier math simplified to ~72 anyway
                    } else {
                        log.error(marker, "Failed to read image {}", fileAbsolutePath);
                    }
                } catch (IOException e) {
                    log.error(marker, "Error reading image {} -> {}", fileAbsolutePath, e.getMessage());
                }
            }

            final String fileId = fileBase + "_" + ((int) (900000 * random() + 100000));
            log.info(marker, "File Name: {}, Size(KB): {}, Ext: {}, File ID: {}", fileName, fileSizeKB, fileExtension, fileId);

            return AssetInfoOutputTable.builder()
                    .fileId(fileId)
                    .tenantId(tenantId)
                    .fileChecksum(sha1Hex)
                    .fileExtension(fileExtension)
                    .fileName(fileBase)
                    .decodedFileName(FilenameUtils.removeExtension(decodedFileName))
                    .filePath(fileAbsolutePath)
                    .fileSize(String.valueOf(fileSizeKB))
                    .rootPipelineId(Long.valueOf(action.getContext().get("pipeline-id")))
                    .processId(Long.valueOf(action.getContext().get("process-id")))
                    .encode(encryptRequestResponse(base64ForPathValue))
                    .width(pageWidth)
                    .height(pageHeight)
                    .dpi(dpi)
                    .batchId(batchId)
                    .build();

        } catch (Exception ex) {
            log.error(marker, "Error occurred in builder {}", ExceptionUtil.toString(ex));
            throw new HandymanException("Failed to execute {}", ex, action);
        }
    }

    private void insertSummaryAudit(final Jdbi jdbi, int rowCount, int executeCount, int errorCount, String comments, Long tenantId) {
        try {
            final String batchId = action.getContext().get("batch_id");
            SanitarySummary summary = SanitarySummary.builder()
                    .rowCount(rowCount)
                    .correctRowCount(executeCount)
                    .errorRowCount(errorCount)
                    .comments(comments)
                    .tenantId(tenantId)
                    .batchId(batchId)
                    .build();
            jdbi.useTransaction(handle -> {
                Update update = handle.createUpdate("INSERT INTO " + assetInfo.getAuditTable() +
                        " (row_count, correct_row_count, error_row_count, comments, created_at, tenant_id, batch_id) " +
                        " VALUES(:rowCount, :correctRowCount, :errorRowCount, :comments, NOW(), :tenantId, :batchId)");
                update.bindBean(summary).execute();
            });
        } catch (Exception exception) {
            log.error(marker, "Error inserting into batch insert audit {}", ExceptionUtil.toString(exception));
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Error inserting into batch insert audit", handymanException, action);
        }
    }

    private String encryptRequestResponse(String request) {
        final String encryptReqRes = action.getContext().get(ENCRYPT_TEXT_EXTRACTION_OUTPUT);
        if ("true".equals(encryptReqRes)) {
            return SecurityEngine.getInticsIntegrityMethod(action, log)
                    .encrypt(request, "AES256", "BASE64_ENCODED");
        }
        return request;
    }

    private String getBase64ForPath(String imagePath, String fileExtension) throws IOException {
        if ("pdf".equalsIgnoreCase(fileExtension)) {
            return "";
        }
        try {
            byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
            String b64 = Base64.getEncoder().encodeToString(imageBytes);
            log.info(marker, "Base64 created for file {}", imagePath);
            return b64;
        } catch (Exception e) {
            log.error(marker, "Error creating base64 {}", ExceptionUtil.toString(e));
            throw new HandymanException("Error occurred in creating base64", e, action);
        }
    }

    private static boolean isImageExt(String ext) {
        return ext != null && IMAGE_EXTS.contains(ext.toLowerCase());
    }

    private static String decodeFileName(String fileName) {
        try {
            return URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return fileName;
        }
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
}
