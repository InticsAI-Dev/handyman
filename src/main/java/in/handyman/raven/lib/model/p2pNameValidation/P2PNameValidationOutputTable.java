package in.handyman.raven.lib.model.p2pNameValidation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class P2PNameValidationOutputTable {

    private String p2pBbox;
    private Double p2pConfidenceScore;
    private String p2pConcatenatedName;
    private Double p2pFilterScore;
    private Integer groupId;
    private Double p2pMaximumScore;
    private String originId;
    private Integer paperNo;
    private Long rootPipelineId;
    private Integer tenantId;

}
