package in.handyman.raven.lib.model.testDataExtractor;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestDataExtractorUtil {

    public Map<String, String> extractTextFromPdf(File pdfFile, Tesseract tesseract) throws IOException, TesseractException {
        Map<String, String> results = new LinkedHashMap<>();
        PDDocument document = PDDocument.load(pdfFile);
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        for (int page = 0; page < document.getNumberOfPages(); page++) {
            BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300);
            String pageText = tesseract.doOCR(image);
            results.put("page" + (page + 1), pageText);
        }

        document.close();
        return results;
    }

    public void saveOutput(Object response, String outputPath) throws IOException {
        String jsonOutput = convertToJson(response);
        Files.writeString(Paths.get(outputPath), jsonOutput);
    }

    private String convertToJson(Object response) {
        return response.toString();
    }
}
