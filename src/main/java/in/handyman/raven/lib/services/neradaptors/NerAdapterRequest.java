package in.handyman.raven.lib.services.neradaptors;

import in.handyman.raven.lib.services.triton.TritonRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NerAdapterRequest {
    private List<TritonRequest> inputs;
}
