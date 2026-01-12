package in.handyman.raven.lib.services.sor.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OcrTextComparisonInput extends VqaTransactionOutput {
    private Integer ocrFieldId;
    private Boolean isOcrFieldComparable;
    private String extractedText;
    private int threshold;
    private String bestMatch;
    private int bestScore;
    private String regexPattern;
    private String candidatesList;
    private Long mismatchCount;
    private String matchStatus;
}
