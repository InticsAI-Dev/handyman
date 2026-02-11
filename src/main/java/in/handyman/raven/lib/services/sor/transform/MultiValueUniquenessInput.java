package in.handyman.raven.lib.services.sor.transform;


import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@RegisterBeanMapper(OcrTextComparisonInput.class)
public class MultiValueUniquenessInput extends VqaTransactionOutput {

    private Integer multiValueUniquenessId;

}
