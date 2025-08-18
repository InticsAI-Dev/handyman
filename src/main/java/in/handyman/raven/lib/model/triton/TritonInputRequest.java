package in.handyman.raven.lib.model.triton;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TritonInputRequest {
    private List<Object> inputs;
}
