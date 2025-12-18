package in.handyman.raven.lib.services.neradaptors.copro;

import in.handyman.raven.lib.services.neradaptors.NerAdapterPrediction;
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
