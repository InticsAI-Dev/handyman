package in.handyman.raven.lib.interfaces;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AgadiaAdapter;

public interface ScalarEvaluationInterface {


    int getConfidenceScore(String sentence, AgadiaAdapter adapter, ActionExecutionAudit action) throws Exception;


}
