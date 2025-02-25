package in.handyman.raven.lib.model.paperitemizer;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import javassist.bytecode.stackmap.BasicBlock;
import org.apache.commons.net.bsd.RLoginClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.text.MessageFormat;
import java.util.List;
import org.slf4j.Marker;
import org.slf4j.Logger;

public class PdfToPaperItemizer {
    public static final String PROCESS_NAME = PipelineName.PAPER_ITEMIZER.getProcessName();
    private static ActionExecutionAudit action;

    public static List<PaperItemizerOutputTable> paperItemizer(String filePath, String outputDir, ActionExecutionAudit action, Logger log, PaperItemizerInputTable entity) {

        final String PAPER_ITEMIZER_RESIZE_WIDTH = "paper.itemizer.resize.width";
        final String PAPER_ITEMIZER_RESIZE_HEIGHT = "paper.itemizer.resize.height";
        final String PAPER_ITEMIZER_FILE_FORMAT = "paper.itemizer.output.format";
        final String PAPER_ITEMIZER_FILE_DPI = "paper.itemizer.file.dpi";
        List<PaperItemizerOutputTable> parentObj = new ArrayList<>();


        List<HashMap<Integer, File>> itemizedFilePaths = new ArrayList<>();

        File targetDir = readFile(outputDir, log);

        if (targetDir != null) {
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                String message = "Failed to create directories for path: " + outputDir;
                log.error(message);
                throw new HandymanException(message, new Exception(), action);
            } else {
                log.info("Directory is ready at path: {}", outputDir);
            }
        } else {
            log.error("Target directory is null. Directory creation may have failed for path: {}", outputDir);
        }

        // getting file extension
        String fileExtension = getFileExtension(new File(filePath)).toLowerCase();
        log.info("The input file has Origin ID {} and file extension {}", entity.getOriginId(), fileExtension);
        String outputImageExtension = action.getContext().get("paper.itemizer.file.format");


        if (fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
            log.info("Input file is in jpg/jpeg format");
            File sourceFile = new File(filePath);

            // getting file name without extension
            String fileNameWithoutExtension = removeExtension(sourceFile.getName());
            File outputFile = new File(targetDir, fileNameWithoutExtension + outputImageExtension);

            try {
                Objects.requireNonNull(targetDir, "Target directory is null");
                // creating directory with file ame
                Path combinedTargetDirPath = targetDir.toPath().resolve(fileNameWithoutExtension);
                File combinedTargetDir = combinedTargetDirPath.toFile();

                boolean created = combinedTargetDir.mkdirs();
                log.info("Directory creation {} for file {}", created ? "succeeded" : "failed", fileNameWithoutExtension);
                boolean resizeActive = Objects.equals("true", action.getContext().get("paper.itemization.resize.activator"));

                if (resizeActive) {
                    // Get resize dimensions from context
                    int resizeWidth = Integer.parseInt(action.getContext().get(PAPER_ITEMIZER_RESIZE_WIDTH));
                    int resizeHeight = Integer.parseInt(action.getContext().get(PAPER_ITEMIZER_RESIZE_HEIGHT));
                    log.info("Resizing image to dimensions: {}x{}", resizeWidth, resizeHeight);

                    // Read the image  // Resize the image and  // Save the resized image
                    BufferedImage originalImage = ImageIO.read(sourceFile);
                    BufferedImage resizedImage = resizeImage(originalImage, resizeWidth, resizeHeight);
                    ImageIO.write(resizedImage, outputImageExtension, outputFile);

                    log.info("Resized image successfully saved to: {}", outputFile.getAbsolutePath());
                } else {
                    // Simply copy the original file if no resize needed
                    Files.copy(sourceFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    log.info("Original image copied to: {}", outputFile.getAbsolutePath());
                }

                Files.copy(sourceFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log.info("File successfully copied to: {}", outputFile.getAbsolutePath());

                HashMap<Integer, File> fileMap = new HashMap<>();
                fileMap.put(0, outputFile);
                itemizedFilePaths.add(fileMap);

            } catch (IOException | NullPointerException exception) {
                log.error("Error copying JPG file: {}", exception.getMessage(), exception);
                throw new HandymanException("Error copying JPG file", exception, action);
            }

        } else if (fileExtension.equals("pdf")) {
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

                for (int page = 0; page < pageCount; page++) {
                    BufferedImage image = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);
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
//                    HashMap<Integer, File> fileMap = new HashMap<>();
//                    fileMap.put(page+1, outputFile);
//                    itemizedFilePaths.add(fileMap);

                    //-----------------------------------------------------------------
                    try {
                        parentObj.add(
                                PaperItemizerOutputTable
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
                                        .modelName("APP")
                                        .modelVersion("1")
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
                        HandymanException.insertException("Paper Itemize consumer failed for originId " + entity.getOriginId(), handymanException, action);
                        log.error("The Exception occurred in request {}", exception.getMessage(), exception);
                    }
                    log.info("Itemized papers successfully created in folder: {}", targetDir.getAbsolutePath());

                }
//                    ---------------------------------------------------------------------
                return parentObj;


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

    public static List<PaperItemizerOutputTable> PaperItemizerOuputInsert(PaperItemizerInputTable entity, ActionExecutionAudit action, Logger log, Marker aMarker , List<HashMap<Integer, File>> itemizedPapers){
        log.info("Paper Itemization with app is started");
        List<PaperItemizerOutputTable> parentObj = new ArrayList<>();

        try {
            // Process all itemized papers in a single flattened loop
            for (HashMap<Integer, File> itemizedPaperMap : itemizedPapers) {
                itemizedPaperMap.forEach((key, file) -> {
                    parentObj.add(
                            PaperItemizerOutputTable
                                    .builder()
                                    .processedFilePath(file.getAbsolutePath())
                                    .originId(entity.getOriginId())
                                    .groupId(entity.getGroupId())
                                    .templateId(entity.getTemplateId())
                                    .tenantId(entity.getTenantId())
                                    .processId(entity.getProcessId())
                                    .paperNo(Long.valueOf(key))
                                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                                    .stage(PROCESS_NAME)
                                    .message("Paper Itemize macro completed")
                                    .createdOn(CreateTimeStamp.currentTimestamp())
                                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                    .rootPipelineId(entity.getRootPipelineId())
                                    .modelName("APP")
                                    .modelVersion("1")
                                    .batchId(entity.getBatchId())
                                    .request("")
                                    .response("")
                                    .endpoint("")
                                    .build());
                });
            }
            return parentObj;
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
            HandymanException.insertException("Paper Itemize consumer failed for originId " + entity.getOriginId(), handymanException, action);
            log.error(aMarker, "The Exception occurred in request {}", exception.getMessage(), exception);
        }
        return parentObj;
    }

    private static File readFile(String outputDir, Logger log) {
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
