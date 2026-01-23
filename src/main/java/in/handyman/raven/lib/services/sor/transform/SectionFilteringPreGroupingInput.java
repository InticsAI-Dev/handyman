package in.handyman.raven.lib.services.sor.transform;


import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@RegisterBeanMapper(SectionFilteringPreGroupingInput.class)
public class SectionFilteringPreGroupingInput extends VqaTransactionOutput {
    private String sectionFilteringId;
    private String blacklistedLabels;
    private String blacklistedSections;
    private String whitelistedLabels;
    private String whitelistedLabelsWithPriority;
    private String sorContainerInstance;

}
