package in.handyman.raven.lib.model;

import lombok.*;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.time.LocalDateTime;

/**
 * Input DTO for MultivalueSorItemHandlingAction
 * Maps to the input table columns from MultivalueSorItemHandlingAction query
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MultivalueSorItemHandlingInput {

    @ColumnName("simfi_id")
    private Long simfiId;

    @ColumnName("transaction_id")
    private String transactionId;

    @ColumnName("created_on")
    private LocalDateTime createdOn;

    @ColumnName("created_user_id")
    private Long createdUserId;

    @ColumnName("last_updated_on")
    private LocalDateTime lastUpdatedOn;

    @ColumnName("last_updated_user_id")
    private Long lastUpdatedUserId;

    private String status;
    private Integer version;
    private String answer;

    @ColumnName("b_box")
    private String bBox;

    @ColumnName("document_id")
    private String documentId;

    @ColumnName("extracted_image_unit")
    private String extractedImageUnit;

    @ColumnName("group_id")
    private Integer groupId;

    @ColumnName("image_dpi")
    private Long imageDpi;

    @ColumnName("image_height")
    private Long imageHeight;

    @ColumnName("image_width")
    private Long imageWidth;

    @ColumnName("model_id")
    private Long modelId;

    @ColumnName("model_info")
    private String modelInfo;

    @ColumnName("origin_id")
    private String originId;

    @ColumnName("paper_no")
    private Integer paperNo;

    @ColumnName("question_id")
    private Long questionId;

    @ColumnName("root_pipeline_id")
    private Long rootPipelineId;

    private Long score;

    @ColumnName("sor_item_attribution_id")
    private Integer sorItemAttributionId;

    @ColumnName("sor_item_name")
    private String sorItemName;

    @ColumnName("sor_question")
    private String sorQuestion;

    @ColumnName("synonym_id")
    private Long synonymId;

    @ColumnName("tenant_id")
    private Long tenantId;

    @ColumnName("vqa_score")
    private Double vqaScore;

    private Integer weight;

    @ColumnName("model_registry")
    private String modelRegistry;

    private String category;

    @ColumnName("model_registry_id")
    private Long modelRegistryId;

    private String stage;

    @ColumnName("batch_id")
    private String batchId;

    @ColumnName("line_item_type")
    private String lineItemType;

    @ColumnName("is_encrypted")
    private Boolean isEncrypted;

    @ColumnName("encryption_policy")
    private String encryptionPolicy;

    @ColumnName("sor_container_instance")
    private String sorContainerInstance;

    @ColumnName("is_multi_entity_enabled")
    private Boolean isMultiEntityEnabled;

    @ColumnName("sor_container_name")
    private String sorContainerName;

    @ColumnName("section_alias")
    private String sectionAlias;

    @ColumnName("whitelisted_sections")
    private String whitelistedSections;

    private String label;

    @ColumnName("truth_id")
    private Integer truthId;

    @ColumnName("sor_item_id")
    private Long sorItemId;

    @ColumnName("sor_container_id")
    private Long sorContainerId;
}
