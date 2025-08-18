package in.handyman.raven.lib.model.hwdectection;

import in.handyman.raven.lib.model.triton.TritonRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class HwDetectionRequest {
    private List<TritonRequest> inputs;

}
