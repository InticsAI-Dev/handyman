package in.handyman.raven.lib.model.jsonParser.Text;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.handyman.raven.lib.model.jsonParser.BoundingBox;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TextLineItems{

    @JsonProperty("line_number")
    private Integer lineNumber;
    @JsonProperty("content")
    private String content;
    @JsonProperty("confidence")
    private double confidence;
    @JsonProperty("bounding_box")
    private BoundingBox boundingBox;
}
