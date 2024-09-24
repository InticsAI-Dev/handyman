package in.handyman.raven.lib.model.templatedetection.copro;

import com.fasterxml.jackson.databind.JsonNode;

public class XenonResultCopro{
    private String question;
    private String predictedAttributionValue;
    private Float scores;
    private JsonNode bboxes;

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

