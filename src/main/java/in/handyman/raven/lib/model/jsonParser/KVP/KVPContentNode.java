package in.handyman.raven.lib.model.jsonParser.KVP;


import in.handyman.raven.lib.model.jsonParser.Bbox;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j

public class KVPContentNode {
    private String key;
    private String value;
    private double confidence;
    private Bbox bBox;
}

