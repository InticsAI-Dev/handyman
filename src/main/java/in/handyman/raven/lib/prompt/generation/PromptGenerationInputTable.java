package in.handyman.raven.lib.prompt.generation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromptGenerationInputTable {
    private String  basePrompt;
    private Map<String,String> placeholderInput;
    private String generatedPrompt;
    private Integer promptId;
    private String status;

}
