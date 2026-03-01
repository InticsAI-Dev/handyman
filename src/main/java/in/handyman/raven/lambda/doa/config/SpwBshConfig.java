package in.handyman.raven.lambda.doa.config;

import in.handyman.raven.lambda.doa.Auditable;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpwBshConfig extends Auditable {

    private Long id;
    private Long tenantId;
    private String callerName;
    private String className;
    private String sourceCode;
    private String referenceId;
    private String status;
    private Integer version;

}
