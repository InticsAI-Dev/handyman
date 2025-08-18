package in.handyman.raven.lib.model.neradaptors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NerAdapterOutput {

        private String name;
        private String datatype;
        private List<Integer> shape;
        private List<String> data;
    }


