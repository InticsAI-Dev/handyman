package in.handyman.raven.lib.model.tableExtraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableExtractionResponsePayload {
    private String encode;
    private TableExtractionResponseData tableData;

}
