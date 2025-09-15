package in.handyman.raven.lib.adapters.ocr;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OcrComparisonMatchResult {
    private double score;
    private String candidate;
    private String restoredMatch;
    private String originalExpected;
}