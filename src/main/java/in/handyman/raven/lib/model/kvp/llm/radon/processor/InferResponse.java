package in.handyman.raven.lib.model.kvp.llm.radon.processor;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class InferResponse {
    private JsonNode lines;
}
