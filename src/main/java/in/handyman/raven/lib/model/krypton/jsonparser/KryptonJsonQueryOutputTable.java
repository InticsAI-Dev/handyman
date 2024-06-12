package in.handyman.raven.lib.model.krypton.jsonparser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KryptonJsonQueryOutputTable {
    private Timestamp createdOn;
    private Long createdUserId;
    private Timestamp lastUpdatedOn;
    private Long lastUpdatedUserId;
    private List<KryptonJsonParsedResponse> parsedOutput;
    private Integer paperNo;
    private String originId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private String extractedImageUnit;
    private Long imageDpi;
    private Long imageWidth ;
    private Long imageHeight ;
}
