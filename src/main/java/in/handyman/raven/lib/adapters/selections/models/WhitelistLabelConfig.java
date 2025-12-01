package in.handyman.raven.lib.adapters.selections.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhitelistLabelConfig {
    private String whitelistKey;
    private String labelSearchConfig;  // optional if needed
}
