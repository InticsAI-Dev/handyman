package in.handyman.raven.lib.model.jsonParser.Text;


import in.handyman.raven.lib.model.jsonParser.Bbox;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContentNode {
    private String content;
    private double confidence;
    private Bbox bBox;
}
