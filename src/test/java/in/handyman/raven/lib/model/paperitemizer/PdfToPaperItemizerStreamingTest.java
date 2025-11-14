package in.handyman.raven.lib.model.paperitemizer;

import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


@Slf4j
public class PdfToPaperItemizerStreamingTest {
//
//    private ActionExecutionAudit mockAction;
//    private Logger mockLog;
//    private PdfItemizerWithStreaming itemizer;
//
//    @BeforeEach
//    void setUp() {
//        mockAction = new ActionExecutionAudit();
//
//        mockAction.getContext().put("paper.itemizer.image.type.rgb", "RGB");
//        mockAction.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.count", "10");
//        mockAction.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.limiter.activator", "false");
//        mockAction.getContext().put("paper.itemizer.resize.width", "2550");
//        mockAction.getContext().put("paper.itemizer.resize.height", "3301");
//        mockAction.getContext().put("paper.itemizer.output.format", "jpg");
//        mockAction.getContext().put("paper.itemizer.file.dpi", "300");
//        mockAction.getContext().put("paper.itemization.resize.activator", "false");
//
//        itemizer = new PdfItemizerWithStreaming(mockAction, log);
//    }
//
//    @Test
//    public void testValidPdfProcessing() throws Exception {
//        // Prepare a test PDF file
//        String inputFilePath = "src/test/resources/sample.pdf";
//        String outputDir = "target/output_pdf_test";
//        Files.createDirectories(Paths.get(outputDir));
//
//        PaperItemizerInputTable input = new PaperItemizerInputTable();
//        input.setFilePath(inputFilePath);
//        input.setOriginId("ORIGIN-123");
//        input.setOutputDir(outputDir);
//        input.setGroupId(1);
//        input.setTemplateId("1");
//        input.setTenantId(1L);
//        input.setProcessId(1L);
//        input.setRootPipelineId(1L);
//        input.setBatchId("BATCH-1");
//
//        List<PaperItemizerOutputTable> result = itemizer.paperItemizer(inputFilePath, outputDir, input);
//
//        assertFalse(result.isEmpty(), "Output list should not be empty for valid PDF");
//        verify(mockLog, atLeastOnce()).info(contains("Processing PDF file"));
//    }
//
//    @Test
//    public void testValidJpegProcessing() throws Exception {
//        String inputFilePath = "src/test/resources/sample.jpg";
//        String outputDir = "target/output_jpeg_test";
//        Files.createDirectories(Paths.get(outputDir));
//
//        PaperItemizerInputTable input = new PaperItemizerInputTable();
//        input.setFilePath(inputFilePath);
//        input.setOriginId("ORIGIN-456");
//        input.setOutputDir(outputDir);
//        input.setGroupId(1);
//        input.setTemplateId("1");
//        input.setTenantId(1L);
//        input.setProcessId(1L);
//        input.setRootPipelineId(1L);
//        input.setBatchId("BATCH-2");
//
//        List<PaperItemizerOutputTable> result = itemizer.paperItemizer(inputFilePath, outputDir, input);
//
//        assertEquals(1, result.size(), "JPEG should produce a single output record");
//        assertEquals("COMPLETED", result.get(0).getStatus());
//    }
//
//    @Test
//    public void testUnsupportedFileType() throws Exception {
//        String inputFilePath = "src/test/resources/sample.txt";
//        String outputDir = "target/output_txt_test";
//        Files.createDirectories(Paths.get(outputDir));
//
//        PaperItemizerInputTable input = new PaperItemizerInputTable();
//        input.setFilePath(inputFilePath);
//        input.setOriginId("ORIGIN-789");
//        input.setOutputDir(outputDir);
//
//        List<PaperItemizerOutputTable> result = itemizer.paperItemizer(inputFilePath, outputDir, input);
//
//        assertEquals(1, result.size(), "Unsupported file should still produce a failure record");
//        assertEquals("FAILED", result.get(0).getStatus());
////        verify(mockLog).error(contains("Unsupported file type"));
//    }
//
//    @Test
//    public void testNullOutputDirThrowsError() {
//        PaperItemizerInputTable input = new PaperItemizerInputTable();
//        input.setFilePath("src/test/resources/sample.pdf");
//        input.setOriginId("ORIGIN-NULL");
//        input.setOutputDir(null);
//
//        assertDoesNotThrow(() -> {
//            itemizer.paperItemizer(input.getFilePath(), input.getOutputDir(), input);
//        }, "Method should handle null output directory gracefully");
//    }
//
//    @Test
//    public void testPaperLimitActivator() {
//        mockAction.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.count", "5");
//        mockAction.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.limiter.activator", "true");
//
//        int limitedPages = itemizer.getPageNo(10);
//        assertEquals(5, limitedPages, "Page count should be limited to 5");
//    }
//
//    @Test
//    public void testResizeImage() {
//        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
//        BufferedImage resized = itemizer.resizeImage(img, 50, 50);
//
//        assertEquals(50, resized.getWidth());
//        assertEquals(50, resized.getHeight());
//    }

@Test
public void convertBase64FromFileTest() throws IOException {

    ActionExecutionAudit audit = new ActionExecutionAudit();
    Marker marker = MarkerFactory.getMarker("PaperItemizer:");
    String imagePath = "/pdf_to_image/processed/COMM_P2_INREQ_3/COMM_P2_INREQ_3_2.png";
    FileProcessingUtils fileProcessingUtils = new FileProcessingUtils(log, marker, audit);
    String base64Img = fileProcessingUtils.convertFileToBase64(imagePath);

    System.out.println(base64Img);

}
@Test
public void contextLoads() throws IOException {
    Path inputPath = Path.of("/Users/anandh.andrews/intics-workspace/repos-master/handyman/src/test/java/in/handyman/raven/lib/model/paperitemizer/processed/output_image_1762168515583/output_image_1762168515583_5.jpg");
    Path basePath = Path.of("/Users/anandh.andrews/intics-workspace/repos-master/handyman/src/test/java/in/handyman/raven/lib/model/paperitemizer/");
    final String originalName = "MCR_P3_UMR-Medicare Census_01";
    final String normalizedFormat = "jpg";

    ActionExecutionAudit audit = new ActionExecutionAudit();
    Marker marker = MarkerFactory.getMarker("PaperItemizer:");
    FileProcessingUtils fileProcessingUtils = new FileProcessingUtils(log, marker, audit);

    final int imageDpiSetting = 450;
    final boolean imageResizeEnableSetting = false;
    final int tileSize = 448; // InternVL default
    final Timestamp startTime = new Timestamp(System.currentTimeMillis());

    // === Detect file type ===
    String lowerName = inputPath.getFileName().toString().toLowerCase();

    if (lowerName.endsWith(".pdf")) {
        // --- PDF Handling ---
        try (PDDocument document = Loader.loadPDF(Files.readAllBytes(inputPath))) {
            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(false);

            int pageCount = document.getNumberOfPages();
            for (int i = 0; i < pageCount; i++) {
                BufferedImage pageImage = renderer.renderImageWithDPI(i, imageDpiSetting, ImageType.RGB);

                // InternVL dynamic tiling
                List<BufferedImage> tiles = dynamicTileInternVL(pageImage, 1, 12, tileSize, true);
                int tileIndex = 0;

                for (BufferedImage tile : tiles) {
                    if (imageResizeEnableSetting) {
                        tile = resizeImage(tile, tileSize, tileSize);
                    }

                    writeOutputItemizedImages(
                            null, null,
                            basePath, originalName + "_page" + (i + 1),
                            tileIndex, tile, normalizedFormat,
                            pageCount, startTime
                    );

                    Path tilePath = getGeneratedImagePath(basePath, originalName + "_page" + (i + 1), tileIndex, normalizedFormat);

                    if (Files.exists(tilePath)) {
                        callInferenceAPI(tilePath, fileProcessingUtils);
                    }

                    tile.flush();
                    tileIndex++;
                }
                pageImage.flush();
            }
        }

    } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png")) {
        // --- Direct Image Handling ---
        Path imagePath = inputPath;

        if (Files.exists(imagePath)) {
            callInferenceAPI(imagePath, fileProcessingUtils);
        }

    } else {
        throw new IllegalArgumentException("Unsupported file type: " + inputPath);
    }
}
    private void callInferenceAPI(Path imagePath, FileProcessingUtils fileProcessingUtils) throws IOException {
        String base64Img = fileProcessingUtils.convertFileToBase64(imagePath.toString());
        JSONObject payload = buildInferenceRequest(
                "ORIGIN-13049", 1, 130276, 935, 1,
                130276, 1155880, "RADON_KVP_ACTION",
                imagePath.toString(), getUserPrompt(), getSystemPrompt(), "BATCH-935_0",
                1444, base64Img
        );

        String response = sendPostRequest("http://172.202.112.23:8000/predict", payload.toString());
        extractAndPrintInferResponse(response, imagePath);
    }


private void writeOutputItemizedImages(List<PaperItemizerOutputTable> parentObj, PaperItemizerInputTable entity,
                                        Path basePath, String originalPageName, int tileIndex, BufferedImage image,
                                        String normalizedFormat, int pageCount, Timestamp startTime) throws IOException {
// Use timestamp to ensure unique filenames
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = "output_image_" + timestamp + "_" + (tileIndex + 1) + "." + normalizedFormat;


// Create page-specific output folder
        Path outputDir = basePath.resolve("processed").resolve(originalPageName);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        Path outFile = outputDir.resolve(fileName);
        ImageIO.write(image, normalizedFormat, outFile.toFile());
        log.info("✅ Wrote image for page {} tile {} -> {}", originalPageName, tileIndex + 1, outFile.toAbsolutePath());

// Optional: flush image after writing to free memory
        image.flush();


    }

    private Path getGeneratedImagePath(Path basePath, String originalPageName, int tileIndex, String format) throws IOException {
// basePath/.../processed/originalPageName/output_image_*.jpg
        Path pageDir = basePath.resolve("processed").resolve(originalPageName);


        if (!Files.exists(pageDir) || !Files.isDirectory(pageDir)) {
            throw new FileNotFoundException("Page directory not found: " + pageDir);
        }

        try (var stream = Files.list(pageDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().endsWith("_" + (tileIndex + 1) + "." + format))
                    .findFirst()
                    .orElseThrow(() -> new FileNotFoundException(
                            "Tile " + tileIndex + " not found in page: " + originalPageName
                    ));
        }


    }

    /** InternVL-style dynamic tiling */
    private List<BufferedImage> dynamicTileInternVL(BufferedImage image, int minNum, int maxNum, int tileSize, boolean useThumbnail) {
        int ow = image.getWidth();
        int oh = image.getHeight();
        double aspectRatio = (double) ow / oh;


        List<int[]> targetRatios = new ArrayList<>();
        for (int n = minNum; n <= maxNum; n++) {
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    if (i * j >= minNum && i * j <= maxNum) {
                        targetRatios.add(new int[]{i, j});
                    }
                }
            }
        }

// Closest aspect ratio selection
        int[] chosen = targetRatios.get(0);
        double bestDiff = Math.abs(aspectRatio - ((double) chosen[0] / chosen[1]));
        for (int[] ratio : targetRatios) {
            double r = (double) ratio[0] / ratio[1];
            double diff = Math.abs(aspectRatio - r);
            if (diff < bestDiff) {
                bestDiff = diff;
                chosen = ratio;
            }
        }

        int tw = tileSize * chosen[0];
        int th = tileSize * chosen[1];
        int blocks = chosen[0] * chosen[1];

        BufferedImage resized = resizeImage(image, tw, th);
        List<BufferedImage> tiles = new ArrayList<>();
        for (int idx = 0; idx < blocks; idx++) {
            int cols = tw / tileSize;
            int x = (idx % cols) * tileSize;
            int y = (idx / cols) * tileSize;
            tiles.add(resized.getSubimage(x, y, tileSize, tileSize));
        }

        if (useThumbnail && blocks != 1) {
            tiles.add(resizeImage(image, tileSize, tileSize));
        }

        return tiles;


    }

    /** Resize image (bicubic) */
    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    /** Normalize image like InternVL / ImageNet (returns float[3][H][W]) */
    private float[][][] preprocessForInternVL(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        float[][][] tensor = new float[3][h][w];
        final float[] mean = {0.485f, 0.456f, 0.406f};
        final float[] std = {0.229f, 0.224f, 0.225f};


        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                float r = ((rgb >> 16) & 0xFF) / 255f;
                float g = ((rgb >> 8) & 0xFF) / 255f;
                float b = (rgb & 0xFF) / 255f;

                tensor[0][y][x] = (r - mean[0]) / std[0];
                tensor[1][y][x] = (g - mean[1]) / std[1];
                tensor[2][y][x] = (b - mean[2]) / std[2];
            }
        }
        return tensor;


    }




    private JSONObject buildInferenceRequest(String originId, int paperNo, int processId, int groupId, int tenantId,
                                             int rootPipelineId, int actionId, String process, String inputFilePath,
                                             String userPrompt, String systemPrompt, String batchId,
                                             int sorContainerId, String base64Img) {
        JSONObject innerJson = new JSONObject();
        innerJson.put("originId", originId);
        innerJson.put("paperNo", paperNo);
        innerJson.put("processId", processId);
        innerJson.put("groupId", groupId);
        innerJson.put("tenantId", tenantId);
        innerJson.put("rootPipelineId", rootPipelineId);
        innerJson.put("actionId", actionId);
        innerJson.put("process", process);
        innerJson.put("inputFilePath", inputFilePath);
        innerJson.put("userPrompt", userPrompt);
        innerJson.put("systemPrompt", systemPrompt);
        innerJson.put("batchId", batchId);
        innerJson.put("sorContainerId", sorContainerId);
        innerJson.put("base64Img", base64Img);
        innerJson.put("modelName", null);

        JSONObject input = new JSONObject();
        input.put("name", "KRYPTON START");
        input.put("shape", new JSONArray());
        input.put("datatype", "BYTES");
        input.put("data", new JSONArray().appendElement(innerJson.toString()));

        JSONObject payload = new JSONObject();
        payload.put("inputs", new JSONArray().appendElement(input));

        return payload;
    }

    private String sendPostRequest(String apiUrl, String jsonPayload) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes());
            os.flush();
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);
            return response.toString();
        }
    }

    private void extractAndPrintInferResponse(String apiResponse, Path imagePath) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(apiResponse);

            JSONArray outputs = (JSONArray) json.get("outputs");
            JSONObject firstOutput = (JSONObject) outputs.get(0);
            JSONArray dataArray = (JSONArray) firstOutput.get("data");

            String innerResponseRaw = (String) dataArray.get(0);
            JSONObject innerResponse = (JSONObject) parser.parse(innerResponseRaw);

            // Get the infer_response string (which itself contains a JSON array)
            String inferResponseStr = (String) innerResponse.get("infer_response");

            // Clean up formatting if wrapped with json or backticks
            inferResponseStr = inferResponseStr
                    .replaceAll("json", "")
                    .replaceAll("", "")
                    .trim();

            // Parse infer_response as a JSON array
            JSONArray inferArray = (JSONArray) parser.parse(inferResponseStr);

            String memberFullName = null;
            for (Object obj : inferArray) {
                JSONObject item = (JSONObject) obj;
                String key = (String) item.get("key");

                if ("auth_id".equalsIgnoreCase(key)) {
                    memberFullName = (String) item.get("value");
                    break;
                }
            }

            if (memberFullName != null && !memberFullName.isEmpty()) {
                System.out.println("✅ AUTH ID found for  -> " + memberFullName);
            } else {
                System.out.println("⚠️ AUTH ID not found in infer_response for " + imagePath);
            }

        } catch (Exception e) {
            log.error("❌ Failed to parse infer response for image {}: {}", imagePath, e.getMessage(), e);
        }
    }

    public String getUserPrompt(){
        return "**Step-by-step reasoning to extract auth id:**\n" +
                "- Think through the reasoning internally. Do not include any reasoning or commentary in the output.\n" +
                "- Output only the structured JSON response following the schema below.\n" +
                "\n" +
                "---\n" +
                "\n" +
                "### Task\n" +
                "Find Authorization IDs on THIS PAGE ONLY.\n" +
                "\n" +
                "### Allowed key list (case-insensitive; same line required)\n" +
                "Auth ID, AUTH NUMBER, AUTHORIZATION NUMBER, AUTH ID, AUTHORIZATION ID, AUTHID, REF/AUTH,\n" +
                "Authorization number for approved days, Reference Number, Reference No, Reference Nõ, Case #,\n" +
                "Review Requested #, Review Requested Ref, Service Ref #, Case Number, Ref#, Ref #, Reference #,\n" +
                "Pharmacy Prior Authorization Request#, prior authorization request, prior authorization request #,\n" +
                "prior authorization request number, Plan Reference Number, PA #, Case, EOC, Reference Number:,\n" +
                "Confidential UM Information for:, MMS, MMS#, Existing Authorization, Authorization #,\n" +
                "UM Number, UM#, UM Reference Number, UM Ref#, UM ID,\n" +
                "Auth #, AUTH #, Auth No, AUTH NO, Auth Number, AUTH NUMBER\n" +
                "\n" +
                "### Negative/exclude keys (reject if on same line)\n" +
                "MRN, Medical Record Number, Member ID, Medicaid ID, Policy #, Account #, Claim #, CSID, Referring #,\n" +
                "Fax #, Phone, Dept #, Invoice, Bill, NPI, TIN, Tax ID, Date/Time, Zip, Page, File, Document #, Record #\n" +
                "\n" +
                "### Acceptance rules (must ALL hold)\n" +
                "1) The value is on the SAME printed line/field/cell as an allowed key.\n" +
                "2) The value matches ^UM[0-9]{8}$ exactly (UM + 8 digits; no spaces/dashes/punctuation).\n" +
                "3) No concatenation across lines. No standalone pattern without an allowed key on the same line.\n" +
                "4) If any negative/exclude key appears on that same line, reject the candidate.\n" +
                "5) Capture all distinct valid matches in document order; deduplicate identical IDs.\n" +
                "6) Return the bounding box coordinates in the speficied format. (topLeftX, topLeftY, bottomRightX, bottomRightY)\n" +
                "7) Also capture the exact printed source label text from that same line/field/cell in the \"label\" field (preserve case/punctuation).\n" +
                "8) Do not convert example patterns like UM######## into numbers; reject unless a printed token matches ^UM[0-9]{8}$ on the same line as an allowed key.\n" +
                "9) Zero-drop: If your first read has only 7 digits, re-read and resolve any faint/ambiguous digit using the visual ambiguity map (O↔0, I/l↔1, S↔5, G↔6, B↔8, Z↔2). If still not 8 digits with visual support, return the no-match schema.\n" +
                "10) 8 vs 3 check: If any digit resembles “3” but shows two closed loops or a faint vertical bridge, treat it as “8”. If you still cannot support 8 digits visually after re-read, return the no-match schema instead of a 7-digit value.\n" +
                "\n" +
                "**Bounding box**: Provide the bounding box for the extracted ID when available in the specified format.\n" +
                "\n" +
                "---\n" +
                "\n" +
                "### Output Schema (strict)\n" +
                "\n" +
                "If one or more valid auth ids found:\n" +
                "[\n" +
                "  {\n" +
                "    \"key\": \"auth_id\",\n" +
                "    \"label\": \"{exact source label as printed}\",\n" +
                "    \"value\": \"{UM########}\",\n" +
                "    \"boundingBox\": {\n" +
                "      \"topLeftX\": <xmin>,\n" +
                "      \"topLeftY\": <ymin>,\n" +
                "      \"bottomRightX\": <xmax>,\n" +
                "      \"bottomRightY\": <ymax>\n" +
                "    }\n" +
                "  }\n" +
                "  // repeat for each additional extracted id\n" +
                "]\n" +
                "\n" +
                "If no valid auth id found:\n" +
                "[\n" +
                "  {\n" +
                "    \"key\": \"auth_id\",\n" +
                "    \"label\": \"\",\n" +
                "    \"value\": \"\",\n" +
                "    \"boundingBox\": {}\n" +
                "  }\n" +
                "]\n" +
                "";
    }

    public String getSystemPrompt(){
        return "You are an extraction assistant with VISION capabilities. Your job: extract ONLY true Authorization IDs that exactly match the pattern UM######## (UM + 8 digits) when and only when they are explicitly printed in the image AND appear on the same line/field/cell as an allowed key.\n" +
                "\n" +
                "Return STRICT JSON only as per the user’s schema. Never include explanations or reasoning in the final output.\n" +
                "\n" +
                "ABSOLUTES (page-level)\n" +
                "• THIS PAGE ONLY — do not use values from other pages or prior content.\n" +
                "• NO DEFAULTS, NO GUESSES — if a value is not visibly present as per the rules, return empty per the no-match schema.\n" +
                "• DO NOT CORRECT OR NORMALIZE words or punctuation. Extract exactly the printed token.\n" +
                "• DO NOT CONCATENATE across lines or stitch digits from multiple places.\n" +
                "\n" +
                "ALLOWED KEYS (case-insensitive; match singular/plural and minor punctuation)\n" +
                "Auth ID, AUTH NUMBER, AUTHORIZATION NUMBER, AUTH ID, AUTHORIZATION ID, AUTHID, REF/AUTH,\n" +
                "Authorization number for approved days, Reference Number, Reference No, Reference Nõ, Case #,\n" +
                "Review Requested #, Review Requested Ref, Service Ref #, Case Number, Ref#, Ref #, Reference #,\n" +
                "Pharmacy Prior Authorization Request#, prior authorization request, prior authorization request #,\n" +
                "prior authorization request number, Plan Reference Number, PA #, Case, EOC, Reference Number:,\n" +
                "Confidential UM Information for:, MMS, MMS#, Existing Authorization, Authorization #,\n" +
                "UM Number, UM#, UM Reference Number, UM Ref#, UM ID,\n" +
                "Auth #, AUTH #, Auth No, AUTH NO, Auth Number, AUTH NUMBER\n" +
                "\n" +
                "NEGATIVE/EXCLUDE KEYS (reject even if a UM-pattern is nearby)\n" +
                "MRN, Medical Record Number, Member ID, Medicaid ID, Policy #, Account #, Claim #, CSID, Referring #,\n" +
                "Fax #, Phone, Dept #, Invoice, Bill, NPI, TIN, Tax ID, Date/Time, Zip, Page, File, Document #, Record #\n" +
                "\n" +
                "STRICT EXTRACTION RULES\n" +
                "1) KEY PROXIMITY: Extract a value only if it is on the SAME printed line/field/cell as an ALLOWED KEY (exact line adjacency). Do NOT accept values merely near or under a header. If no allowed key on the same line, reject.\n" +
                "2) VALUE FORMAT: Value MUST match ^UM[0-9]{8}$ exactly (UM + 8 digits, no spaces/dashes/punctuation).\n" +
                "3) NO STANDALONE PATTERN: A UM######## token with no allowed key on the same line MUST be rejected.\n" +
                "4) NO CROSS-LINE / NO STITCHING: Do not join digits across lines or cells.\n" +
                "5) MULTIPLE MATCHES: Capture all distinct valid matches in top-to-bottom order; deduplicate identical IDs.\n" +
                "6) REJECT NON-UM IDs: Anything not matching ^UM[0-9]{8}$ must be ignored (e.g., UM-12345678, UM12345, plain numbers).\n" +
                "7) BOUNDING BOX: If the valid auth id is present return the bounding box coordinates in the specified structure.\n" +
                "8) EXCLUDE-LINE: If any NEGATIVE/EXCLUDE KEY is on the same line as the candidate value, reject it.\n" +
                "9) NO HALLUCINATION GUARANTEE: If you cannot point to a visible token that satisfies (ALLOWED KEY on the same line) + (exact regex), you MUST return the no-match schema.\n" +
                "10) ANTI-PLACEHOLDER: The string “UM########” is an EXAMPLE ONLY. Never output “UM########” literally and never replace “########” with guessed digits. Only output a value if the exact printed token matches ^UM[0-9]{8}$ and is on the same line as an allowed key.\n" +
                "11) ANTI-ECHO: Do not copy example patterns from this prompt into the output. If no valid token exists, return the no-match schema.\n" +
                "\n" +
                "CHARACTER-INTEGRITY & DIGIT-RESCUE (no dropped digits)\n" +
                "A) ZERO-DROP: Never emit fewer than 8 digits after “UM”. If first read < 8 digits, re-read the token region before output.\n" +
                "B) LOCAL ZOOM RE-READ: Reinspect a tight crop around UM + digits; expand left/right by ~5–8 px to catch clipped edges.\n" +
                "C) CONTRAST PASSES: Try binarize → light dilation (1 px) → closing (1 px) and an inverted (white-on-black) check; keep only visually supported strokes.\n" +
                "D) 8 vs 3 DISAMBIGUATION (visual, not guessing):\n" +
                "   • “8” = two CLOSED loops (two counters). Often a faint vertical bridge between upper/lower loops.\n" +
                "   • “3” = two OPEN arcs with no closed counters and no vertical bridge.\n" +
                "   • “B” has a vertical spine; reject as digit unless spine is absent.\n" +
                "   • If loops are broken by tiny gaps/dropout, treat as a closed loop (stroke dropout).\n" +
                "E) LEADING/TRAILING ZERO RESCUE: Check just after “UM” and at the tail for faint “0”s merged with neighbors.\n" +
                "F) WIDTH SANITY: Compare token width to average per-digit width in that row. If width suggests 8 digits but OCR read 7, re-read.\n" +
                "G) CONTIGUITY: Final token must be visually contiguous as UM########. No stitched digits from other lines/cells.\n" +
                "H) NO INVENTION: If, after re-reads, you cannot visually justify 8 digits, DO NOT emit the value; use the no-match schema.\n" +
                "\n" +
                "SOURCE LABEL CAPTURE\n" +
                "• Along with each extracted value, also capture the exact printed source label text from that same line/field/cell and return it in the \"label\" field.\n" +
                "• Capture the label EXACTLY as printed (preserve case, punctuation, slashes, “#”, diacritics, and spacing; trim only obvious leading/trailing whitespace).\n" +
                "• If multiple label tokens are on the same line, choose the token closest to the extracted value; if equidistant, choose the leftmost label token.\n" +
                "• Do NOT normalize the label to the allowed-keys list; return the literal source label as seen in the image.\n" +
                "\n" +
                "OUTPUT\n" +
                "• Return EXACT JSON ONLY as described by the user (no extra fields, no prose).";
    }

}
