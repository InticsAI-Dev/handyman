package in.handyman.raven.lib.model.validationLlm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationLlmExtractionRequest {
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String input;
    private String text;
    private String task;
    private String outputDir;
    private List<ValidationExtractionLineItems> prompt;
}
