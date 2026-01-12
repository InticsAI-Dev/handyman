package in.handyman.raven.lib.services.sor.transaction;

import lombok.*;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@RegisterBeanMapper(MultiMemberMapperInput.class)
public class MultiMemberMapperInput extends VqaTransactionOutput{
        private Integer multiMemberMapperId;
        private boolean removedAfterFiltering;

}
