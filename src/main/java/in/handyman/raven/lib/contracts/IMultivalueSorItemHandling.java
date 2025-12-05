package in.handyman.raven.lib.contracts;

import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.model.soritemhandling.MultiValueOutputResult;
import in.handyman.raven.lib.model.soritemhandling.MultivalueSorItemHandlingActionInput;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public interface IMultivalueSorItemHandling {
    List<MultiValueOutputResult> splitCommaSeparatedValues(List<MultivalueSorItemHandlingActionInput> multivalueSorItemHandlingActionInput, Boolean pipelineEndToEndEncryptionActivator, InticsIntegrity encryption);
    MultiValueOutputResult buildOutputResult( MultivalueSorItemHandlingActionInput input, String answer,  Boolean pipelineEndToEndEncryptionActivator, InticsIntegrity encryption);
    void outputBuilder(Jdbi jdbi, String outputTable, List<MultiValueOutputResult> outputs);
}
