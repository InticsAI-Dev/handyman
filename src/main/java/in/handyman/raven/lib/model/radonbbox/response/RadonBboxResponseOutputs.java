package in.handyman.raven.lib.model.radonbbox.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadonBboxResponseOutputs {

    private String name;
    private String datatype;
    private List<String> shape;
    private List<String> data;
}
