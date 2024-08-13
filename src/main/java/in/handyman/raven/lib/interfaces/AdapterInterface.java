package in.handyman.raven.lib.interfaces;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.Validator;

public interface AdapterInterface {
    int getThresholdScore(String sentence) throws Exception;

    boolean getValidationModel(String sentence, String requiredFeature,ActionExecutionAudit audit) throws Exception;

    boolean getNameValidationModel(Validator input, String uri, ActionExecutionAudit audit) throws Exception;

    boolean getDateValidationModel(String sentence, int comparableYear, String[] dateFormats) throws Exception;

}
