package in.handyman.raven.lib.model;

import in.handyman.raven.lambda.action.ActionContext;
import in.handyman.raven.lambda.action.IActionContext;
import java.lang.Boolean;
import java.lang.String;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Auto Generated By Raven
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ActionContext(
    actionName = "CoproStart"
)
public class CoproStart implements IActionContext {
  private String name;

  private String moduleName;

  private String coproServerUrl;

  private String exportCommand;

  private String processID;

  private String resourceConn;

  private String command;

  private Boolean condition = true;
}
