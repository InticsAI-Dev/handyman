package in.handyman.raven.lib.model.jsonParser.checkbox;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    @JsonProperty("Question")
    private String question;
    @JsonProperty("Options")
    private List<JsonNode> Options;
    private double confidence;
    private Bbox boundingBox;
}

