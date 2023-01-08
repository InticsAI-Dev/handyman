package in.handyman.raven.lib;

import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DonutDocqa;
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
    actionName = "DonutDocqa"
)
public class DonutDocqaAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final DonutDocqa donutDocqa;

  private final Marker aMarker;

  public DonutDocqaAction(final ActionExecutionAudit action, final Logger log,
      final Object donutDocqa) {
    this.donutDocqa = (DonutDocqa) donutDocqa;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" DonutDocqa:"+this.donutDocqa.getName());
  }

  @Override
  public void execute() throws Exception {
  }

  @Override
  public boolean executeIf() throws Exception {
    return donutDocqa.getCondition();
  }
}
