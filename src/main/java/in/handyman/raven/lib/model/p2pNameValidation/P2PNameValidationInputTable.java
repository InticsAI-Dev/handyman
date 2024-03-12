package in.handyman.raven.lib.model.p2pNameValidation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class P2PNameValidationInputTable implements CoproProcessor.Entity {

    private String p2pFirstNameBbox;
    private String p2pLastNameBBox;
    private Double p2pFirstNameConfidenceScore;
    private Double p2pLastNameConfidenceScore;
    private String p2pFirstName;
    private String p2pLastName;
    private Double p2pFirstNameFilterScore;
    private Double p2pLastNameFilterScore;
    private Integer groupId;
    private Double p2pFirstNameMaximumScore;
    private Double p2pLastNameMaximumScore;
    private String originId;
    private Integer paperNo;
    private Long rootPipelineId;
    private Integer tenantId;
    private String sorItemName;

    @Override
    public List<Object> getRowData() {
        return null;
    }
}
