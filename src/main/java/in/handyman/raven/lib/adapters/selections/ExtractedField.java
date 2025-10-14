package in.handyman.raven.lib.adapters.selections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractedField {
    private Integer id;
    private String label;
    private String sectionAlias;
    private String value;
    private Set<String> blacklistedLabels;
    private Set<String> blacklistedSections;
    private boolean isLabelMatching;
    private String labelMatchMessage;

}
