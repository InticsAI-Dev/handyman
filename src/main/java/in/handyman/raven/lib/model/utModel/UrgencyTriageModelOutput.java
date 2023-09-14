package in.handyman.raven.lib.model.utModel;

import in.handyman.raven.lib.model.TextExtraction.DataExtractionDataItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UrgencyTriageModelOutput {
    private String name;
    private String datatype;
    private List<Integer> shape;
    private List<UrgencyTriageModelDataItem> data;
}