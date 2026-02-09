package in.handyman.raven.lib.custom.outbound.model;

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
@JsonPropertyOrder({"quantityType", "quantityUnits"})
public class ServiceQuantity {

    private SimpleValueField quantityType;

    @JsonProperty("quantityUnits")
    private ExtractedField quantityUnits;
}
