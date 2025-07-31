package in.handyman.raven.lib.model.testDataExtractor;

import in.handyman.raven.lib.model.testDataExtractor.TestDataExtractorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/test-data-extractor")
public class TestDataExtractorController {

    @Autowired
    private TestDataExtractorService service;

    @PostMapping("/extract-text")
    public ResponseEntity<String> extractText(@RequestParam("files") MultipartFile[] files,
                                              @RequestParam("outputPath") String outputPath) {
        try {
            service.processTextExtraction(Arrays.asList(files), outputPath);
            return ResponseEntity.ok("Text extraction completed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/extract-keywords")
    public ResponseEntity<String> extractKeywords(@RequestParam("files") MultipartFile[] files,
                                                  @RequestParam("keywords") List<String> keywords,
                                                  @RequestParam("outputPath") String outputPath) {
        try {
            service.processKeywordExtraction(Arrays.asList(files), keywords, outputPath);
            return ResponseEntity.ok("Keyword extraction completed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}