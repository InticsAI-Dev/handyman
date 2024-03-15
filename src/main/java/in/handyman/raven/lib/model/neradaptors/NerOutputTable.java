package in.handyman.raven.lib.model.neradaptors;

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
    private String question;
    private Integer questionId;
    private Integer synonymId;
    private String answer;
    private float vqaScore;
    private int weight;
    private String createdUserId;
    private Long tenantId;
    private Timestamp createdOn;
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
    private Long rootPipelineId;
    private String modelName;
    private String modelVersion;
    private String modelRegistry;
    private String batchId;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.paperNo, this.groupId, this.processId, this.sorId, this.sorItemId, this.sorItemName,
                this.question, this.questionId,this.synonymId,this.answer,this.vqaScore, this.weight, this.createdUserId, this.tenantId, this.createdOn, this.wordScore, this.charScore,
                this.validatorScoreAllowed, this.validatorScoreNegative, this.confidenceScore, this.validationName, this.bBox,
                this.status, this.stage, this.message, this.rootPipelineId, this.modelName, this.modelVersion, this.modelRegistry,this.batchId
        ).collect(Collectors.toList());
    }

}

