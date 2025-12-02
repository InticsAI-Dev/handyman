package in.handyman.raven.lib.adapters.selections.models;


import lombok.Data;

@Data
public class WhitelistLabelPriority {
    private String whitelistKey;
    private Integer labelPriority;
    private String labelSearchConfig;  // optional if needed
}
