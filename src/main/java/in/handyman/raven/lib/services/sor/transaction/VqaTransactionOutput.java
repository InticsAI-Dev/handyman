package in.handyman.raven.lib.services.sor.transaction;


import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VqaTransactionOutput extends VqaTransactionBase {
    private Long vqaId;
    private Long sorContainerId;
    private String sorContainerName;
    private String sorContainerInstance;
    private String sorItemName;
    private Long sorItemId;
    private Integer sorItemAttributionId;
    private Long modelId;
    private String modelInfo;
    private String modelRegistry;
    private Long modelRegistryId;
    private String answer;
    private Double vqaScore;
    private Long score;
    private String bBox;
    private String label;
    private String sectionAlias;
    private Long synonymId;
    private String sorSynonym;
    private Long questionId;
    private String sorQuestion;
    private Integer weight;
    private String category;
    private String lineItemType;
    private String isMultiEntityEnabled;
    private String encryptionPolicy;
    private boolean isEncrypted;
}
