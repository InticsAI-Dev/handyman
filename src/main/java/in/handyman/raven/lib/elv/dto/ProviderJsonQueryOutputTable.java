package in.handyman.raven.lib.elv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProviderJsonQueryOutputTable {
    private String createdOn;
    private Long tenantId;
    private Timestamp lastUpdatedOn;
    private Long lastUpdatedUserId;
    private double confidenceScore;
    private String sorItemName;
    private String answer;
    private String boundingBox;
    private Integer paperNo;
    private String originId;
    private Long groupId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private String extractedImageUnit;
    private Long imageDpi;
    private Long imageHeight;
    private Long imageWidth;
    private String sorContainerName;
}
