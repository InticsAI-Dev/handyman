package in.handyman.raven.lib.model.paperitemizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaperItemizerOutput {
    private String name;
    private String datatype;
    private List<Integer> shape;
    private List<String> data;
}
