package in.handyman.raven.lib.custom.outbound.dao;

import lombok.*;

import java.time.LocalDateTime;

// DTO class to represent SQL query result
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PredictionDTO {
    private Long predictionId;
    private LocalDateTime createdOn;
    private String createdUserId;
    private LocalDateTime lastUpdatedOn;
    private String lastUpdatedUserId;
    private String status;
    private Integer version;
    private String encode;
    private String feature;
    private String label;
    private Double leftPos;
    private Double lowerPos;
    private String originId;
    private Double precision;
    private String predictedValue;
    private Long questionId;
    private Double rightPos;
    private String rootPipelineId;
    private String state;
    private Long synonymId;
    private String tableData;
    private Long tenantId;
    private String transactionId;
    private Double upperPos;
    private String workspaceId;
    private Long truthId;
    private String channelId;
    private String csvFilePath;
    private Long sorContainerId;
    private Long truthEntityId;
    private String currencyAsciiValue;
    private String currencyValue;
    private String aggregatedJson;
    private String bulletinPoints;
    private String bulletinSection;
    private String paragraphPoints;
    private String paragraphSection;
    private Long sorItemId;
    private String lineItemType;
    private String sorItemName;
    private String containerName;
    private String sorContainerInstance;
    private Boolean isMultiEntityEnabled;
    private Integer paperNo;
    private Integer imageWidth;
    private Integer imageHeight;
    private String metadataJson;
    private Long groupId;
    private String batchId;
    private String checkboxData;

}
