package in.handyman.raven.lib.model.utmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UrgencyTriageModelBoundingBox {
    private int topLeftX;
    private int topLeftY;
    private int bottomRightX;
    private int bottomRightY;

}
