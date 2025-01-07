package in.handyman.raven.lib.model.jsonParser.checkbox;


import in.handyman.raven.lib.model.jsonParser.Bbox;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckboxStatus {
    private String optionText;
    private String status;
}

