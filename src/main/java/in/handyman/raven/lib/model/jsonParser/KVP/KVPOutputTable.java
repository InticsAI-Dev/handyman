package in.handyman.raven.lib.model.jsonParser.KVP;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j

public class KVPOutputTable {
    private Timestamp createdOn;
    private Long createdUserId;
    private Timestamp lastUpdatedOn;
    private Long lastUpdatedUserId;
    private String sorItem;
    private String value;
    private Integer paperNo;
    private String originId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private Long imageDpi;
    private Long imageHeight;
    private Long imageWidth;
    private Float confidenceScore;
    private String bBox;

}


