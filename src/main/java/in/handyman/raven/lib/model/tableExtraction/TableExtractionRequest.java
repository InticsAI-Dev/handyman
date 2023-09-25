package in.handyman.raven.lib.model.tableExtraction;

import in.handyman.raven.lib.model.triton.TritonRequest;
import lombok.*;

import java.util.List;
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableExtractionRequest {
    private List<TritonRequest> inputs;
}
