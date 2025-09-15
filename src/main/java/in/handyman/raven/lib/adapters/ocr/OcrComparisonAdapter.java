package in.handyman.raven.lib.adapters.ocr;

public interface OcrComparisonAdapter {
    OcrComparisonResult compareValues(String expectedValue, String extractedText, double threshold);
    String getName();

}