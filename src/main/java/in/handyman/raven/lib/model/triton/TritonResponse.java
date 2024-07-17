package in.handyman.raven.lib.model.triton;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TritonResponse {

    private String model_name;
    private String model_version;
    private String output;
}
