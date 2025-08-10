package in.handyman.raven.lib.model.testDataExtractor;

import in.handyman.raven.lib.model.testDataExtractor.TestDataExtractorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/test-data-extractor")
public class TestDataExtractorController {

    @Autowired
    private TestDataExtractorService service;

    @PostMapping("/extract-text")
    public ResponseEntity<String> extractText(@RequestParam("filePaths") List<String> filePaths,
                                              @RequestParam("outputPath") String outputPath) {
        try {
            for (String filePath : filePaths) {
                if (!Files.exists(Paths.get(filePath))) {
                    return ResponseEntity.status(400).body("File does not exist: " + filePath);
                }
                if (!filePath.toLowerCase().endsWith(".jpg") && !filePath.toLowerCase().endsWith(".jpeg")) {
                    return ResponseEntity.status(400).body("File is not a JPEG: " + filePath);
                }
            }
            service.processTextExtraction(filePaths, outputPath);
            return ResponseEntity.ok("Text extraction completed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}