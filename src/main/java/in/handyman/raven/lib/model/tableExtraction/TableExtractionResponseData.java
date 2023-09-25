package in.handyman.raven.lib.model.tableExtraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableExtractionResponseData
{
    private List<Integer> columns;
    private List<List<String>> data;
}
