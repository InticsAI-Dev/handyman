package in.handyman.raven.lib.model.validationLlm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationExtractionResponse {
    private String sectionHeader;
    private String sectionPoints;

}
