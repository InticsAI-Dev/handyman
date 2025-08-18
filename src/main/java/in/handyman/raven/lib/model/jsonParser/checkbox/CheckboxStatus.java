package in.handyman.raven.lib.model.jsonParser.checkbox;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j

@JsonPropertyOrder({"OptionText", "Status"})  // Specifying the order of fields in JSON

public class CheckboxStatus {
    private String optionText;
    private String status;
}

