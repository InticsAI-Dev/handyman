package in.handyman.raven.lib.model.scalar;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostProcessingFieldsInput {

    private Integer postProcessingFieldId;
    private String postProcessingCode;
    private String postProcessingKey;
    private Double aggregatedScore;
    private String sorItemName;
    private Long vqaId;
    private Long sorContainerId;
    private String sorContainerName;
    private String sorContainerInstance;
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
    private String transactionId;
    private LocalDateTime createdOn;
    private Long createdUserId;
    private LocalDateTime lastUpdatedOn;
    private Long lastUpdatedUserId;
    private Long rootPipelineId;
    private Long tenantId;
    private String documentId;
    private Integer groupId;
    private String batchId;
    private String originId;
    private Integer paperNo;
    private Integer truthId;
    private String status;
    private String stage;
    private String message;
    private Integer version;
    private String extractedImageUnit;
    private Long imageDpi;
    private Long imageHeight;
    private Long imageWidth;
    private String sectionPriorityAfterFilter;
}