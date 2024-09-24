package in.handyman.raven.lib.model.zeroshotclassifier;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZeroShotClassifierRequestBody {
        private String content;
        private JsonNode labels;

}


