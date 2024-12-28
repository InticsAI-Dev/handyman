package in.handyman.raven.lib.model.jsonParser.checkbox;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor

public class Option {
    @JsonProperty("OptionText")
    private String OptionText;
    @JsonProperty("Status")
    private String Status;

}
