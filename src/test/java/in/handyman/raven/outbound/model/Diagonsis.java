package in.handyman.raven.outbound.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"cd", "desc", "codePointer"})
public class Diagonsis {
    @JsonProperty("cd")
    private ExtractedField cd;
    @JsonProperty("desc")
    private ExtractedField desc;
    @JsonProperty("codePointer")
    private ExtractedField codePointer;
}
