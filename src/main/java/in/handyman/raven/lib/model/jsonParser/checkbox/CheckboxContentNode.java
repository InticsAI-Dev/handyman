package in.handyman.raven.lib.model.jsonParser.checkbox;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import in.handyman.raven.lib.model.jsonParser.Bbox;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j

@JsonPropertyOrder({"Question", "Options", "Confidence", "boundingBox"})  // Specifying the order of fields in JSON
public class CheckboxContentNode {
    private String question;
    private List<CheckboxStatus> Options;
    private double confidence;
    private Bbox boundingBox;
}

