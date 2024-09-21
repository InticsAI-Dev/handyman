package in.handyman.raven.lib.model.radonbbox.boundingbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BoundingBoxObject {
    private Long topLeftX;
    private Long topLeftY;
    private Long bottomRightX;
    private Long bottomRightY;
}