package in.handyman.raven.outbound.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalPredictionView {

    @ColumnName("prediction_id")
    private Long predictionId;

    @ColumnName("created_on")
    private LocalDateTime createdOn;

    @ColumnName("created_user_id")
    private String createdUserId;

    @ColumnName("last_updated_on")
    private LocalDateTime lastUpdatedOn;

    @ColumnName("last_updated_user_id")
    private String lastUpdatedUserId;

    @ColumnName("status")
    private String status;

    @ColumnName("version")
    private Long version;

    @ColumnName("encode")
    private String encode;

    @ColumnName("feature")
    private String feature;

    @ColumnName("label")
    private String label;

    @ColumnName("left_pos")
    private Double leftPos;

    @ColumnName("lower_pos")
    private Double lowerPos;

    @ColumnName("origin_id")
    private String originId;

    @ColumnName("precision")
    private Double precision;

    @ColumnName("predicted_value")
    private String predictedValue;

    @ColumnName("question_id")
    private Long questionId;

    @ColumnName("right_pos")
    private Double rightPos;

    @ColumnName("root_pipeline_id")
    private Long rootPipelineId;

    @ColumnName("state")
    private String state;

    @ColumnName("synonym_id")
    private Long synonymId;

    @ColumnName("table_data")
    private String tableData;

    @ColumnName("tenant_id")
    private Long tenantId;

    @ColumnName("transaction_id")
    private String transactionId;

    @ColumnName("upper_pos")
    private Double upperPos;

    @ColumnName("workspace_id")
    private Long workspaceId;

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

    @ColumnName("aggregated_json")
    private String aggregatedJson;

    @ColumnName("bulletin_points")
    private String bulletinPoints;

    @ColumnName("bulletin_section")
    private String bulletinSection;

    @ColumnName("paragraph_points")
    private String paragraphPoints;

    @ColumnName("paragraph_section")
    private String paragraphSection;

    @ColumnName("sor_item_id")
    private Long sorItemId;

    // Assumed Columns required for logic processing

    @ColumnName("item_name")
    private String itemName;

    @ColumnName("line_item_type")
    private String lineItemType;

    @ColumnName("paper_no")
    private Integer paperNo;

    @ColumnName("total_pages")
    private Integer totalPages; // Logic might use IngestionDetails, but field allocated as requested

    @ColumnName("asset_height")
    private Double assetHeight;

    @ColumnName("asset_width")
    private Double assetWidth;

    @ColumnName("container_name")
    private String containerName;

    @ColumnName("mms_id")
    private String mmsId;
}
