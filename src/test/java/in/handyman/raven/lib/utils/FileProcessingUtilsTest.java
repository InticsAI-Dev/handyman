package in.handyman.raven.lib.utils;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Marker;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class FileProcessingUtilsTest {

    private FileProcessingUtils utils;
    private Marker markerMock;
    private ActionExecutionAudit auditMock;

    @BeforeEach
    void setUp() {
        markerMock = Mockito.mock(Marker.class);
        auditMock = Mockito.mock(ActionExecutionAudit.class);
        utils = new FileProcessingUtils(log, markerMock, auditMock);
    }


    @Test
    void testConvertFileToBase64() throws Exception {
        // Arrange
        File file = new File("src/test/resources/sample.png");
        assertTrue(file.exists(), "Test image file should exist");

        // Act
        String base64 = utils.convertFileToBase64(file.getAbsolutePath());

        // Assert
        assertNotNull(base64);
        assertFalse(base64.isEmpty());
    }

    @Test
    void testConvertBase64ToFile() throws Exception {
        // Arrange
        File file = new File("src/test/resources/sample.png");
        byte[] bytes = Files.readAllBytes(file.toPath());
        String base64 = Base64.getEncoder().encodeToString(bytes);

        String outputPath = "target/test-output/cropped_output.png";

        // Act
        String resultPath = utils.convertBase64ToFile(base64, outputPath);

        // Assert
        File resultFile = new File(resultPath);
        assertTrue(resultFile.exists());
        assertTrue(resultFile.length() > 0);
    }

    @Test
    void testConvertBase64ToFile_withInvalidInput() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            String result = utils.convertBase64ToFile("", "target/test-output/empty_output.png");
            assertEquals("target/test-output/empty_output.png", result);
        });
    }

    @Disabled("Enable only if local FastAPI server is running on port 8000")
    @Test
    void testCallCropImageApi() throws Exception {
        // Arrange
        File file = new File("/data/tenant/PI/pdf_to_image/SYNT_166730399_c3_/SYNT_166730399_c3__0.png");
        String base64 = utils.convertFileToBase64(file.getAbsolutePath());

        // Act
        String croppedBase64 = utils.callCropImageApiWithOkHttp(base64);

        // Assert
        assertNotNull(croppedBase64);
        assertFalse(croppedBase64.isEmpty());
    }
}