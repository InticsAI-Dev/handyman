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
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
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
    private static final int BINARY_THRESHOLD_VALUE = 230;
    private static final int GAUSSIAN_BLUR_SIZE = 5;
    private static final int PDF_RENDER_DPI = 72;

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
                            fileExtension = java.util.Optional.ofNullable(row.get("file_extension"))
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

    private void processPdf(final org.jdbi.v3.core.Handle handle,
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

        try (PDDocument document = Loader.loadPDF(new java.io.File(pdfPath))) {
            final PDFRenderer renderer = new PDFRenderer(document);
            final int totalPages = document.getNumberOfPages();

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                final long startTime = System.currentTimeMillis();

                final BufferedImage pageImage = renderer.renderImageWithDPI(pageIndex, PDF_RENDER_DPI,
                        ImageType.GRAY);
                final Mat mat = bufferedImageToMat(pageImage);
                final boolean isBlank = isBlankPage(mat);
                mat.release();
                final long execMs = System.currentTimeMillis() - startTime;

                insertResult(handle, insertQuery, originId, groupId, tenantId, pdfPath,
                        pageIndex + 1, isBlank, processId, rootPipelineId, batchId, execMs);
            }
        }
    }

    private void processImage(final org.jdbi.v3.core.Handle handle,
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
                pageNo, isBlank, processId, rootPipelineId, batchId, execMs);
    }

    private void insertResult(final org.jdbi.v3.core.Handle handle,
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
            final long execMs) {

        final String status = isBlank ? "BLANK" : "CONTENT";
        final String message = isBlank ? "Page is blank" : "Page has content";
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
                .bind(12, "OPENCV")
                .bind(13, "4.7.0")
                .bind(14, batchId)
                .bind(15, now)
                .bind(16, "")
                .bind(17, "")
                .bind(18, java.util.Optional.ofNullable(blankPageDetection.getEndPoint()).orElse("LOCAL_PDFBOX_OPENCV"))
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