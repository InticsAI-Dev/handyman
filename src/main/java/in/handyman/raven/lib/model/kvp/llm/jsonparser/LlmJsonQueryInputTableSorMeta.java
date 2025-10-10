package in.handyman.raven.lib.model.kvp.llm.jsonparser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LlmJsonQueryInputTableSorMeta {
    private String sorItemName;
    private String isEncrypted;
    private String encryptionPolicy;
    private String sorItemLabels;
}
