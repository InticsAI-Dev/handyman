package in.handyman.raven.lib.model.triton;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TritonRequest {

        private String name;
        private List<Integer> shape;
        private String datatype;
        private List<Object> data;

}
