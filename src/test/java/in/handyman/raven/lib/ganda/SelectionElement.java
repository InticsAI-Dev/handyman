package in.handyman.raven.lib.ganda;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
@JsonPropertyOrder({"Question","Options","boundingBox","confidence"})
public class SelectionElement {
    @JsonProperty("Question")
    private String question;
    @JsonProperty("Options")
    private List<Option> options;
    private BoundingBox boundingBox;
    private double confidence;

}
