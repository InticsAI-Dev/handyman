package in.handyman.raven.lib.model.tableExtraction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableExtractionDataItem {
    private List<String > csvTablesPath;
    @JsonProperty("table_response")
    private TableExtractionTableResponse tableResponse;

}
