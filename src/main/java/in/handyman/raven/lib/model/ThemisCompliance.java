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
 * Action context for the {@code themisCompliance} Raven keyword. Runs a regulatory
 * labelling-compliance evaluation: the input query supplies the extracted label fields +
 * the THEMIS_COMPLIANCE prompt, the action calls copro (NEON) via CoproProcessor to get the
 * rule verdicts, then deterministically scores the report (pass/warn/fail counts + overall
 * status) inside handyman. Mirrors the NeonKvp context shape.
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ActionContext(
    actionName = "ThemisCompliance"
)
public class ThemisCompliance implements IActionContext {
  private String name;

  private String resourceConn;

  private String endpoint;

  private String outputTable;

  private String querySet;

  private Boolean condition = true;
}
