package in.handyman.raven.lib.services.sor.transform;


import lombok.Builder;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class MultiValueUniquenessInput extends VqaTransactionOutput {

    private Integer multiValueUniquenessId;

}
