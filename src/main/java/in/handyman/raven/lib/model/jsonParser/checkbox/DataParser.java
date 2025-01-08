package in.handyman.raven.lib.model.jsonParser.checkbox;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class DataParser {

    @JsonProperty("Row")
    private int row;

    @JsonProperty("Column")
    private int column;

    @JsonProperty("Question")
    private String question;

    @JsonProperty("Options")
    private List<Option> options;  // A List of JsonNode to hold each option as a JSON object
}
