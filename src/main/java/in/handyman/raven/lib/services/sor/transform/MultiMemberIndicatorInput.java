package in.handyman.raven.lib.services.sor.transform;


import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@RegisterBeanMapper(MultiMemberIndicatorInput.class)
public class MultiMemberIndicatorInput extends VqaTransactionOutput{

    private Long multiMemberIndicatorId;
}
