package in.handyman.raven.lib.model.jsonParser;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Slf4j

public class Bbox {
    private double x;
    private double y;
    private double width;
    private double height;

}
