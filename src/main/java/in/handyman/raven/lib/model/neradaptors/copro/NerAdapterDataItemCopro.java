package in.handyman.raven.lib.model.neradaptors.copro;

import in.handyman.raven.lib.model.neradaptors.NerAdapterPrediction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NerAdapterDataItemCopro {
    private List<NerAdapterPrediction> prediction;

}
