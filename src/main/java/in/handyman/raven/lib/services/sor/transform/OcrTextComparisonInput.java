package in.handyman.raven.lib.services.sor.transform;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@RegisterBeanMapper(OcrTextComparisonInput.class)
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
    private String allowedAdapter;
}
