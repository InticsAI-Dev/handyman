package in.handyman.raven.lib.model.templatedetection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TemplateDetectionArgonAttributes {
    private String question;
    private String predictedAttributionValue;
    private Float scores;
    private Map<String, Double> bboxes;
    private Integer questionId;
    private Integer synonymId;
    private String sorItemName;

}
