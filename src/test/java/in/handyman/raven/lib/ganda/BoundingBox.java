package in.handyman.raven.lib.ganda;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
public class BoundingBox {
    private double x;
    private double y;
    private double width;
    private double height;

}