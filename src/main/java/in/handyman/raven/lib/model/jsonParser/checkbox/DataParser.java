package in.handyman.raven.lib.model.jsonParser.checkbox;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Slf4j

public class DataParser {

    @JsonProperty("Row")
    private int row;

    @JsonProperty("Column")
    private int column;

    @JsonProperty("Question")
    private String question;

    @JsonProperty("Options")
    private List<JsonNode> options;  // A List of JsonNode to hold each option as a JSON object
}
