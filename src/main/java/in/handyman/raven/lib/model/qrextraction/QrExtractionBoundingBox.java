package in.handyman.raven.lib.model.qrextraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QrExtractionBoundingBox {
    private Integer topLeftX;
    private Integer topLeftY;
    private Integer bottomRightX;
    private Integer bottomRightY;
}
