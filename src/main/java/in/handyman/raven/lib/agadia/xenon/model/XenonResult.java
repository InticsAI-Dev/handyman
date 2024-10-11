package in.handyman.raven.lib.agadia.xenon.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class XenonResult {

    private String question;
    private String predictedAttributionValue;
    private Float scores;
    private JsonNode bboxes;
    private String originId;
    private Integer paperNo;
    private  Long processId;
    private Integer groupId;
    private Long tenantId;
    private String batchId;

    private Long questionId;
    private Long synonymId;
    private String sorItemName;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getPredictedAttributionValue() {
        return predictedAttributionValue;
    }

    public void setPredictedAttributionValue(String predictedAttributionValue) {
        this.predictedAttributionValue = predictedAttributionValue;
    }

    public Float getScores() {
        return scores;
    }

    public void setScore(Float scores) {
        this.scores = scores;
    }

    public JsonNode getBboxes() {
        return bboxes;
    }

    public void setBboxes(JsonNode bboxes) {
        this.bboxes = bboxes;
    }
}
