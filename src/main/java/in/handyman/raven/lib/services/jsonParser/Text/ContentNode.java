package in.handyman.raven.lib.services.jsonParser.Text;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import in.handyman.raven.lib.services.jsonParser.Bbox;
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
@JsonPropertyOrder({"Text", "Confidence", "BoundingBox"})
public class ContentNode {
    private String text;
    private double confidence;
    private Bbox boundingBox;
}
