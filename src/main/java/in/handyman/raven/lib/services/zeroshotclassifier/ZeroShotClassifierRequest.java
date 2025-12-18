package in.handyman.raven.lib.services.zeroshotclassifier;

import in.handyman.raven.lib.services.triton.TritonRequest;
import lombok.*;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ZeroShotClassifierRequest {

        private List<TritonRequest> inputs;
}

