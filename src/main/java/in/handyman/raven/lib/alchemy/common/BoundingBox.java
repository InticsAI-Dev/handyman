package in.handyman.raven.lib.alchemy.common;


import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class BoundingBox {
    private int topLeftX;
    private int topLeftY;
    private int bottomRightX;
    private int bottomRightY;
}
