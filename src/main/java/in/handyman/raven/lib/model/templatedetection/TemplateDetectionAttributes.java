package in.handyman.raven.lib.model.templatedetection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TemplateDetectionAttributes {
    private String question;
    private String predictedAttributionValue;
    private float scores;
    private String bboxes;
    private String originId;
    private Integer paperNo;
    private  Long processId;
    private Integer groupId;
    private Integer tenantId;

}
