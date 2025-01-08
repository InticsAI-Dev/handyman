package in.handyman.raven.lib.model.jsonParser.Text;

import com.fasterxml.jackson.annotation.JsonProperty;
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
}
