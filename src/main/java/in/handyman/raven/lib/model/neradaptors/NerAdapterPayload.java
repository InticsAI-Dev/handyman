package in.handyman.raven.lib.model.neradaptors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NerAdapterPayload {

        private List<String> inputString;
        private Long rootPipelineId;
        private Long actionId;
        private String process;
        private String originId;
        private Long processId;
        private Integer groupId;
        private Long tenantId;
}
