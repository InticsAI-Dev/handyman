package in.handyman.raven.lib.model.table.extraction.headers.copro.legacy.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bboxes {
    private Float topLeftX;
    private Float topLeftY;
    private Float bottomRightX;
    private Float bottomRightY;
}
