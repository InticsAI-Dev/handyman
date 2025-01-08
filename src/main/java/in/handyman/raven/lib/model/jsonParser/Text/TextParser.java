package in.handyman.raven.lib.model.jsonParser.Text;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class TextParser {
    @JsonProperty("lines")
    private List<TextLineItems> lines;
}
