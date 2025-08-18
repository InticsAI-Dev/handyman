package in.handyman.raven.lib.ganda;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
@JsonPropertyOrder({"TableId","Rows","confidence","boundingBox"})
public class Table {
    @JsonProperty("TableId")
    private int tableId;
    @JsonProperty("Rows")
    private List<Map<String,String>> rows;
    private double confidence;
    private BoundingBox boundingBox;
}
