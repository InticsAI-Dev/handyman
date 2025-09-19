package in.handyman.raven.lib.adapters.ocr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result object for comparison operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrComparisonResult {
    private boolean isMatch;
    private String bestMatch;
    private int bestScore;
    private String matchingMethod;
    private String candidatesList;
}