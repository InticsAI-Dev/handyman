package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.BlankPageDetection;
import nu.pattern.OpenCV;
import org.opencv.imgcodecs.Imgcodecs;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import java.util.Optional;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * Action to detect blank pages in PDF files using OpenCV.
 */
@ActionExecution(actionName = "BlankPageDetection")
public class BlankPageDetectionAction implements IActionExecution {

    static {
        OpenCV.loadLocally();
    }

    private static final double BLANK_INK_RATIO_THRESHOLD = 0.005; // 0.5%
    private static final int MIN_SIGNIFICANT_CONTENT = 50;
    private static final int BINARY_THRESHOLD_VALUE = 230;
    private static final int GAUSSIAN_BLUR_SIZE = 5;
    private static final int PDF_RENDER_DPI = 72;
    private static final String PHOTON_BLANK_PAGE_DETECTION_ACTIVATOR = "photon.blank.page.detection.activator";
    private static final String PHOTON_NAME = "PHOTON";
    private static final String NEON_NAME = "NEON";
    private static final String PHOTON_VERSION = "4.7.0";
    private static final String NEON_VERSION = "1.0.0";

    private static final String INSERT_COLUMNS = "origin_id,group_id,tenant_id,template_id,processed_file_path,paper_no,"
            +
            "status,stage,message,created_on,process_id,root_pipeline_id," +
            "model_name,model_version,batch_id,last_updated_on," +
            "request,response,endpoint,execution_time,is_blank";

    private static final String INSERT_VALUES = "VALUES (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?,?)";

    private final ActionExecutionAudit action;
    private final Logger log;
    private final BlankPageDetection blankPageDetection;
    private final Marker marker;

    public BlankPageDetectionAction(final ActionExecutionAudit action,
            final Logger log,
            final Object blankPageDetection) {
        this.blankPageDetection = (BlankPageDetection) blankPageDetection;
        this.action = action;
        this.log = log;
        this.marker = MarkerFactory.getMarker("BlankPageDetection:" + this.blankPageDetection.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            log.info(marker, "Starting BlankPageDetection execution");
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(blankPageDetection.getResourceConn());
            log.info(marker, "Database connection established: {}", blankPageDetection.getResourceConn());

            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));

            final String insertQuery = "INSERT INTO " + blankPageDetection.getResultTable() +
                    "(" + INSERT_COLUMNS + ") " +
                    INSERT_VALUES;

            final String query = blankPageDetection.getQuerySet();

            log.info(marker, "Executing query: {}", query);
            log.info(marker, "Result table: {}", blankPageDetection.getResultTable());

            final int[] rowCount = { 0 };

            jdbi.useHandle(handle -> handle.createQuery(query)
                    .mapToMap()
                    .forEach(row -> {
                        rowCount[0]++;
                        log.info(marker, "Processing row #{}", rowCount[0]);

                        final String originId = String.valueOf(row.get("origin_id"));
                        final String batchId = String.valueOf(row.get("batch_id"));
                        final int groupId = Integer
                                .parseInt(String.valueOf(row.get("group_id")));
                        final long tenantId = Long
                                .parseLong(String.valueOf(row.get("tenant_id")));
                        String pdfPath = String.valueOf(row.get("file_path"));
                        final Object processIdObj = row.get("process_id");
                        final Object rootPipelineIdObj = row.get("root_pipeline_id");

                        if (pdfPath == null || pdfPath.equals("null") || pdfPath.trim().isEmpty()) {
                            pdfPath = String.valueOf(row.get("processed_file_path"));
                        }

                        if (pdfPath == null || pdfPath.equals("null")
                                || pdfPath.trim().isEmpty()) {
                            log.warn(marker, "Skipping NULL or empty file_path for origin_id: {}",
                                    originId);
                            return;
                        }

                        if (!Files.exists(Paths.get(pdfPath))) {
                            log.error(marker, "File not found: {} for origin_id: {}",
                                    pdfPath, originId);
                            throw new HandymanException("File not found: " + pdfPath,
                                    new RuntimeException("File not found"), action);
                        }

                        String fileExtension = "";
                        if (pdfPath.contains(".")) {
                            fileExtension = pdfPath.substring(pdfPath.lastIndexOf(".") + 1).toLowerCase();
                        }

                        if (fileExtension.isEmpty()) {
                            fileExtension = Optional.ofNullable(row.get("file_extension"))
                                    .map(String::valueOf)
                                    .map(String::toLowerCase)
                                    .orElse("");
                        }

                        log.info(marker, "Processing file: {} with extension: {} for origin_id: {}", pdfPath,
                                fileExtension,
                                originId);

                        int pageNumber = 1;
                        if (row.containsKey("paper_no") && row.get("paper_no") != null) {
                            try {
                                pageNumber = Integer.parseInt(String.valueOf(row.get("paper_no")));
                            } catch (NumberFormatException e) {
                                // Ignore and fall back to filename parsing
                            }
                        }

                        if (pageNumber == 1 && pdfPath.matches(".*_\\d+\\.[a-zA-Z]+$")) {
                            try {
                                String numberPart = pdfPath.substring(pdfPath.lastIndexOf("_") + 1,
                                        pdfPath.lastIndexOf("."));
                                pageNumber = Integer.parseInt(numberPart);
                            } catch (Exception e) {
                                // Ignore
                            }
                        }

                        try {
                            if (fileExtension.contains("pdf")) {
                                processPdf(
                                        handle,
                                        insertQuery,
                                        pdfPath,
                                        originId,
                                        groupId,
                                        tenantId,
                                        batchId,
                                        processIdObj,
                                        rootPipelineIdObj);
                            } else if (fileExtension.contains("png") || fileExtension.contains("jpeg")
                                    || fileExtension.contains("jpg")) {
                                processImage(
                                        handle,
                                        insertQuery,
                                        pdfPath,
                                        originId,
                                        groupId,
                                        tenantId,
                                        batchId,
                                        processIdObj,
                                        rootPipelineIdObj,
                                        pageNumber);
                            } else {
                                log.warn(marker, "Unsupported or missing file extension: {} for file: {}",
                                        fileExtension, pdfPath);
                            }

                        } catch (Exception ex) {
                            log.error(marker, "Failed blank detection for file: {}", pdfPath,
                                    ex);
                            throw new HandymanException(
                                    "Failed blank detection for file: " + pdfPath,
                                    ex, action);
                        }

                        log.info(marker, "Completed processing for origin_id: {}", originId);
                    }));

            log.info(marker, "BlankPageDetection completed successfully. Total rows processed: {}",
                    rowCount[0]);

            if (rowCount[0] == 0) {
                log.warn(marker, "No rows returned from query. Check your query conditions.");
            }

        } catch (Exception e) {
            log.error(marker, "Error in BlankPageDetectionAction", e);
            throw new HandymanException("Error in BlankPageDetectionAction", e, action);
        }
    }

    private void processPdf(final Handle handle,
            final String insertQuery,
            final String pdfPath,
            final String originId,
            final int groupId,
            final long tenantId,
            final String batchId,
            final Object processIdObj,
            final Object rootPipelineIdObj) throws Exception {

        final long processId = Long.parseLong(String.valueOf(processIdObj));
        final long rootPipelineId = Long.parseLong(String.valueOf(rootPipelineIdObj));

        try (PDDocument document = Loader.loadPDF(new File(pdfPath))) {
            final int totalPages = document.getNumberOfPages();

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                final long startTime = System.currentTimeMillis();

                final String activator = this.action.getContext().getOrDefault(PHOTON_BLANK_PAGE_DETECTION_ACTIVATOR,
                        "FALSE");
                final boolean usePhoton = "TRUE".equalsIgnoreCase(activator);

                final boolean isBlank;
                final String modelName;
                final String modelVersion;

                if (usePhoton) {
                    final PDFRenderer renderer = new PDFRenderer(document);
                    final BufferedImage pageImage = renderer.renderImageWithDPI(pageIndex, PDF_RENDER_DPI,
                            ImageType.GRAY);
                    final Mat mat = bufferedImageToMat(pageImage);
                    isBlank = isBlankPage(mat);
                    mat.release();
                    modelName = PHOTON_NAME;
                    modelVersion = PHOTON_VERSION;
                } else {
                    isBlank = isPageBlankUltraFast(document.getPage(pageIndex));
                    modelName = NEON_NAME;
                    modelVersion = NEON_VERSION;
                }

                final long execMs = System.currentTimeMillis() - startTime;

                insertResult(handle, insertQuery, originId, groupId, tenantId, pdfPath,
                        pageIndex + 1, isBlank, processId, rootPipelineId, batchId, execMs, modelName, modelVersion,
                        "PDF");
            }
        }
    }

    private boolean isPageBlankUltraFast(final PDPage page) throws IOException {
        // 1. Check if Images/XObjects Exist
        final PDResources resources = page.getResources();
        if (resources != null) {
            if (resources.getXObjectNames().iterator().hasNext()) {
                return false; // Not blank
            }
        }

        // 2. Check Content Stream Bytes
        try (InputStream stream = page.getContents()) {
            if (stream == null) {
                return true;
            }

            final byte[] buffer = new byte[4096];
            int totalRead = 0;
            int bytesRead;

            boolean hasText = false;
            boolean hasImage = false;
            boolean hasDraw = false;

            while ((bytesRead = stream.read(buffer)) != -1) {
                totalRead += bytesRead;

                for (int i = 0; i < bytesRead - 1; i++) {
                    final byte b1 = buffer[i];
                    final byte b2 = buffer[i + 1];

                    // Text Operators (Tj / TJ)
                    if ((b1 == 'T' && b2 == 'j') ||
                            (b1 == 'T' && b2 == 'J')) {
                        hasText = true;
                        break;
                    }

                    // Image Operators (Do / BI)
                    if ((b1 == 'D' && b2 == 'o') ||
                            (b1 == 'B' && b2 == 'I')) {
                        hasImage = true;
                        break;
                    }

                    // Drawing Operators (S / f / B)
                    if (isWhitespace(b2)) {
                        if (b1 == 'S' || b1 == 's' ||
                                b1 == 'f' || b1 == 'F' ||
                                b1 == 'B' || b1 == 'b') {
                            hasDraw = true;
                            break;
                        }
                    }
                }

                if (hasText || hasImage || hasDraw) {
                    break;
                }
            }

            if (hasText || hasImage) {
                return false;
            }

            if (hasDraw && totalRead > MIN_SIGNIFICANT_CONTENT) {
                return false;
            }

            return totalRead < MIN_SIGNIFICANT_CONTENT;
        }
    }

    private static boolean isWhitespace(final byte b) {
        return b == ' ' || b == '\n' || b == '\r' || b == '\t';
    }

    private void processImage(final Handle handle,
            final String insertQuery,
            final String filePath,
            final String originId,
            final int groupId,
            final long tenantId,
            final String batchId,
            final Object processIdObj,
            final Object rootPipelineIdObj,
            final int pageNo) throws Exception {

        final long processId = Long.parseLong(String.valueOf(processIdObj));
        final long rootPipelineId = Long.parseLong(String.valueOf(rootPipelineIdObj));

        final long startTime = System.currentTimeMillis();

        final Mat mat = Imgcodecs.imread(filePath, Imgcodecs.IMREAD_GRAYSCALE);
        if (mat.empty()) {
            throw new HandymanException("Failed to load image from path: " + filePath,
                    new RuntimeException("Image load failed"), action);
        }

        final boolean isBlank = isBlankPage(mat);
        mat.release();
        final long execMs = System.currentTimeMillis() - startTime;

        insertResult(handle, insertQuery, originId, groupId, tenantId, filePath,
                pageNo, isBlank, processId, rootPipelineId, batchId, execMs, PHOTON_NAME, PHOTON_VERSION, "IMAGE");
    }

    private void insertResult(final Handle handle,
            final String insertQuery,
            final String originId,
            final int groupId,
            final long tenantId,
            final String filePath,
            final int pageNo,
            final boolean isBlank,
            final long processId,
            final long rootPipelineId,
            final String batchId,
            final long execMs,
            final String modelName,
            final String modelVersion,
            final String fileType) {

        final String status = isBlank ? "BLANK" : "CONTENT";
        final String message = String.format("Page is %s detected by %s for %s file",
                isBlank ? "blank" : "having content", modelName, fileType);
        final Timestamp now = new Timestamp(System.currentTimeMillis());

        handle.createUpdate(insertQuery)
                .bind(0, originId)
                .bind(1, groupId)
                .bind(2, tenantId)
                .bind(3, "N/A")
                .bind(4, filePath)
                .bind(5, pageNo)
                .bind(6, status)
                .bind(7, "BLANK_PAGE_DETECTION")
                .bind(8, message)
                .bind(9, now)
                .bind(10, processId)
                .bind(11, rootPipelineId)
                .bind(12, modelName)
                .bind(13, modelVersion)
                .bind(14, batchId)
                .bind(15, now)
                .bind(16, "")
                .bind(17, "")
                .bind(18, Optional.ofNullable(blankPageDetection.getEndPoint()).orElse("LOCAL_PDFBOX_OPENCV"))
                .bind(19, execMs + " ms")
                .bind(20, isBlank)
                .execute();

        log.info(marker, "Inserted Blank Detection -> originId={}, page={}, blank={}", originId,
                pageNo, isBlank);
    }

    private boolean isBlankPage(final Mat mat) {
        final Mat resized = new Mat();
        double scale = 1.0;
        if (mat.width() > 1024) {
            scale = 1024.0 / mat.width();
            Imgproc.resize(mat, resized, new Size(), scale, scale, Imgproc.INTER_AREA);
        } else {
            mat.copyTo(resized);
        }

        final Mat blurred = new Mat();
        Imgproc.GaussianBlur(resized, blurred, new Size(GAUSSIAN_BLUR_SIZE, GAUSSIAN_BLUR_SIZE), 0);

        final Mat binary = new Mat();
        Imgproc.threshold(blurred, binary, BINARY_THRESHOLD_VALUE, 255, Imgproc.THRESH_BINARY_INV);

        final int nonZeroPixels = Core.countNonZero(binary);
        final int totalPixels = binary.rows() * binary.cols();
        final double inkRatio = (double) nonZeroPixels / totalPixels;

        blurred.release();
        binary.release();
        resized.release();

        return inkRatio < BLANK_INK_RATIO_THRESHOLD;
    }

    private Mat bufferedImageToMat(final BufferedImage image) {
        final byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
        mat.put(0, 0, data);
        return mat;
    }

    @Override
    public boolean executeIf() throws Exception {
        return blankPageDetection.getCondition();
    }
}