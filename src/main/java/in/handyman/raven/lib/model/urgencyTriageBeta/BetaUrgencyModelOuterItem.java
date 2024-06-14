package in.handyman.raven.lib.model.urgencyTriageBeta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BetaUrgencyModelOuterItem {

    private BetaUrgencyModelItems urgencyCheckboxModel;
    private BetaUrgencyModelItems urgencyHandwrittenModel;
    private BetaUrgencyModelItems urgencyBinaryModel;
    private String originId;
    private Integer paperNo;
    private String processId;
    private Integer groupId;
    private Integer tenantId;
    private Integer rootPipelineId;
}