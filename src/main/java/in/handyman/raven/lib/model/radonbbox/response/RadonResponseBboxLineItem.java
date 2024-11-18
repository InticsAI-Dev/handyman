package in.handyman.raven.lib.model.radonbbox.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.handyman.raven.lib.model.radonbbox.boundingbox.BoundingBoxObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadonResponseBboxLineItem {
    private String sorItemName;
    private String answer;
    private String valueType;

    @JsonProperty("bBox")
    private BoundingBoxObject bBox;

}
