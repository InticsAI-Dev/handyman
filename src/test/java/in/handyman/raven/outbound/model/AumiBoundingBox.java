package in.handyman.raven.outbound.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AumiBoundingBox {
    private Double x;
    private Double y;
    private Double width;
    private Double height;

}
