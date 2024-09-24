package in.handyman.raven.lib.alchemy.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoundingBoxAlchemy {
    private int topLeftX;
    private int topLeftY;
    private int bottomRightX;
    private int bottomRightY;
}
