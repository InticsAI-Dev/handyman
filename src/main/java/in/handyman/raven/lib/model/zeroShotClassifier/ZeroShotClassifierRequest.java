package in.handyman.raven.lib.model.zeroShotClassifier;

import in.handyman.raven.lib.model.triton.TritonRequest;
import lombok.*;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ZeroShotClassifierRequest {

        private List<TritonRequest> inputs;
}

