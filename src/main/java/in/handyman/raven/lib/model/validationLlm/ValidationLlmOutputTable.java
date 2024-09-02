package in.handyman.raven.lib.model.validationLlm;

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
public class ValidationLlmOutputTable implements CoproProcessor.Entity {
    private String originId;
    private Integer paperNo;
    private Integer groupId;
    private Integer processId;
    private Integer sorId;
    private Integer sorItemId;
    private String sorItemName;
    private String sorQuestion;
    private String answer;
    private String validatedAnswer;
    private Float vqaScore;
    private Integer weight;
    private Integer createdUserId;
    private Long tenantId;
    private Timestamp createdOn;
    private Integer confidenceScore;
    private String validationName;
    private String bBox;
    private String status;
    private String stage;
    private String message;
    private Long rootPipelineId;
    private Integer questionId;
    private Integer synonymId;
    private Integer modelName;
    private Integer modelVersion;
    private String modelRegistry;
    private String category;



    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.paperNo, this.groupId,
                this.processId, this.sorId,this.sorItemId, this.sorItemName, this.sorQuestion,this.answer,
                this.validatedAnswer, this.vqaScore, this.weight,this.createdUserId, this.tenantId,
                this.createdOn, this.confidenceScore, this.validationName, this.bBox,this.status, this.stage, this.message,
                this.rootPipelineId, this.questionId, this.synonymId, this.modelName, this.modelVersion, this.modelRegistry,
                this.category).collect(Collectors.toList());
    }
}
