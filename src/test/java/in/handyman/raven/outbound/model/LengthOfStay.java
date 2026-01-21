package in.handyman.raven.outbound.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LengthOfStay {
    @JsonProperty("levelOfCare")
    private ExtractedField levelOfCare;


}
