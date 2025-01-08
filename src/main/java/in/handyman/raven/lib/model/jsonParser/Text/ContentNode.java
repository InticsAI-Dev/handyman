package in.handyman.raven.lib.model.jsonParser.Text;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import in.handyman.raven.lib.model.jsonParser.Bbox;
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
@JsonPropertyOrder({"Content", "Confidence", "BoundingBox"})
public class ContentNode {
    private String content;
    private double confidence;
    private Bbox boundingBox;
}
