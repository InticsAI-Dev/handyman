package in.handyman.raven.lib.model.neradaptors;

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
    private int sorItemId;
    private String sorKey;
    private String question;
    private String inputValue;
    private int weight;
    private float vqaScore;
    private String allowedAdapter;
    private String restrictedAdapter;
    private String processId;
    private int wordLimit;
    private int wordThreshold;
    private int charLimit;
    private int charThreshold;
    private int validatorThreshold;
    private String allowedCharacters;
    private String comparableCharacters;
    private int restrictedAdapterFlag;
    private String originId;
    private int paperNo;
    private Integer groupId;
    private String createdUserId;
    private Long rootPipelineId;
    private Long tenantId;
    private String bbox;
    private Integer questionId;
    private Integer synonymId;
    private String modelRegistry;
    private String category;
    private int sorId;
    private double wordScore;
    private double charScore;
    private double validatorScore;
    private double validatorNegativeScore;
    private double confidenceScore;
    private String sorItemName;
    private String batchId;

    @Override
    public List<Object> getRowData() {
        return null;
    }
}
