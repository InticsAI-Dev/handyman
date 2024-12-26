package in.handyman.raven.lib.model.jsonParser.Text;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class TextParser {
    @JsonProperty("lines")
    private List<TextLineItems> lines;
}
