package in.handyman.raven.lib.model.jsonParser.KVP;


import in.handyman.raven.lib.model.jsonParser.BoundingBox;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor


public class KVPContentNode {
    private String key;
    private String value;
    private double confidence;
    private BoundingBox boundingBox;
}

