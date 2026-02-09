package in.handyman.raven.lib.model;

import lombok.*;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.time.LocalDateTime;

/**
 * DTO for DocumentWisePostProcessing input table
 * Maps to document_wise_post_processing_input table structure
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentWisePostProcessingInput {
    private LocalDateTime createdOn;
    
    @ColumnName("created_user_id")
    private String createdUserId;
    
    @ColumnName("last_updated_on")
    private LocalDateTime lastUpdatedOn;
    
    @ColumnName("last_updated_user_id")
    private String lastUpdatedUserId;
    
    private String status;
    private Integer version;
    private String encode;
    private String feature;
    private String label;
    
    @ColumnName("origin_id")
    private String originId;
    
    private Double precision;
    
    @ColumnName("predicted_value")
    private String predictedValue;
    
    @ColumnName("question_id")
    private Long questionId;
    
    @ColumnName("root_pipeline_id")
    private String rootPipelineId;
    
    private String state;
    
    @ColumnName("synonym_id")
    private Long synonymId;
    
    @ColumnName("tenant_id")
    private Long tenantId;
    
    @ColumnName("transaction_id")
    private String transactionId;
    
    @ColumnName("truth_id")
    private Long truthId;
    
    @ColumnName("channel_id")
    private String channelId;
    
    @ColumnName("csv_file_path")
    private String csvFilePath;
    
    @ColumnName("sor_container_id")
    private Long sorContainerId;
    
    @ColumnName("truth_entity_id")
    private Long truthEntityId;
    
    @ColumnName("currency_ascii_value")
    private String currencyAsciiValue;
    
    @ColumnName("currency_value")
    private String currencyValue;
    
    @ColumnName("paragraph_section")
    private String paragraphSection;
    
    @ColumnName("sor_item_id")
    private Long sorItemId;
    
    @ColumnName("left_pos")
    private Double leftPos;
    
    @ColumnName("right_pos")
    private Double rightPos;
    
    @ColumnName("lower_pos")
    private Double lowerPos;
    
    @ColumnName("upper_pos")
    private Double upperPos;
    
    @ColumnName("is_encrypted")
    private Boolean isEncrypted;
    
    // Additional fields that might be needed for processing
    @ColumnName("sor_item_name")
    private String sorItemName;
    
    @ColumnName("group_id")
    private Long groupId;
    
    @ColumnName("batch_id")
    private String batchId;
    
    @ColumnName("paper_no")
    private Integer paperNo;
}
