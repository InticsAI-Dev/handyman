package in.handyman.raven.lib.model.jsonParser;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Bbox {
    private double x;
    private double y;
    private double width;
    private double height;

}
