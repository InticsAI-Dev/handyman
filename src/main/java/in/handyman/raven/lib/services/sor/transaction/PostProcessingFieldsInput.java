package in.handyman.raven.lib.services.sor.transaction;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostProcessingFieldsInput extends VqaTransactionOutput {
    private Integer postProcessingFieldId;
    private String postProcessingCode;
    private String postProcessingKey;
}
