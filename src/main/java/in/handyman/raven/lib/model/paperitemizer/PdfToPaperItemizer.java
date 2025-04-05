package in.handyman.raven.lib.model.paperitemizer;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import in.handyman.raven.lib.utils.FileProcessingUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.Logger;
import org.slf4j.MarkerFactory;

public class PdfToPaperItemizer {
    public static final String PROCESS_NAME = PipelineName.PAPER_ITEMIZER.getProcessName();
    private static ActionExecutionAudit action;
    private static Marker aMarker = MarkerFactory.getMarker("HANDYMAN_ACTION");

    public static List<PaperItemizerOutputTable> paperItemizer(String filePath, String outputDir, ActionExecutionAudit action, Logger log, PaperItemizerInputTable entity) {

        final String PAPER_ITEMIZER_RESIZE_WIDTH = "paper.itemizer.resize.width";
        final String PAPER_ITEMIZER_RESIZE_HEIGHT = "paper.itemizer.resize.height";
        final String PAPER_ITEMIZER_FILE_FORMAT = "paper.itemizer.output.format";
        final String PAPER_ITEMIZER_FILE_DPI = "paper.itemizer.file.dpi";
        final String MODEL_NAME = "APP";
        final String VERSION = "1";
        final String IMAGE_TYPE = action.getContext().get("paper.itemizer.image.type.rgb");
        List<PaperItemizerOutputTable> parentObj = new ArrayList<>();

        File targetDir = readDirectory(outputDir, log);

        // getting file extension
        String fileExtension = getFileExtension(new File(filePath)).toLowerCase();
        log.info("The input file has Origin ID {} and file extension {}", entity.getOriginId(), fileExtension);

       if (fileExtension.equals("pdf")) {
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
                    new HandymanException("Failed to create output directory: " + targetDir );
                }

                log.info("Status for directory creation : {} for file {}", created ? "Successful" : "Failed", fileNameWithoutExtension);

                int pageCount = document.getNumberOfPages();
                File outputFile = null;
                Integer pageNumber = 0;
                ImageType imageType = Objects.equals("true", IMAGE_TYPE)? ImageType.RGB: ImageType.GRAY;

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

                    String croppedImagePath = getCroppedImage(action, log, outputFile, fileFormat);

                    log.info("Page {} successfully saved in {} for document ID: {}", page,croppedImagePath, entity.getOriginId());
                    try {
                        paperItemizerOutputTables.add(PaperItemizerOutputTable
                                .builder()
                                .processedFilePath(croppedImagePath)
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
                    }catch (Exception exception) {
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
                        HandymanException.insertException("Paper Itemize consumer failed for originId " + entity.getOriginId(), handymanException, action);
                        log.error("The Exception occurred in request {}", exception.getMessage(), exception);
                    }
                    log.info("Itemized papers successfully created in folder: {} ", targetDir.getAbsolutePath());

                }
                document.close();
                parentObj.addAll(paperItemizerOutputTables);
            } catch (Exception e) {
                log.error("Error during paper itemization: {}", e.getMessage());
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException( "paper itemization consumer failed for {}"+ entity.getOriginId(), handymanException, action);

            }
        } else {
            new HandymanException("Unsupported file type: " + fileExtension);
        }
        return parentObj;
    }

    @NotNull
    private static String getCroppedImage(ActionExecutionAudit action, Logger log, File outputFile, String fileFormat) throws IOException, InterruptedException {
        FileProcessingUtils fileUtils = new FileProcessingUtils(log, aMarker, action);
        String base64 = fileUtils.convertFileToBase64(outputFile.getAbsolutePath());

        String croppedBase64 = fileUtils.callCropImageApi(base64);

        String croppedImagePath = outputFile.getAbsolutePath().replace("." + fileFormat, "_cropped." + fileFormat);
        fileUtils.convertBase64ToFile(croppedBase64, croppedImagePath);
        return croppedImagePath;
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
