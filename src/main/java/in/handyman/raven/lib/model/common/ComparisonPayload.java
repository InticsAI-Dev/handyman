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

    private String inputSentence;
    private List<String> sentence;
    private Long rootPipelineId;
    private Long actionId;
    private String process;

}