package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.testDataExtractor.TestDataExtractorInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
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
        String inputFilePath = createMockJpeg("Text Extraction Content By implementing the recommendations above (especially JSON serialization, thread safety, and validation), the code can be made robust and reliable. Without these fixes, it will likely fail in production or with invalid inputs.\n" +
                "If you have additional details (e.g., expected file types, concurrency requirements, or deployment environment), I can refine the analysis further. Would you like me to provide a specific code patch, focus on a particular issue, or suggest tests for the system?");

        String outputFilePath = tempDir.resolve("output.txt").toString();

        TestDataExtractorInput model = TestDataExtractorInput.builder()
                .name("TextExtractionTest")
                .condition(true)
                .inputFilePaths(Collections.singletonList(inputFilePath))
                .mode("text")
                .outputPath(outputFilePath)
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
    public void testKeywordExtractionMode() throws Exception {
        String inputFilePath = createMockJpeg("This Wood County Human Services document contains important keywords like robust, reliable, and validation. The system should ensure thread safety and proper JSON serialization.");

        String outputFilePath = tempDir.resolve("output.txt").toString();

        TestDataExtractorInput model = TestDataExtractorInput.builder()
                .name("KeywordExtractionTest")
                .condition(true)
                .inputFilePaths(Collections.singletonList(inputFilePath))
                .mode("keywords")
                .outputPath(outputFilePath)
                .keywords(Arrays.asList("robust", "reliable", "validation"))
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
                .inputFilePaths(Collections.emptyList())
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

    private String createMockJpeg(String text) throws IOException {
        File file = tempDir.resolve("mock_input.jpg").toFile();

        // Create a simple image with text
        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Set background to white
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Set text properties
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        int x = 50;
        int y = 50;
        int lineHeight = 15;

        // Draw text line by line
        for (String line : text.split("\\R")) {
            g.drawString(line, x, y);
            y += lineHeight;
        }

        g.dispose();

        // Save as JPEG
        ImageIO.write(image, "jpg", file);
        return file.getAbsolutePath();
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