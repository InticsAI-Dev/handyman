package in.handyman.raven.lib.model.kvp.llm.jsonparser;


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
    private int topLeftX;
    private int topLeftY;
    private int bottomRightX;
    private int bottomRightY;
}
