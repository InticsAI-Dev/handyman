package in.handyman.raven.lib.model.jsonParser.Text;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import in.handyman.raven.lib.model.jsonParser.Bbox;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@JsonPropertyOrder({"content", "confidence", "bBox"})
public class ContentNode {
    private String content;
    private double confidence;
    private Bbox bBox;
}
