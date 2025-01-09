package in.handyman.raven.lib.model.jsonParser.Table;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
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
@JsonPropertyOrder({"TableId", "Rows", "Confidence", "BoundingBox"})
public class TableContentNode {
    @JsonProperty("TableId")
    private Integer tableId;
    @JsonProperty("Rows")
    private JsonNode rows;
    private double confidence;
    private Bbox boundingBox;
}

