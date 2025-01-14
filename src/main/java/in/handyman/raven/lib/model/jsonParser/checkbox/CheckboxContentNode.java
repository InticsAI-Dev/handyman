package in.handyman.raven.lib.model.jsonParser.checkbox;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import in.handyman.raven.lib.model.jsonParser.Bbox;
import in.handyman.raven.lib.model.jsonParser.BoundingBox;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@JsonPropertyOrder({"question", "options", "confidence", "bBox"})  // Specifying the order of fields in JSON
public class CheckboxContentNode {
    private String question;
    private List<CheckboxStatus> Options;
    private double confidence;
    private BoundingBox bBox;
}

