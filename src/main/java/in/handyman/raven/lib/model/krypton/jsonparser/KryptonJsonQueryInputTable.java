package in.handyman.raven.lib.model.krypton.jsonparser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KryptonJsonQueryInputTable {
    private String response;
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
