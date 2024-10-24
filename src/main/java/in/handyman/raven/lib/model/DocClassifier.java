package in.handyman.raven.lib.model;

import in.handyman.raven.lambda.action.ActionContext;
import in.handyman.raven.lambda.action.IActionContext;
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
        actionName = "DocClassifier"
)
public class DocClassifier implements IActionContext {
    private String name;

    private String inputFilePath;

    private String labelModelFilePath;

    private String handwrittenModelFilePath;

    private String checkboxModelFilePath;

    private String labels;

    private String synonyms;

    private String viltConfigLabel;

    private String outputDir;

    private Boolean condition = true;
}
