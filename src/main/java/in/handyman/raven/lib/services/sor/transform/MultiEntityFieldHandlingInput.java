package in.handyman.raven.lib.services.sor.transform;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@RegisterBeanMapper(MultiEntityFieldHandlingInput.class)
public class MultiEntityFieldHandlingInput extends VqaTransactionOutput {
        private Integer multiEntityFilteringId;
        private boolean isRemovedAfterFiltering;
        private String whitelistedSections;

}
