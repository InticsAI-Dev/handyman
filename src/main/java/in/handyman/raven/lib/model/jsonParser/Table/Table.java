package in.handyman.raven.lib.model.jsonParser.Table;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Slf4j
public class Table {
    @JsonProperty("Table Name")
    private String tableName;
    @JsonProperty("Rows")
    private List<JsonNode> rows;



}
