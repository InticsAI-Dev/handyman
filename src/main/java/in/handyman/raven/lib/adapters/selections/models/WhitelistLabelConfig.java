package in.handyman.raven.lib.adapters.selections.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhitelistLabelConfig {
    private String whitelistKey;
    private String labelSearchConfig; // optional if needed
    private String sectionSearchConfig;
    private Integer sectionPriority;

    public WhitelistLabelConfig(String whitelistKey, String labelSearchConfig) {
        this.whitelistKey = whitelistKey;
        this.labelSearchConfig = labelSearchConfig;
    }
}
