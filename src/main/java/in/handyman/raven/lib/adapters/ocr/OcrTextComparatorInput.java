package in.handyman.raven.lib.adapters.ocr;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OcrTextComparatorInput {
    private String originId;
    private String sorItemName;
    private Long sorItemId;
    private String sorQuestion;
    private String answer;
    private Long tenantId;
    private String batchId;
    private Integer groupId;
    private Integer paperNo;
    private Double vqaScore;
    private Double score;
    private Integer weight;
    private Integer sorItemAttributionId;
    private String documentId;
    private String bBox;
    private Long rootPipelineId;
    private Long questionId;
    private Long synonymId;
    private String modelRegistry;
    private String category;
    private Boolean isOcrFieldComparable;
    private String extractedText;
    private String encryptionPolicy;
    private String allowedAdapter;

}
