package in.handyman.raven.lib.model.jsonParser.checkbox;


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

public class CheckboxStatus {
    private String optionText;
    private String status;
}

