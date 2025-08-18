package in.handyman.raven.lib.ganda;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Builder
@JsonPropertyOrder({"OptionText","status"})
public class Option {
    @JsonProperty("OptionText")
    private String optionText;
    private String status;

}
