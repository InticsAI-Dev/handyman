package in.handyman.raven.lib.model.filemergerpdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TritonRequest {

    private String name;
    private List<Integer> shape;
    private String datatype;
    private List<String> data;
}
