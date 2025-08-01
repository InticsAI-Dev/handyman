package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.testDataExtractor.TestDataExtractorInput;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

public class TestDataExtractorActionTest {

    private Path tempDir;
    private ActionExecutionAudit actionAudit;
    private Logger mockLogger;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("testDataExtractor");
        actionAudit = Mockito.mock(ActionExecutionAudit.class);
        mockLogger = Mockito.mock(Logger.class);
    }

    @After
    public void tearDown() throws IOException {
        deleteDirectory(tempDir.toFile());
    }


    @Test
    public void testTextExtractionMode() throws Exception {
        MultipartFile mockFile = createMockPdf("Text Extraction Content By implementing the recommendations above (especially JSON serialization, thread safety, and validation), the code can be made robust and reliable. Without these fixes, it will likely fail in production or with invalid inputs.\n" +
                "If you have additional details (e.g., expected file types, concurrency requirements, or deployment environment), I can refine the analysis further. Would you like me to provide a specific code patch, focus on a particular issue, or suggest tests for the system?");

        String outputFilePath = tempDir.resolve("output.txt").toString(); // Specify a file path

        TestDataExtractorInput model = TestDataExtractorInput.builder()
                .name("TextExtractionTest")
                .condition(true)
                .files(Collections.singletonList(mockFile))
                .mode("text")
                .outputPath(outputFilePath) // Use file path
                .keywords(null)
                .build();

        TestDataExtractorAction action = new TestDataExtractorAction(actionAudit, mockLogger, model);
        action.execute();

        File outputFile = new File(outputFilePath);
        System.out.println("Output file exists: " + outputFile.exists());

        String outputContent = Files.readString(outputFile.toPath());
        System.out.println("Output Content:\n" + outputContent);
    }

    @Test
    public void  testKeywordExtractionMode() throws Exception {
        MultipartFile mockFile = createMockPdf("This Wood County Human Services document contains important keywords like robust, reliable, and validation. The system should ensure thread safety and proper JSON serialization.");

        String outputFilePath = tempDir.resolve("output.txt").toString(); // Specify a file path

        TestDataExtractorInput model = TestDataExtractorInput.builder()
                .name("KeywordExtractionTest")
                .condition(true)
                .files(Collections.singletonList(mockFile))
                .mode("keywords")
                .outputPath(tempDir.toString())
                .build();

        TestDataExtractorAction action = new TestDataExtractorAction(actionAudit, mockLogger, model);
        action.execute();

        File outputFile = new File(outputFilePath);
        System.out.println("Output file exists: " + outputFile.exists());

        String outputContent = Files.readString(outputFile.toPath());
        System.out.println("Output Content:\n" + outputContent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMode() throws Exception {
        TestDataExtractorInput model = TestDataExtractorInput.builder()
                .name("InvalidModeTest")
                .condition(true)
                .files(Collections.emptyList())
                .mode("invalid")
                .outputPath(tempDir.toString())
                .build();

        TestDataExtractorAction action = new TestDataExtractorAction(actionAudit, mockLogger, model);
        action.execute();
    }

    @Test
    public void testExecuteIfConditionFalse() throws Exception {
        TestDataExtractorInput model = TestDataExtractorInput.builder()
                .name("ConditionFalseTest")
                .condition(false)
                .build();

        TestDataExtractorAction action = new TestDataExtractorAction(actionAudit, mockLogger, model);
        boolean result = action.executeIf();
        System.out.println("executeIf returned: " + result);
    }

    // -----------------------
    // 📄 Utility Methods
    // -----------------------

    private MultipartFile createMockPdf(String text) throws IOException {
        File file = tempDir.resolve("mock_input.pdf").toFile();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.setLeading(14.5f); // spacing between lines
                contentStream.newLineAtOffset(50, 700);

                for (String line : text.split("\\R")) { // \R handles \r, \n, or \r\n
                    contentStream.showText(line);
                    contentStream.newLine(); // move to the next line
                }

                contentStream.endText();
            }
            document.save(file);
        }

        FileInputStream fis = new FileInputStream(file);
        return new MockMultipartFile("file", "mock_input.pdf", "application/pdf", fis);
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
}