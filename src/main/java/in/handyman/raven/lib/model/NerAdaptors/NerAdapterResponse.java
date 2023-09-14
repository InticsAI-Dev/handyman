package in.handyman.raven.lib.model.NerAdaptors;

import in.handyman.raven.lib.model.hwDectection.HwDetectionOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NerAdapterResponse {
        private String model_name;
        private String model_version;
        private List<NerAdapterOutput> outputs;
    }

