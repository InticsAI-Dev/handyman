package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.agadia.adapters.DateOfBirthAdapter;
import in.handyman.raven.lib.agadia.adapters.MemberIdAdapter;
import in.handyman.raven.lib.interfaces.ScalarEvaluationInterface;
import in.handyman.raven.lib.model.AgadiaAdapter;
import in.handyman.raven.lib.model.EvalDateOfBirth;

import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "EvalDateOfBirth"
)
public class EvalDateOfBirthAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final EvalDateOfBirth evalDateOfBirth;

    private final Marker aMarker;

    public EvalDateOfBirthAction(final ActionExecutionAudit action, final Logger log,
                                 final Object evalDateOfBirth) {
        this.evalDateOfBirth = (EvalDateOfBirth) evalDateOfBirth;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" EvalDateOfBirth:" + this.evalDateOfBirth.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            ScalarEvaluationInterface dobAdapter = new DateOfBirthAdapter();
            AgadiaAdapter adapter = AgadiaAdapter.builder()
                    .wordCountLimit(evalDateOfBirth.getWordCountLimit())
                    .wordCountThreshold(evalDateOfBirth.getWordCountThreshold())
                    .charCountLimit(evalDateOfBirth.getCharCountLimit())
                    .charCountThreshold(evalDateOfBirth.getCharCountThreshold())
                    .dateFormats(evalDateOfBirth.getDateFormats().split(","))
                    .comparableYear(evalDateOfBirth.getComparableYear())
                    .validatorThreshold(evalDateOfBirth.getValidatorThreshold()).build();
            int score = dobAdapter.getConfidenceScore(evalDateOfBirth.getDob(), adapter);
            action.getContext().put(evalDateOfBirth.getName().concat(".score"), String.valueOf(score));
        } catch (Exception ex) {
            action.getContext().put(evalDateOfBirth.getName().concat(".error"), "true");
            log.info(aMarker, "The Exception occurred ", ex);
            throw new HandymanException("Failed to execute", ex);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return evalDateOfBirth.getCondition();
    }
}
