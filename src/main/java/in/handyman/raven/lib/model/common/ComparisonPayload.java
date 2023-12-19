package in.handyman.raven.lib.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ComparisonPayload {
    private Long tenantId;
    private Long processId;
    private String originId;
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputSentence;
    private List<String> sentence;
}