package in.handyman.raven.lib.model.jsonParser.Text;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class TextLineItems{

    @JsonProperty("line_number")
    private Integer lineNumber;
    @JsonProperty("content")
    private String content;
}
