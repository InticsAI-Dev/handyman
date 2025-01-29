package in.handyman.raven.lib.model.kvp.llm.jsonparser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class BoundingBox {
    private double topLeftX;
    private double topLeftY;
    private double bottomRightX;
    private double bottomRightY;
}
