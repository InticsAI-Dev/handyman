package in.handyman.raven.lib.custom.outbound.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"cd", "desc", "lineNumber", "modifier"})
public class ServiceModifier {

    @JsonProperty("cd")
    private ExtractedField cd;
    @JsonProperty("desc")
    private ExtractedField desc;
    private ExtractedField lineNumber;
    private List<ServiceModifierWrapper> modifier;

    private List<ServiceQuantity> serviceQuantity;
}
