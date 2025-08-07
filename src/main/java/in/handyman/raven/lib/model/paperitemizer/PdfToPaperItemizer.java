package in.handyman.raven.lib.model.paperitemizer;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PdfToPaperItemizer {
    public static final String PROCESS_NAME = PipelineName.PAPER_ITEMIZER.getProcessName();
    public static final String PAPER_ITEMIZER_IMAGE_TYPE_RGB = "paper.itemizer.image.type.rgb";
    static final String EXTRACTION_PROCESSING_PAPER_COUNT = "extraction.preprocess.paper.itemizer.processing.paper.count";
    static final String EXTRACTION_PAPER_LIMIT_ACTIVATOR = "extraction.preprocess.paper.itemizer.processing.paper.limiter.activator";
    static final String PAPER_ITEMIZER_RESIZE_WIDTH = "paper.itemizer.resize.width";
    static final String PAPER_ITEMIZER_RESIZE_HEIGHT = "paper.itemizer.resize.height";
    static final String PAPER_ITEMIZER_FILE_FORMAT = "paper.itemizer.output.format";
    static final String PAPER_ITEMIZER_FILE_DPI = "paper.itemizer.file.dpi";
    static final String MODEL_NAME = "APP";
    static final String VERSION = "1";
    public final String EXTENSION_PDF = "pdf";
    private final ActionExecutionAudit action;
    private final Logger log;


    public PdfToPaperItemizer(ActionExecutionAudit action, Logger log) {
        this.action = action;
        this.log = log;
    }

    public List<PaperItemizerOutputTable> paperItemizer(
            String fileOrDirPath, String outputDir, PaperItemizerInputTable entity) {
        List<PaperItemizerOutputTable> allResults = new ArrayList<>();
        File input = new File(fileOrDirPath);

        if (!input.exists()) {
            throw new HandymanException("Input path does not exist: " + fileOrDirPath);
        }

        // List of PDF files to process (one file, or all in dir)
        List<File> filesToProcess = new ArrayList<>();
        if (input.isFile()) {
            filesToProcess.add(input);
        } else if (input.isDirectory()) {
            File[] files = input.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
            if (files == null || files.length == 0) {
                throw new HandymanException("No PDF files found in directory: " + fileOrDirPath);
            }
            filesToProcess.addAll(Arrays.asList(files));
        } else {
            throw new HandymanException("Input path is neither a file nor a directory: " + fileOrDirPath);
        }

        // Outer thread pool: one job per file
        int fileThreads = Math.min(filesToProcess.size(), Runtime.getRuntime().availableProcessors());
        ExecutorService docExecutor = Executors.newFixedThreadPool(fileThreads);
        List<Future<List<PaperItemizerOutputTable>>> futures = new ArrayList<>();

        for (File pdfFile : filesToProcess) {
            futures.add(docExecutor.submit(() -> processSingleFile(pdfFile, outputDir, entity)));
        }

        // Collect results
        for (Future<List<PaperItemizerOutputTable>> fut : futures) {
            try {
                allResults.addAll(fut.get());
            } catch (Exception e) {
                log.error("Failed processing file: " + e.getMessage(), e);
            }
        }

        docExecutor.shutdown();
        try {
            docExecutor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.warn("File processing pool interrupted", e);
            docExecutor.shutdownNow();
        }

        return allResults;
    }

    private List<PaperItemizerOutputTable> processSingleFile(File pdfFile, String outputDir, PaperItemizerInputTable entity)
    {
        long startTime = System.currentTimeMillis();
        final String IMAGE_TYPE = action.getContext().get(PAPER_ITEMIZER_IMAGE_TYPE_RGB);
        List<PaperItemizerOutputTable> parentObj = new ArrayList<>();
        File targetDir = readDirectory(outputDir, log);

        String fileExtension = getFileExtension(pdfFile).toLowerCase();
        log.info("The input file has Origin ID {} and file extension {}", entity.getOriginId(), fileExtension);

        if (!fileExtension.equals(EXTENSION_PDF)) {
            throw new HandymanException("Unsupported file type: " + fileExtension);
        }

        ExecutorService executor = null;
        try (PDDocument document = PDDocument.load(pdfFile)) {
            int dpi = Integer.parseInt(action.getContext().get(PAPER_ITEMIZER_FILE_DPI));
            String fileFormat = action.getContext().get(PAPER_ITEMIZER_FILE_FORMAT);
            boolean resizeActive = Objects.equals("true", action.getContext().get("paper.itemization.resize.activator"));
            ImageType imageType = Objects.equals("true", IMAGE_TYPE) ? ImageType.RGB : ImageType.GRAY;

            String originalFileName = pdfFile.getName();
            String fileNameWithoutExtension = removeExtension(originalFileName);

            assert targetDir != null;
            Path combinedtargetDirPath = Paths.get(targetDir.toString(), fileNameWithoutExtension);
            targetDir = new File(String.valueOf(combinedtargetDirPath));
            boolean created = targetDir.mkdirs();
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                throw new HandymanException("Failed to create output directory: " + targetDir);
            }

            log.info("Status for directory creation : {} for file {}", created ? "Successful" : "Failed", fileNameWithoutExtension);

            int pageCount = document.getNumberOfPages();
            int resizeWidth = resizeActive ? Integer.parseInt(action.getContext().get(PAPER_ITEMIZER_RESIZE_WIDTH)) : 0;
            int resizeHeight = resizeActive ? Integer.parseInt(action.getContext().get(PAPER_ITEMIZER_RESIZE_HEIGHT)) : 0;

            int threadCount = Integer.parseInt(action.getContext().getOrDefault("paper.itemizer.consumer.API.count", "1"));
            executor = Executors.newFixedThreadPool(threadCount);
            List<Future<PaperItemizerOutputTable>> futures = new ArrayList<>();
            final File tempTargetDir = targetDir;
            final PDFRenderer renderer = new PDFRenderer(document);

            for (int page = 0; page < pageCount; page++) {
                final int currentPage = page;
                futures.add(executor.submit(() -> {
                    long startPageTime = System.currentTimeMillis();
                    String threadName = Thread.currentThread().getName();
                    try {
                        log.info("Thread {}: Processing page {} started", threadName, currentPage);

                        BufferedImage image;
                        // Only rendering must be synchronized
                        synchronized (document) {
                            image = renderer.renderImageWithDPI(currentPage, dpi, imageType);
                        }
                        if (resizeActive) {
                            image = resizeImage(image, resizeWidth, resizeHeight);
                        }
                        String pageFileName = String.format("%s_%d.%s", fileNameWithoutExtension, currentPage, fileFormat);
                        File outputFile = new File(tempTargetDir, pageFileName);
                        ImageIO.write(image, fileFormat, outputFile);

                        long endPageTime = System.currentTimeMillis();
                        log.info("Thread {}: Page {} processed in {} ms", threadName, currentPage, (endPageTime - startPageTime));

                        return PaperItemizerOutputTable.builder()
                                .processedFilePath(outputFile.toString())
                                .originId(entity.getOriginId())
                                .groupId(entity.getGroupId())
                                .templateId(entity.getTemplateId())
                                .tenantId(entity.getTenantId())
                                .processId(entity.getProcessId())
                                .paperNo((long) currentPage + 1)
                                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                                .stage(PROCESS_NAME)
                                .message("Paper Itemize macro completed")
                                .createdOn(CreateTimeStamp.currentTimestamp())
                                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                .rootPipelineId(entity.getRootPipelineId())
                                .modelName(MODEL_NAME)
                                .modelVersion(VERSION)
                                .batchId(entity.getBatchId())
                                .request("")
                                .response("")
                                .endpoint("")
                                .build();
                    } catch (Exception e) {
                        log.error("Thread {}: Error processing page {}: {}", threadName, currentPage, e.getMessage(), e);
                        return PaperItemizerOutputTable.builder()
                                .paperNo((long) currentPage + 1)
                                .status("FAILED")
                                .message("Failed: " + e.getMessage())
                                .originId(entity.getOriginId())
                                .groupId(entity.getGroupId())
                                .templateId(entity.getTemplateId())
                                .tenantId(entity.getTenantId())
                                .processId(entity.getProcessId())
                                .stage(PROCESS_NAME)
                                .build();
                    }
                }));
            }

            for (Future<PaperItemizerOutputTable> future : futures) {
                try {
                    parentObj.add(future.get());
                } catch (Exception e) {
                    log.error("Failed to process a page future: {}", e.getMessage(), e);
                }
            }

            executor.shutdown();
            boolean finished = executor.awaitTermination(10, TimeUnit.MINUTES);
            if (!finished) {
                log.warn("Executor didn't finish in time for file {}", originalFileName);
            }

            long endTime = System.currentTimeMillis();
            log.info("Total elapsed time for paperItemizer: {} ms", (endTime - startTime));
        } catch (Exception e) {
            log.error("Error during paper itemization: {}", e.getMessage(), e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(
                    "paper itemization consumer failed for origin Id " + entity.getOriginId(),
                    handymanException, action
            );
        } finally {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdownNow();
            }
        }
        return parentObj;
    }



    private int getPageNo(int originalPaperCount) {
        String extractionPaperLimitActivator = action.getContext().get(EXTRACTION_PAPER_LIMIT_ACTIVATOR);
        int extractionProcessingPaperCount = Integer.parseInt(action.getContext().get(EXTRACTION_PROCESSING_PAPER_COUNT));

        if (extractionPaperLimitActivator.equals("true")) {
            return Math.min(originalPaperCount, extractionProcessingPaperCount);
        } else {
            return originalPaperCount;
        }

    }

    private static String removeExtension(final String fileName) {
        if (fileName.indexOf(".") > 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }
    }

    public static String getFileExtension(final File file) {
        final String name = file.getName();
        int lastIndexOf = name.indexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf + 1);
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.drawImage(originalImage, 0, 0, width, height, null);
        graphics.dispose();
        return resizedImage;
    }

    private static File readDirectory(String outputDir, Logger log) {
        try {
            Path combinedPath = Paths.get(outputDir, "pdf_to_image");
            File targetDir = combinedPath.toFile();

            if (targetDir.mkdirs() || targetDir.exists()) {
                log.info("Directory is ready: {}", targetDir.getAbsolutePath());
            } else {
                log.error("Failed to create directory: {}", targetDir.getAbsolutePath());
            }
            return targetDir;

        } catch (Exception e) {
            log.error("Error handling the directory: {}", e.getMessage(), e);
            return null;
        }

    }
}