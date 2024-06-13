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

    private String p2pFirstNameBBox;
    private String p2pLastNameBBox;
    private String p2pFullNameBBox;
    private Double p2pFirstNameConfidenceScore;
    private Double p2pLastNameConfidenceScore;
    private Double p2pFullNameConfidenceScore;
    private String p2pFirstName;
    private String p2pLastName;
    private String p2pFullName;
    private Integer groupId;
    private Double p2pFirstNameMaximumScore;
    private Double p2pLastNameMaximumScore;
    private Double p2pFullNameMaximumScore;
    private String originId;
    private Integer paperNo;
    private Long rootPipelineId;
    private Integer tenantId;
    private String sorItemName;
    private Integer firstNameQuestionId;
    private Integer lastNameQuestionId;
    private Integer questionId;
    private Integer firstNameSynonymId;
    private Integer lastNameSynonymId;
    private Integer synonymId;
    private String modelRegistry;
    private String category;

    @Override
    public List<Object> getRowData() {
        return null;
    }
}
