package in.handyman.raven.lib.model;

import lombok.*;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.time.LocalDateTime;

/**
 * DTO for DocumentWisePostProcessing input table
 * Maps to document_wise_post_processing_input table structure.
 * The 'answer' column maps to predictedValue for processing.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentWisePostProcessingInput {

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
    private String feature;
    private String label;

    @ColumnName("left_pos")
    private Double leftPos;

    @ColumnName("lower_pos")
    private Double lowerPos;

    @ColumnName("right_pos")
    private Double rightPos;

    @ColumnName("upper_pos")
    private Double upperPos;

    @ColumnName("b_box")
    private String bBox;

    @ColumnName("precision")
    private Double precision;

    /**
     * Maps from 'answer' column from MultivalueSorItemHandlingAction output table
     * This is the predicted value for processing
     */
    @ColumnName("answer")
    private String predictedValue;

    @ColumnName("section_alias")
    private String sectionAlias;

    @ColumnName("sor_container_instance")
    private String sorContainerInstance;

    @ColumnName("document_id")
    private String documentId;

    @ColumnName("truth_id")
    private Long truthId;

    @ColumnName("channel_id")
    private Long channelId;

    @ColumnName("group_id")
    private Integer groupId;

    @ColumnName("origin_id")
    private String originId;

    @ColumnName("paper_no")
    private Integer paperNo;

    @ColumnName("question_id")
    private Long questionId;

    @ColumnName("root_pipeline_id")
    private Long rootPipelineId;

    @ColumnName("score")
    private Long score;

    @ColumnName("vqa_score")
    private Double vqaScore;

    @ColumnName("sor_item_name")
    private String sorItemName;

    @ColumnName("sor_question")
    private String sorQuestion;

    @ColumnName("synonym_id")
    private Long synonymId;

    @ColumnName("tenant_id")
    private Long tenantId;

    @ColumnName("category")
    private String category;

    @ColumnName("stage")
    private String stage;

    @ColumnName("batch_id")
    private String batchId;

    @ColumnName("line_item_type")
    private String lineItemType;

    @ColumnName("is_encrypted")
    private Boolean isEncrypted;

    @ColumnName("encryption_policy")
    private String encryptionPolicy;

    @ColumnName("is_removed_after_filtering")
    private Boolean isRemovedAfterFiltering;

    @ColumnName("message")
    private String message;

    @ColumnName("sor_container_id")
    private Long sorContainerId;

    @ColumnName("truth_entity_id")
    private Long truthEntityId;

    @ColumnName("sor_item_id")
    private Long sorItemId;

    @ColumnName("is_multi_entity_enabled")
    private Boolean isMultiEntityEnabled;
}
