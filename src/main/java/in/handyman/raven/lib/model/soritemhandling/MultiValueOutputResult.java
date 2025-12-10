package in.handyman.raven.lib.model.soritemhandling;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultiValueOutputResult {
    private String status;
    private String answer;
    private String bBox;
    private String documentId;
    private Long groupId;
    private Integer modelId;
    private String modelInfo;
    private String originId;
    private Long paperNo;
    private Long questionId;
    private Long rootPipelineId;
    private Double score;
    private Long sorItemAttributionId;
    private String sorItemName;
    private String sorQuestion;
    private Long synonymId;
    private Long tenantId;
    private Double vqaScore;
    private String modelRegistry;
    private String category;
    private String stage;
    private String batchId;
    private String lineItemType;
    private Boolean isEncrypted;
    private Integer encryptionPolicyId;
    private String encryptionPolicy;
    private String sorContainerInstance;
    private String isMultiEntityEnabled;
}
