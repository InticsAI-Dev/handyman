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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    public List<PaperItemizerOutputTable> paperItemizer(String filePath, String outputDir, PaperItemizerInputTable entity) {

        final String IMAGE_TYPE = action.getContext().get(PAPER_ITEMIZER_IMAGE_TYPE_RGB);
        List<PaperItemizerOutputTable> parentObj = new ArrayList<>();

        File targetDir = readDirectory(outputDir, log);

        // getting file extension
        String fileExtension = getFileExtension(new File(filePath)).toLowerCase();
        log.info("The input file has Origin ID {} and file extension {}", entity.getOriginId(), fileExtension);

        if (fileExtension.equals(EXTENSION_PDF)) {
            try (PDDocument document = PDDocument.load(new File(filePath))) {
                int dpi = Integer.parseInt(action.getContext().get(PAPER_ITEMIZER_FILE_DPI));
                String fileFormat = action.getContext().get(PAPER_ITEMIZER_FILE_FORMAT);
                boolean resizeActive = Objects.equals("true", action.getContext().get("paper.itemization.resize.activator"));

                PDFRenderer pdfRenderer = new PDFRenderer(document);
                String originalFileName = new File(filePath).getName();
                String fileNameWithoutExtension = removeExtension(originalFileName);

                assert targetDir != null;
                Path combinedtargetDirPath = Paths.get(targetDir.toString(), fileNameWithoutExtension);
                targetDir = new File(String.valueOf(combinedtargetDirPath));
                boolean created = targetDir.mkdirs();
                if (!targetDir.exists() && !targetDir.mkdirs()) {
                    new HandymanException("Failed to create output directory: " + targetDir);
                }

                log.info("Status for directory creation : {} for file {}", created ? "Successful" : "Failed", fileNameWithoutExtension);

                int pageCount = getPageNo(document.getNumberOfPages());

                File outputFile = null;
                Integer pageNumber = 0;
                ImageType imageType = Objects.equals("true", IMAGE_TYPE) ? ImageType.RGB : ImageType.GRAY;

                List<PaperItemizerOutputTable> paperItemizerOutputTables = new ArrayList<>();
                for (int page = 0; page < pageCount; page++) {
                    BufferedImage image = pdfRenderer.renderImageWithDPI(page, dpi, imageType);
                    if (resizeActive) {
                        int resizeWidth = Integer.parseInt(action.getContext().get(PAPER_ITEMIZER_RESIZE_WIDTH));
                        int resizeHeight = Integer.parseInt(action.getContext().get(PAPER_ITEMIZER_RESIZE_HEIGHT));

                        log.info("Resizing the image to width: {}, height: {}", resizeWidth, resizeHeight);
                        image = resizeImage(image, resizeWidth, resizeHeight);
                        log.info("Resized the page {} for OriginId: {}", page, entity.getOriginId());
                    }
                    // Save image
                    String pageFileName = String.format("%s_%d.%s", fileNameWithoutExtension, page, fileFormat);
                    outputFile = new File(targetDir, pageFileName);
                    pageNumber = page + 1;
                    ImageIO.write(image, fileFormat, outputFile);
                    log.info("Page {} successfully saved for document ID: {}", page, entity.getOriginId());
                    try {
                        paperItemizerOutputTables.add(PaperItemizerOutputTable
                                .builder()
                                .processedFilePath(String.valueOf(outputFile))
                                .originId(entity.getOriginId())
                                .groupId(entity.getGroupId())
                                .templateId(entity.getTemplateId())
                                .tenantId(entity.getTenantId())
                                .processId(entity.getProcessId())
                                .paperNo(Long.valueOf(pageNumber))
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
                                .build());
                    } catch (Exception exception) {
                        parentObj.add(
                                PaperItemizerOutputTable
                                        .builder()
                                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                        .groupId(entity.getGroupId())
                                        .processId(entity.getProcessId())
                                        .templateId(entity.getTemplateId())
                                        .tenantId(entity.getTenantId())
                                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                        .stage(PROCESS_NAME)
                                        .message(exception.getMessage())
                                        .createdOn(entity.getCreatedOn())
                                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                        .rootPipelineId(entity.getRootPipelineId())
                                        .batchId(entity.getBatchId())
                                        .request("")
                                        .response("Error In getting Response from copro")
                                        .endpoint("")
                                        .build());
                        HandymanException handymanException = new HandymanException(exception);
                        HandymanException.insertException("Paper Itemize consumer failed for origin Id " + entity.getOriginId(), handymanException, action);
                        log.error("The Exception occurred in request {}", exception.getMessage(), exception);
                    }
                    log.info("Itemized papers successfully created in folder: {} ", targetDir.getAbsolutePath());

                }
                document.close();
                parentObj.addAll(paperItemizerOutputTables);
            } catch (Exception e) {
                log.error("Error during paper itemization: {}", e.getMessage());
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("paper itemization consumer failed for origin Id " + entity.getOriginId(), handymanException, action);

            }
        } else {
            new HandymanException("Unsupported file type: " + fileExtension);
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
