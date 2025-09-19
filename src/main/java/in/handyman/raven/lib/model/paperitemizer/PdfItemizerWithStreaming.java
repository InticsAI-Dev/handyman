package in.handyman.raven.lib.model.paperitemizer;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class PdfItemizerWithStreaming {
    public static final String PROCESS_NAME = PipelineName.PAPER_ITEMIZER.getProcessName();
    public static final String PAPER_ITEMIZER_IMAGE_TYPE_RGB = "paper.itemizer.image.type.rgb";
    static final String EXTRACTION_PROCESSING_PAPER_COUNT = "extraction.preprocess.paper.itemizer.processing.paper.count";
    static final String EXTRACTION_PAPER_LIMIT_ACTIVATOR = "extraction.preprocess.paper.itemizer.processing.paper.limiter.activator";
    static final String PAPER_ITEMIZER_RESIZE_WIDTH = "paper.itemizer.resize.width";
    static final String PAPER_ITEMIZER_RESIZE_HEIGHT = "paper.itemizer.resize.height";
    static final String PAPER_ITEMIZER_OUTPUT_FORMAT = "paper.itemizer.output.format";
    static final String PAPER_ITEMIZER_FILE_DPI = "paper.itemizer.file.dpi";
    static final String PAPER_ITEMIZATION_RESIZE_ACTIVATOR = "paper.itemization.resize.activator";
    static final String MODEL_NAME = "APP";
    static final String VERSION = "1";
    private final ActionExecutionAudit action;
    private final Logger log;


    public PdfItemizerWithStreaming(ActionExecutionAudit action, Logger log) {
        this.action = action;
        this.log = log;
    }

    public List<PaperItemizerOutputTable> paperItemizer(String filePath, String outputDir, PaperItemizerInputTable entity) throws IOException {
        log.info("Starting paper itemization for file: {} with output directory: {}", filePath, outputDir);
        final Timestamp startTime = Timestamp.valueOf(LocalDateTime.now());
        checkOutputDir(outputDir, "Output directory is null or empty for originId {}", entity, "Output directory cannot be null or empty");

        checkOutputDir(filePath, "File path is null or empty for originId {}", entity, "File path cannot be null or empty");
        final List<PaperItemizerOutputTable> parentObj = new ArrayList<>();

        final File targetDir = createOutputDirectory(outputDir);

        final String fileExtension = getExtension(filePath);
        final String originId = entity.getOriginId();
        if (PdfExtensions.EXTENSION_PDF.getEType().equalsIgnoreCase(fileExtension)) {
            log.debug("Processing PDF file: {} for originId {}", filePath, originId);
            itemisePdfIntoPapersWithStreaming(parentObj, entity, targetDir.getPath(), filePath, startTime);

        } else if (PdfExtensions.EXTENSION_JPEG.getEType().equalsIgnoreCase(fileExtension) || PdfExtensions.EXTENSION_JPG.getEType().equalsIgnoreCase(fileExtension) || PdfExtensions.EXTENSION_PNG.getEType().equalsIgnoreCase(fileExtension)) {
            log.debug("Processing image file: {} for originId {}", filePath, originId);
            PaperItemizerOutputTable paperItemizerOutputTable = getPaperItemizeCompletedOutput(entity, new File(entity.getFilePath()), 0, 1, startTime);
            parentObj.add(paperItemizerOutputTable);
        } else {
            log.error("Unsupported file type: {} for originId {}", fileExtension, originId);
            PaperItemizerOutputTable paperItemizerOutputTable = getPaperItemizeFailedOutput(entity, 0, startTime);
            parentObj.add(paperItemizerOutputTable);
            HandymanException handymanException = new HandymanException(new NoSuchElementException());
            HandymanException.insertException("Unsupported file type: " + fileExtension, handymanException, action);
        }
        log.info("Paper itemization completed for file: {} with output directory: {}", filePath, outputDir);
        return parentObj;
    }

    private void checkOutputDir(String outputDir, String s, PaperItemizerInputTable entity, String outputDirError) {
        log.debug("Checking output directory: {} for originId {}", outputDir, entity.getOriginId());
        if (outputDir == null || outputDir.isEmpty()) {
            log.error(s, entity.getOriginId());
            HandymanException handymanException = new HandymanException(new IllegalArgumentException(outputDirError));
            HandymanException.insertException(outputDirError, handymanException, action);
        }

    }

    public String getExtension(String filePath) {
        log.debug("Extracting file extension from path: {}", filePath);
        if (filePath != null && filePath.lastIndexOf('.') != -1) {
            return filePath.substring(filePath.lastIndexOf('.') + 1);
        }
        return ""; // No extension
    }

    private String getContextVariableWithDefault(String variableName, String defaultValue) {
        return action.getContext().getOrDefault(variableName, defaultValue);
    }

    private PaperItemizerOutputTable getPaperItemizeCompletedOutput(PaperItemizerInputTable entity, File outputFile, long currentPage, int pageCount, Timestamp startTime) {
        log.debug("Creating completed output for paper itemization with file: {} for originId: {}", outputFile.getAbsolutePath(), entity.getOriginId());
        return PaperItemizerOutputTable.builder()
                .processedFilePath(outputFile.toString())
                .originId(entity.getOriginId())
                .groupId(entity.getGroupId())
                .templateId(entity.getTemplateId())
                .tenantId(entity.getTenantId())
                .processId(entity.getProcessId())
                .paperNo(currentPage+1)
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(PROCESS_NAME)
                .message("Paper Itemize macro completed to process the file with page numbers " + pageCount)
                .createdOn(startTime)
                .lastUpdatedOn(Timestamp.valueOf(CreateTimeStamp.currentTimestamp().toLocalDateTime()))
                .rootPipelineId(entity.getRootPipelineId())
                .modelName(MODEL_NAME)
                .modelVersion(VERSION)
                .batchId(entity.getBatchId())
                .request("")
                .response("")
                .endpoint("")
                .build();
    }

    private void itemisePdfIntoPapersWithStreaming(List<PaperItemizerOutputTable> parentObj, PaperItemizerInputTable entity, String basePath, String pdfPath, Timestamp startTime) throws IOException {
        log.debug("Itemizing PDF into papers for file: {} with base path: {}", pdfPath, basePath);
        final String IMAGE_TYPE = getContextVariableWithDefault(PAPER_ITEMIZER_IMAGE_TYPE_RGB, "GRAY");
        final String imageResizeEnable = getContextVariableWithDefault(PAPER_ITEMIZATION_RESIZE_ACTIVATOR, "GRAY");
        final boolean imageResizeEnableSetting = Boolean.parseBoolean(imageResizeEnable);
        final String dpi = getContextVariableWithDefault(PAPER_ITEMIZER_FILE_DPI, "300");
        final int imageDpiSetting = Integer.parseInt(dpi);
        final String imageWidth = getContextVariableWithDefault(PAPER_ITEMIZER_RESIZE_WIDTH, "2550");
        final int imageWidthSetting = Integer.parseInt(imageWidth);
        final String imageHeight = getContextVariableWithDefault(PAPER_ITEMIZER_RESIZE_HEIGHT, "2550");
        final int imageHeightSetting = Integer.parseInt(imageHeight);

        Path path = Paths.get(pdfPath);

        try (PDDocument document = Loader.loadPDF(Files.readAllBytes(path))) {
            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(true);

            final String originalName = getFileNameFromPath(pdfPath);
            final String normalizedFormat = getContextVariableWithDefault(PAPER_ITEMIZER_OUTPUT_FORMAT, "jpg").toLowerCase();
            final int pageCount = getPageNo(document.getNumberOfPages());

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                ImageType imageType = ImageType.RGB.toString().equalsIgnoreCase(IMAGE_TYPE) ? ImageType.RGB : ImageType.GRAY;
                BufferedImage image = renderer.renderImageWithDPI(i, imageDpiSetting, imageType);
                if (imageResizeEnableSetting) {
                    log.debug("Resizing image for page {} of file: {}", i + 1, originalName);
                    BufferedImage bufferedImage = resizeImage(image, imageWidthSetting, imageHeightSetting);
                    writeOutputItemizedImages(parentObj, entity, basePath, originalName, i, bufferedImage, normalizedFormat, pageCount, startTime);

                } else {
                    log.debug("Resize operation was turned off and Writing image for page {} of file: {} without", i + 1, originalName);
                    writeOutputItemizedImages(parentObj, entity, basePath, originalName, i, image, normalizedFormat, pageCount, startTime);
                }
                image.flush();
                image = null;

            }
        }
        log.info("Completed itemizing PDF into papers for file: {} with base path: {}", pdfPath, basePath);
    }

    private void writeOutputItemizedImages(List<PaperItemizerOutputTable> parentObj, PaperItemizerInputTable entity, String basePath, String originalName, int i, BufferedImage image, String normalizedFormat, int pageCount, Timestamp startTime) throws IOException {
        log.debug("Writing output itemized image for page {} of file: {}", i + 1, originalName);
        String fileNameWithoutExtension = removeExtension(originalName);
        final String fileName = fileNameWithoutExtension + "_" + (i + 1) + "." + normalizedFormat;
        final String folderName = fileNameWithoutExtension;
        final Path out = createOutputFile(basePath, fileName, folderName);

        if (!ImageIO.write(image, normalizedFormat, out.toFile())) {
            PaperItemizerOutputTable paperItemizerOutputTable = getPaperItemizeFailedOutput(entity, pageCount, startTime);
            parentObj.add(paperItemizerOutputTable);
            final String imageWriteError = "Failed to write image : " + out.getFileName();
            log.error(imageWriteError);
            HandymanException handymanException = new HandymanException(new IOException(imageWriteError));
            HandymanException.insertException(imageWriteError, handymanException, action);
        } else {
            log.debug("Successfully wrote image to file: {}", out.toFile().getAbsolutePath());
            PaperItemizerOutputTable paperItemizerOutputTable = getPaperItemizeCompletedOutput(entity, out.toFile(), i, pageCount, startTime);
            parentObj.add(paperItemizerOutputTable);
        }

        image.flush();
        image = null;
        log.debug("Successfully wrote output itemized image for page {} of file: {}", i + 1, originalName);
    }

    private Path createOutputFile(String basePath, String fileName, String originalFileName) throws IOException {
        log.debug("Creating output file with name: {} in base path: {}", fileName, basePath);
        Path outputDir = Paths.get(basePath, "processed", originalFileName);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        return outputDir.resolve(fileName);
    }


    public String getFileNameFromPath(String filePath) {
        log.debug("Extracting file name from path: {}", filePath);
        // Convert the file path string to a Path object
        Path path = Paths.get(filePath);

        // Get the file name from the Path object (the last part of the path)
        String fileName = path.getFileName().toString();

        return fileName;
    }

    private PaperItemizerOutputTable getPaperItemizeFailedOutput(PaperItemizerInputTable entity, int pageCount, Timestamp startTime) {
        log.error("Paper itemization failed for file: {} with page count: {}", entity.getFilePath(), pageCount);
        return PaperItemizerOutputTable.builder()
                .processedFilePath(entity.getFilePath())
                .originId(entity.getOriginId())
                .groupId(entity.getGroupId())
                .templateId(entity.getTemplateId())
                .tenantId(entity.getTenantId())
                .processId(entity.getProcessId())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(PROCESS_NAME)
                .message("Paper Itemize macro failed to process the file with page numbers " + pageCount)
                .createdOn(startTime)
                .lastUpdatedOn(Timestamp.valueOf(CreateTimeStamp.currentTimestamp().toLocalDateTime()))
                .rootPipelineId(entity.getRootPipelineId())
                .modelName(MODEL_NAME)
                .modelVersion(VERSION)
                .batchId(entity.getBatchId())
                .request("")
                .response("")
                .endpoint("")
                .build();
    }


    private int getPageNo(int originalPaperCount) {
        String extractionPaperLimitActivator = action.getContext().get(EXTRACTION_PAPER_LIMIT_ACTIVATOR);
        int extractionProcessingPaperCount = Integer.parseInt(action.getContext().get(EXTRACTION_PROCESSING_PAPER_COUNT));
        log.info("Extraction paper limit activator: {}, extraction processing paper count: {}, original paper count: {}",
                extractionPaperLimitActivator, extractionProcessingPaperCount, originalPaperCount);
        if ("true".equalsIgnoreCase(extractionPaperLimitActivator)) {
            return Math.min(originalPaperCount, extractionProcessingPaperCount);
        } else {
            return originalPaperCount;
        }

    }

    private String removeExtension(final String fileName) {
        log.debug("Removing extension from file name: {}", fileName);
        if (fileName.indexOf(".") > 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }
    }

    public String getFileExtension(final File file) {
        final String name = file.getName();
        int lastIndexOf = name.indexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf + 1);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        log.debug("Resizing image from original size {}x{} to new size {}x{}", originalImage.getWidth(), originalImage.getHeight(), width, height);
        BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.drawImage(originalImage, 0, 0, width, height, null);
        graphics.dispose();
        return resizedImage;
    }

    private File createOutputDirectory(String outputDir) {
        log.debug("Creating output directory at: {}", outputDir);
        try {
            Path combinedPath = Paths.get(outputDir, "pdf_to_image");
            File targetDir = combinedPath.toFile();

            boolean createDir = targetDir.mkdirs();
            if (createDir || targetDir.exists()) {
                log.debug("Directory is ready: {}", targetDir.getAbsolutePath());
            } else {
                log.error("Failed to create directory: {}", targetDir.getAbsolutePath());
            }
            return targetDir;

        } catch (Exception e) {
            log.error("Error handling the directory: {}", e.getMessage(), e);
            HandymanException exception = new HandymanException(e);
            throw new HandymanException("Error creating output directory: " + outputDir, exception, action);
        }

    }
}