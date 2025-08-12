package in.handyman.raven.lib.model.paperitemizer;

import lombok.extern.slf4j.Slf4j;


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
}
