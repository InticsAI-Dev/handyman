package in.handyman.raven.lib.adapters.selections.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AggregationEvaluatorInputModel {

    private Long id;
    private Timestamp createdOn;
    private Long createdUserId;
    private Timestamp lastUpdatedOn;
    private Long lastUpdatedUserId;
    private String status;

    private Integer version;

    private String answer;
    private String bBox;
    private String documentId;
    private String extractedImageUnit;

    private Long groupId;

    private Integer imageDpi;
    private Integer imageHeight;
    private Integer imageWidth;

    private String modelId;
    private String modelInfo;

    private String originId;
    private Integer paperNo;
    private Long questionId;

    private Long rootPipelineId;

    private Double score; // vqa.vqa_score AS score
    private Double vqaScore; // vqa.vqa_score (original)

    private Integer sorItemAttributionId;
    private String sorItemName;
    private String sorQuestion;

    private Long synonymId;
    private Long tenantId;

    private Double weight;

    private String modelRegistry;
    private String category;
    private String modelRegistryId;
    private String stage;
    private String batchId;

    // ---- From encryption_policy (ep) ----
    private String lineItemType;
    private Long encryptionPolicyId;
    private Boolean isEncrypted;
    private String encryptionPolicy;
    private Boolean isMultiEntityEnabled;
    private String sorContainerName;

    // ---- From vqa ----
    private String sorContainerInstance;
    private String sectionAlias;

    // ---- From bs ----
    private String whitelistedSectionsWithPriority;
    private Long sorContainerId;
    private boolean labelMatching;
    private String labelMatchMessage;


}
