package in.handyman.raven.lib.model.pharsematch;

import in.handyman.raven.lib.model.triton.TritonRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PharseMatchRequest {

    private List<TritonRequest> inputs;
}

