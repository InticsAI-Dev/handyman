package in.handyman.raven.lib.model.tableExtraction;

import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableExtractionOutput {
    private String name;
    private String datatype;
    private List<Integer> shape;
    private List<String> data;
}
