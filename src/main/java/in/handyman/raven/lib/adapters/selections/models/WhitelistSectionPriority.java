package in.handyman.raven.lib.adapters.selections.models;


import lombok.Data;

@Data
public class WhitelistSectionPriority {
    private String whitelistKey;
    private Integer sectionPriority;
    private String sectionSearchConfig;  // optional if needed
}
