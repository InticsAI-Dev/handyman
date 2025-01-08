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
public class FormElement {
    private String key;
    private String value;
    private BoundingBox boundingBox;
    private double confidence;

}
