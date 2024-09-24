package in.handyman.raven.lib.model.table.extraction.headers.copro.legacy.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableResponse {
    public String encode;
    public TableData tableData;
}
