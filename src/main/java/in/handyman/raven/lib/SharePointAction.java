package in.handyman.raven.lib;

import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.SharePoint;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "SharePoint"
)
public class SharePointAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final SharePoint sharePoint;

    private final Marker aMarker;

    public SharePointAction(final ActionExecutionAudit action, final Logger log,
                            final Object sharePoint) {
        this.sharePoint = (SharePoint) sharePoint;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" SharePoint:" + this.sharePoint.getName());
    }

    @Override
    public void execute() throws Exception {
    }

    @Override
    public boolean executeIf() throws Exception {
        return sharePoint.getCondition();
    }
}
