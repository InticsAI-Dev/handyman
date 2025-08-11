package in.handyman.raven.lib.model.testDataExtractor;

import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TestDataExtractorService {
    private final Tesseract tesseract;

    public TestDataExtractorService() {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
        this.tesseract.setLanguage("eng");
        this.tesseract.setOcrEngineMode(TessAPI.TessOcrEngineMode.OEM_LSTM_ONLY);
    }

    public String processTextExtraction(List<String> inputFilePaths) throws IOException, TesseractException {
        if (inputFilePaths.size() != 1) {
            throw new IllegalArgumentException("Exactly one input file path is expected");
        }

        String filePath = inputFilePaths.get(0);
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File does not exist: " + filePath);
        }
        if (!filePath.toLowerCase().endsWith(".jpg") && !filePath.toLowerCase().endsWith(".jpeg")) {
            throw new IllegalArgumentException("Input file is not a JPEG: " + filePath);
        }

        return tesseract.doOCR(file);
    }
}