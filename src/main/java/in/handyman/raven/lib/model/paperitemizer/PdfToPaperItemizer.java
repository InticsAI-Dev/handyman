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
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class PdfToPaperItemizer {
    private static final String PROCESS_NAME = PipelineName.PAPER_ITEMIZER.getProcessName();
    private static final String MODEL_NAME = "APP";
    private static final String VERSION = "1";

    public static List<PaperItemizerOutputTable> paperItemizer(String filePath, String outputDir, ActionExecutionAudit action, Logger log, PaperItemizerInputTable entity) {
        List<PaperItemizerOutputTable> parentObj = new ArrayList<>();
        File targetDir = prepareOutputDirectory(outputDir, filePath, log);

        if (targetDir == null) {
            log.error("Target directory creation failed: {}", outputDir);
            return parentObj;
        }

        String fileExtension = getFileExtension(new File(filePath)).toLowerCase();
        log.info("Processing file {} with extension: {}", entity.getOriginId(), fileExtension);

        if (fileExtension.equals("pdf")) {
            processPdf(filePath, targetDir, action, log, entity, parentObj);
        } else {
            log.error("Unsupported file type: {}", fileExtension);
        }

        return parentObj;
    }

    private static void processPdf(String filePath, File targetDir, ActionExecutionAudit action, Logger log, PaperItemizerInputTable entity, List<PaperItemizerOutputTable> parentObj) {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            int dpi = Integer.parseInt(action.getContext().get("paper.itemizer.file.dpi"));
            String fileFormat = action.getContext().get("paper.itemizer.output.format");
            boolean resizeActive = Boolean.parseBoolean(action.getContext().get("paper.itemization.resize.activator"));
            int resizeWidth = Integer.parseInt(action.getContext().get("paper.itemizer.resize.width"));
            int resizeHeight = Integer.parseInt(action.getContext().get("paper.itemizer.resize.height"));

            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            List<Future<PaperItemizerOutputTable>> futures = new ArrayList<>();

            for (int page = 0; page < pageCount; page++) {
                final int pageIndex = page;
                futures.add(executor.submit(() -> processPdfPage(filePath,pdfRenderer, pageIndex, dpi, fileFormat, resizeActive, resizeWidth, resizeHeight, targetDir, entity, log)));
            }

            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.MINUTES);

            for (Future<PaperItemizerOutputTable> future : futures) {
                parentObj.add(future.get());
            }

            log.info("Successfully processed {} pages for file: {}", pageCount, filePath);
        } catch (Exception e) {
            log.error("Error processing PDF: {}", e.getMessage(), e);
        }
    }

    private static PaperItemizerOutputTable processPdfPage(String filePath,PDFRenderer pdfRenderer, int pageIndex, int dpi, String fileFormat, boolean resizeActive, int width, int height, File targetDir, PaperItemizerInputTable entity, Logger log) throws IOException {
        BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, dpi, ImageType.RGB);

        if (resizeActive) {
            image = resizeImage(image, width, height);
        }

        String fileNameWithoutExt = removeExtension(new File(filePath).getName());
        File outputFile = new File(targetDir, String.format("%s_%d.%s", fileNameWithoutExt, pageIndex, fileFormat));
        ImageIO.write(image, fileFormat, outputFile);
        log.info("Page {} saved to {}", pageIndex, outputFile.getAbsolutePath());

        return PaperItemizerOutputTable.builder()
                .processedFilePath(outputFile.getAbsolutePath())
                .originId(entity.getOriginId())
                .groupId(entity.getGroupId())
                .templateId(entity.getTemplateId())
                .tenantId(entity.getTenantId())
                .processId(entity.getProcessId())
                .paperNo((long) (pageIndex + 1))
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(PROCESS_NAME)
                .message("Paper Itemization Completed")
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
    }

    private static File prepareOutputDirectory(String baseOutputDir, String filePath, Logger log) {
        File mainDir = new File(baseOutputDir, "pdf_to_image");
        if (!mainDir.exists() && !mainDir.mkdirs()) {
            log.error("Failed to create main directory: {}", mainDir.getAbsolutePath());
            return null;
        }
        // Extract file name without extension
        String fileNameWithoutExt = removeExtension(new File(filePath).getName());

        // Create the subdirectory for the specific file
        File dir = new File(mainDir, fileNameWithoutExt);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.info("Directory created successfully: {}", dir.getAbsolutePath());
            } else {
                log.error("Failed to create directory: {}", dir.getAbsolutePath());
                return null;
            }
        } else {
            log.info("Directory already exists: {}", dir.getAbsolutePath());
        }

        return dir;
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        return (lastIndex == -1) ? "" : name.substring(lastIndex + 1);
    }

    private static String removeExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return (lastIndex > 0) ? fileName.substring(0, lastIndex) : fileName;
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.drawImage(originalImage, 0, 0, width, height, null);
        graphics.dispose();
        return resizedImage;
    }
}