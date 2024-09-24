package in.handyman.raven.lib.model.trinitymodel;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class TrinityModelDataLineItem {

    private String question;
    private String predictedAttributionValue;
    private Float scores;
    private JsonNode bboxes;
    private String originId;
    private Integer paperNo;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
}
