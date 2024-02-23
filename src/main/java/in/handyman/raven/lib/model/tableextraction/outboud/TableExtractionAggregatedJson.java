package in.handyman.raven.lib.model.tableextraction.outboud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableExtractionAggregatedJson {

    private String columnHeaderKey;
    private String columnHeaderValue;
    private String aggregateFunction;
}
