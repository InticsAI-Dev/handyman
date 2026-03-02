package in.handyman.raven.lib.services.sor.transform;


import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@RegisterBeanMapper(PostProcessingFieldsInput.class)
public class PostProcessingFieldsInput extends VqaTransactionOutput {
    private Integer postProcessingFieldId;
    private String postProcessingCode;
    private String postProcessingKey;
    private Double aggregatedScore;
}
