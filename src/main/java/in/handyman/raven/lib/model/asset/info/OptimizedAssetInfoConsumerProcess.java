package in.handyman.raven.lib.model.asset.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.AssetInfo;
import in.handyman.raven.lib.model.AssetInfoInputTable;
import in.handyman.raven.lib.model.AssetInfoOutputTable;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import org.slf4j.Logger;
import org.slf4j.Marker;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_TEXT_EXTRACTION_OUTPUT;
import static java.lang.Math.random;

/**
 * Optimized Consumer process with async processing and parallel streams
 */
public class OptimizedAssetInfoConsumerProcess  {
//    private final Logger log;
//    private final Marker marker;
//    private final ActionExecutionAudit action;
//    private final AssetInfo assetInfo;
//    private final Long tenantId;
//
//    // Thread pool for async operations
//    private final ExecutorService fileProcessingExecutor;
//    private final ExecutorService ioExecutor;
//
//    // Performance tuning constants
//    private static final int PARALLEL_THRESHOLD = 100;
//    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
//    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
//    private static final int QUEUE_CAPACITY = 1000;
//
//    public OptimizedAssetInfoConsumerProcess(Logger log, Marker marker, ActionExecutionAudit action,
//                                             AssetInfo assetInfo, Long tenantId) {
//        this.log = log;
//        this.marker = marker;
//        this.action = action;
//        this.assetInfo = assetInfo;
//        this.tenantId = tenantId;
//
//        // Create optimized thread pools
//        this.fileProcessingExecutor = new ThreadPoolExecutor(
//                CORE_POOL_SIZE, MAX_POOL_SIZE, 60L, TimeUnit.SECONDS,
//                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
//                r -> new Thread(r, "FileProcessor-" + System.currentTimeMillis()),
//                new ThreadPoolExecutor.CallerRunsPolicy()
//        );
//
//        this.ioExecutor = new ThreadPoolExecutor(
//                CORE_POOL_SIZE, MAX_POOL_SIZE, 60L, TimeUnit.SECONDS,
//                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
//                r -> new Thread(r, "IOProcessor-" + System.currentTimeMillis()),
//                new ThreadPoolExecutor.CallerRunsPolicy()
//        );
//    }
//
//    @Override
//    public List<AssetInfoOutputTable> process(URL endpoint, AssetInfoInputTable input) throws Exception {
//        try {
//            log.info(marker, "Starting async processing for file path: {}", input.getFilePath());
//            final String filePathString = Optional.ofNullable(input.getFilePath()).map(String::valueOf).orElse("[]");
//            final String batchId = Optional.ofNullable(input.getBatchId()).map(String::valueOf).orElse("[]");
//
//            File file = new File(filePathString);
//
//            if (file.isDirectory()) {
//                return processDirectoryAsync(file, tenantId, batchId);
//            } else if (file.isFile()) {
//                return Collections.singletonList(processFileAsync(file, tenantId, batchId).get());
//            }
//
//            return Collections.emptyList();
//
//        } catch (Exception e) {
//            log.error(marker, "Error in async file processing for {}", input.getFilePath(), e);
//            insertSummaryAudit(ResourceAccess.rdbmsJDBIConn(assetInfo.getResourceConn()),
//                    0, 0, 1, "Error in file processing: " + e.getMessage(), tenantId);
//            return Collections.emptyList();
//        }
//    }
//
//    /**
//     * Process directory using parallel streams with async file processing
//     */
//    private List<AssetInfoOutputTable> processDirectoryAsync(File directory, Long tenantId, String batchId) {
//        try {
//            log.info(marker, "Processing directory with parallel streams: {}", directory.getPath());
//
//            // Get all files using parallel stream
//            List<Path> allFiles = Files.walk(directory.toPath())
//                    .parallel() // Enable parallel processing
//                    .filter(Files::isRegularFile)
//                    .filter(path -> {
//                        // Filter out hidden files and common non-processable files
//                        String fileName = path.getFileName().toString().toLowerCase();
//                        return !fileName.startsWith(".") &&
//                                !fileName.endsWith(".tmp") &&
//                                !fileName.endsWith(".lock");
//                    })
//                    .collect(Collectors.toList());
//
//            log.info(marker, "Found {} files to process in directory", allFiles.size());
//
//            // Process files in batches using CompletableFuture
//            List<AssetInfoOutputTable> results = processFilesInBatches(allFiles, tenantId, batchId);
//
//            log.info(marker, "Completed processing {} files from directory", results.size());
//            return results;
//
//        } catch (IOException e) {
//            log.error(marker, "Error walking directory: {}", directory.getPath(), e);
//            throw new HandymanException("Error walking directory", e, action);
//        }
//    }
//
//    /**
//     * Process files in batches using CompletableFuture for optimal performance
//     */
//    private List<AssetInfoOutputTable> processFilesInBatches(List<Path> files, Long tenantId, String batchId) {
//        int batchSize = Math.max(10, files.size() / CORE_POOL_SIZE); // Dynamic batch sizing
//        List<AssetInfoOutputTable> allResults = new ArrayList<>();
//
//        // Split files into batches
//        for (int i = 0; i < files.size(); i += batchSize) {
//            List<Path> batch = files.subList(i, Math.min(i + batchSize, files.size()));
//
//            // Process batch asynchronously
//            List<CompletableFuture<AssetInfoOutputTable>> futures = batch.stream()
//                    .map(path -> processFileAsync(path.toFile(), tenantId, batchId))
//                    .collect(Collectors.toList());
//
//            // Wait for batch completion and collect results
//            List<AssetInfoOutputTable> batchResults = futures.stream()
//                    .map(this::safeGet)
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//
//            allResults.addAll(batchResults);
//
//            log.debug(marker, "Completed batch {}-{}, processed {} files",
//                    i, Math.min(i + batchSize, files.size()), batchResults.size());
//        }
//
//        return allResults;
//    }
//
//    /**
//     * Async file processing with CompletableFuture
//     */
//    private CompletableFuture<AssetInfoOutputTable> processFileAsync(File file, Long tenantId, String batchId) {
//        return CompletableFuture
//                .supplyAsync(() -> {
//                    try {
//                        return createFileInfoBuilder(file, tenantId, batchId);
//                    } catch (Exception e) {
//                        log.error(marker, "Error processing file: {}", file.getPath(), e);
//                        return null;
//                    }
//                }, fileProcessingExecutor)
//                .thenCompose(builder -> {
//                    if (builder == null) return CompletableFuture.completedFuture(null);
//
//                    // Async I/O operations
//                    CompletableFuture<String> checksumFuture = calculateChecksumAsync(file);
//                    CompletableFuture<String> base64Future = createBase64Async(file);
//                    CompletableFuture<DimensionInfo> dimensionFuture = calculateDimensionsAsync(file);
//
//                    // Combine all async operations
//                    return CompletableFuture.allOf(checksumFuture, base64Future, dimensionFuture)
//                            .thenApply(v -> {
//                                try {
//                                    String checksum = checksumFuture.get();
//                                    String base64 = base64Future.get();
//                                    DimensionInfo dimensions = dimensionFuture.get();
//
//                                    return builder.toBuilder()
//                                            .fileChecksum(checksum)
//                                            .encode(encryptRequestResponse(base64))
//                                            .width(dimensions.width)
//                                            .height(dimensions.height)
//                                            .dpi(dimensions.dpi)
//                                            .build();
//                                } catch (Exception e) {
//                                    log.error(marker, "Error combining async results for: {}", file.getPath(), e);
//                                    return null;
//                                }
//                            });
//                })
//                .exceptionally(throwable -> {
//                    log.error(marker, "Async processing failed for file: {}", file.getPath(), throwable);
//                    return null;
//                });
//    }
//
//    /**
//     * Create basic file info builder (non-I/O operations)
//     */
//    private AssetInfoOutputTable createFileInfoBuilder(File file, Long tenantId, String batchId) {
//        String fileExtension = FilenameUtils.getExtension(file.getName());
//        String fileAbsolutePath = file.getAbsolutePath();
//        String decodedFileName = decodeFileName(file.getName());
//        var fileSize = file.length() / 1024;
//        String fileId = FilenameUtils.removeExtension(file.getName()) + "_" + ((int) (900000 * random() + 100000));
//
//        return AssetInfoOutputTable.builder()
//                .fileId(fileId)
//                .tenantId(tenantId)
//                .fileExtension(fileExtension)
//                .fileName(FilenameUtils.removeExtension(file.getName()))
//                .decodedFileName(FilenameUtils.removeExtension(decodedFileName))
//                .filePath(fileAbsolutePath)
//                .fileSize(String.valueOf(fileSize))
//                .rootPipelineId(Long.valueOf(action.getContext().get("pipeline-id")))
//                .processId(Long.valueOf(action.getContext().get("process-id")))
//                .batchId(batchId)
//                .build();
//    }
//
//    /**
//     * Async checksum calculation
//     */
//    private CompletableFuture<String> calculateChecksumAsync(File file) {
//        return CompletableFuture.supplyAsync(() -> {
//            try (InputStream is = Files.newInputStream(file.toPath())) {
//                return org.apache.commons.codec.digest.DigestUtils.sha1Hex(is);
//            } catch (IOException e) {
//                log.error(marker, "Error calculating checksum for: {}", file.getPath(), e);
//                return "";
//            }
//        }, ioExecutor);
//    }
//
//    /**
//     * Async base64 creation
//     */
//    private CompletableFuture<String> createBase64Async(File file) {
//        return CompletableFuture.supplyAsync(() -> {
//            String fileExtension = FilenameUtils.getExtension(file.getName());
//            if ("pdf".equalsIgnoreCase(fileExtension)) {
//                return ""; // Skip base64 for PDFs
//            }
//
//            try {
//                byte[] imageBytes = Files.readAllBytes(file.toPath());
//                return Base64.getEncoder().encodeToString(imageBytes);
//            } catch (Exception e) {
//                log.error(marker, "Error creating base64 for: {}", file.getPath(), e);
//                return "";
//            }
//        }, ioExecutor);
//    }
//
//    /**
//     * Async dimension calculation
//     */
//    private CompletableFuture<DimensionInfo> calculateDimensionsAsync(File file) {
//        return CompletableFuture.supplyAsync(() -> {
//            String fileExtension = FilenameUtils.getExtension(file.getName());
//
//            if ("pdf".equalsIgnoreCase(fileExtension)) {
//                return calculatePdfDimensions(file);
//            } else {
//                return calculateImageDimensions(file);
//            }
//        }, ioExecutor);
//    }
//
//    private DimensionInfo calculatePdfDimensions(File file) {
//        try (PDDocument document = PDDocument.load(file)) {
//            PDPage firstPage = document.getPage(0);
//            float pageWidth = firstPage.getMediaBox().getWidth();
//            float pageHeight = firstPage.getMediaBox().getHeight();
//            float widthInches = pageWidth / 72;
//            int dpi = (int) (pageWidth / widthInches);
//            return new DimensionInfo(pageWidth, pageHeight, dpi);
//        } catch (IOException e) {
//            log.error(marker, "Error calculating PDF dimensions for: {}", file.getPath(), e);
//            return new DimensionInfo(0, 0, 0);
//        }
//    }
//
//    private DimensionInfo calculateImageDimensions(File file) {
//        try {
//            BufferedImage image = ImageIO.read(file);
//            if (image != null) {
//                float width = image.getWidth();
//                float height = image.getHeight();
//                float widthInches = width / 72;
//                int dpi = (int) (width / widthInches);
//                return new DimensionInfo(width, height, dpi);
//            }
//        } catch (Exception e) {
//            log.error(marker, "Error calculating image dimensions for: {}", file.getPath(), e);
//        }
//        return new DimensionInfo(0, 0, 0);
//    }
//
//    /**
//     * Safe get for CompletableFuture results
//     */
//    private AssetInfoOutputTable safeGet(CompletableFuture<AssetInfoOutputTable> future) {
//        try {
//            return future.get(30, TimeUnit.SECONDS); // Timeout after 30 seconds
//        } catch (Exception e) {
//            log.error(marker, "Error getting future result", e);
//            return null;
//        }
//    }
//
//    // Helper classes and methods
//    @Data
//    @AllArgsConstructor
//    private static class DimensionInfo {
//        private final float width;
//        private final float height;
//        private final int dpi;
//    }
//
//    private void insertSummaryAudit(final Jdbi jdbi, int rowCount, int executeCount, int errorCount, String comments, Long tenantId) {
//        try {
//            String batchId = action.getContext().get("batch_id");
//            SanitarySummary summary = SanitarySummary.builder()
//                    .rowCount(rowCount)
//                    .correctRowCount(executeCount)
//                    .errorRowCount(errorCount)
//                    .comments(comments)
//                    .tenantId(tenantId)
//                    .batchId(batchId)
//                    .build();
//            jdbi.useTransaction(handle -> {
//                Update update = handle.createUpdate("INSERT INTO " + assetInfo.getAuditTable() +
//                        " (row_count, correct_row_count, error_row_count, comments, created_at, tenant_id, batch_id) " +
//                        " VALUES(:rowCount, :correctRowCount, :errorRowCount, :comments, NOW(), :tenantId, :batchId)");
//                Update bindBean = update.bindBean(summary);
//                bindBean.execute();
//            });
//        } catch (Exception exception) {
//            log.error(marker, "Error inserting into batch insert audit {}", ExceptionUtil.toString(exception));
//            HandymanException handymanException = new HandymanException(exception);
//            HandymanException.insertException("Error inserting into batch insert audit", handymanException, action);
//        }
//    }
//
//    private String encryptRequestResponse(String request) {
//        String encryptReqRes = action.getContext().get(ENCRYPT_TEXT_EXTRACTION_OUTPUT);
//        String requestStr;
//        if ("true".equals(encryptReqRes)) {
//            String encryptedRequest = SecurityEngine.getInticsIntegrityMethod(action, log).encrypt(request, "AES256", "BASE64_ENCODED");
//            requestStr = encryptedRequest;
//        } else {
//            requestStr = request;
//        }
//        return requestStr;
//    }
//
//    private static String decodeFileName(String fileName) {
//        try {
//            return URLDecoder.decode(fileName, StandardCharsets.UTF_8);
//        } catch (IllegalArgumentException e) {
//            return fileName;
//        }
//    }
//
//    /**
//     * Cleanup method - call this when shutting down
//     */
//    public void shutdown() {
//        fileProcessingExecutor.shutdown();
//        ioExecutor.shutdown();
//        try {
//            if (!fileProcessingExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
//                fileProcessingExecutor.shutdownNow();
//            }
//            if (!ioExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
//                ioExecutor.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            fileProcessingExecutor.shutdownNow();
//            ioExecutor.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @Data
//    @Builder
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class SanitarySummary {
//        private int rowCount;
//        private int correctRowCount;
//        private int errorRowCount;
//        private String comments;
//        private Long tenantId;
//        private String batchId;
//    }
}