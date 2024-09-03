package in.handyman.raven.lib.prompt.generation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SanitarySummary {
    private int rowCount;
    private int correctRowCount;
    private int errorRowCount;
    private String comments;
    private Long tenantId;

}
