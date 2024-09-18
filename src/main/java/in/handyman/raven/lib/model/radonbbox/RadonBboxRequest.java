package in.handyman.raven.lib.model.radonbbox;

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

public class RadonBboxRequest {

    private List<TritonRequest> inputs;
}
