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
        actionName = "CopyFile"
)
public class CopyFile implements IActionContext {
    private String name;

    private String srcLocation;

    private String destLocation;

    private String fileName;

    private String extension;

    private String value;

    @Builder.Default
    private Boolean condition = true;
}
