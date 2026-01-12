package in.handyman.raven.lib.services.sor.transaction;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SectionFilteringPreGroupingInput extends VqaTransactionOutput {
    private String sectionFilteringId;
    private String blacklistedLabels;
    private String blacklistedSections;
    private String whitelistedLabels;
    private String whitelistedLabelsWithPriority;
    private String sorContainerInstance;

}
