package in.handyman.raven.lib.model.jsonParser.Table;


import com.fasterxml.jackson.databind.JsonNode;
import in.handyman.raven.lib.model.jsonParser.Bbox;
import in.handyman.raven.lib.model.jsonParser.BoundingBox;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableContentNode {
    private Integer tableId;
    private JsonNode rows;
    private double confidence;
    private BoundingBox bounding_box;
}

