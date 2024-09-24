package in.handyman.raven.lib.model.utmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UrgencyTriageModelOutput {
    private String name;
    private String datatype;
    private List<Integer> shape;
    private List<String> data;
}
