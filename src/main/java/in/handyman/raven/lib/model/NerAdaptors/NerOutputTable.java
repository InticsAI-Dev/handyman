package in.handyman.raven.lib.model.NerAdaptors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NerOutputTable implements CoproProcessor.Entity {
    private String originId;
    private int paperNo;
    private int groupId;
    private String processId;
    private int sorId;
    private int sorItemId;
    private String sorItemName;
    private int weight;
    private String question;
    private String answer;
    private String createdUserId;
    private Long tenantId;
    private Timestamp createdOn;
    private float vqaScore;
    private double wordScore;
    private double charScore;
    private double validatorScoreAllowed;
    private double validatorScoreNegative;
    private double confidenceScore;
    private String validationName;
    private String bBox;
    private String status;
    private String stage;
    private String message;
    private Integer synonymId;
    private Integer questionId;
    private String modelName;
    private String modelVersion;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.paperNo, this.groupId, this.processId, this.sorId, this.sorItemId, this.sorItemName,
                this.question, this.answer, this.weight, this.createdUserId, this.tenantId, this.createdOn, this.wordScore, this.charScore,
                this.validatorScoreAllowed, this.validatorScoreNegative, this.confidenceScore, this.validationName, this.bBox,
                this.status, this.stage, this.message, this.vqaScore,this.questionId,this.synonymId,   this.modelName, this.modelVersion
        ).collect(Collectors.toList());
    }

}

