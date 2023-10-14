package in.handyman.raven.lib.model;

import in.handyman.raven.lambda.action.ActionContext;
import in.handyman.raven.lambda.action.IActionContext;
import java.lang.Boolean;
import java.lang.String;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;

/**
 * Auto Generated By Raven
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ActionContext(
    actionName = "ImageToEncodedFile"
)
public class ImageToEncodedFile implements IActionContext {
  private String name;

  private String outputTable;
  private String resourceConn;
  private String querySet;
  private String outputDir;
  private Boolean condition = true;


  public ImageToEncodedFile(ActionExecutionAudit action, Logger log, ImageToEncodedFile imageToEncodedFile) {
  }
}
