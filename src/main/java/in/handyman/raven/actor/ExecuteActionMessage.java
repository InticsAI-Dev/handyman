package in.handyman.raven.actor;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;

public class ExecuteActionMessage implements Message {

    private final ActionExecutionAudit audit;

    public ExecuteActionMessage(final ActionExecutionAudit audit) {
        this.audit = audit;
    }

    public ActionExecutionAudit getAudit() {
        return audit;
    }
}

