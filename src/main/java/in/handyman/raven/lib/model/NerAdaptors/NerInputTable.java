package in.handyman.raven.lib.model.NerAdaptors;

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
public class NerInputTable implements CoproProcessor.Entity {
    private int sorId;
    private String originId;
    private String processId;
    private String sorKey;
    private String question;
    private String inputValue;
    private float vqaScore;
    private String allowedAdapter;
    private String restrictedAdapter;
    private int wordLimit;
    private int wordThreshold;
    private int charLimit;
    private int charThreshold;
    private int validatorThreshold;
    private String allowedCharacters;
    private String comparableCharacters;
    private int restrictedAdapterFlag;
    private int paperNo;
    private Integer groupId;
    private String bbox;
    private int sorItemId;
    private String createdUserId;
    private Long tenantId;
    private double wordScore;
    private double charScore;
    private double validatorScore;
    private double validatorNegativeScore;
    private double confidenceScore;
    private String sorItemName;
    private int weight;
    private Integer synonymId;
    private Integer questionId;

    @Override
    public List<Object> getRowData() {
        return null;
    }
}
