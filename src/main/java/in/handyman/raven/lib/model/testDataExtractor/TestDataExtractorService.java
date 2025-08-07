package in.handyman.raven.lib.model.testDataExtractor;

import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import in.handyman.raven.lib.model.*;
import in.handyman.raven.lib.model.testDataExtractor.TestDataExtractorUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TestDataExtractorService {
    private final Tesseract tesseract;
    private final TestDataExtractorUtil util;

    public TestDataExtractorService() {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
        this.tesseract.setLanguage("eng");
        this.tesseract.setOcrEngineMode(TessAPI.TessOcrEngineMode.OEM_LSTM_ONLY);
        this.util = new TestDataExtractorUtil();
    }

    public void processTextExtraction(List<String> inputFilePaths, String outputPath) throws IOException, TesseractException {
        List<TesseractResponseDto> documentResults = new ArrayList<>();

        for (String filePath : inputFilePaths) {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IOException("File does not exist: " + filePath);
            }
            if (!filePath.toLowerCase().endsWith(".jpg") && !filePath.toLowerCase().endsWith(".jpeg")) {
                throw new IllegalArgumentException("Input file is not a JPEG: " + filePath);
            }
            TesseractResponseDto result = processDocument(file);
            documentResults.add(result);
        }

        TesseractBatchResponseDto response = TesseractBatchResponseDto.builder()
                .documents(documentResults)
                .build();

        util.saveOutput(response, outputPath);
    }

    private TesseractResponseDto processDocument(File file) throws IOException, TesseractException {
        Map<String, String> extractedTexts;
        String fileName = file.getName();

        String text = tesseract.doOCR(file);
        extractedTexts = new LinkedHashMap<>();
        extractedTexts.put("Data", text);

        return TesseractResponseDto.builder()
                .textByPage(extractedTexts)
                .build();
    }

}