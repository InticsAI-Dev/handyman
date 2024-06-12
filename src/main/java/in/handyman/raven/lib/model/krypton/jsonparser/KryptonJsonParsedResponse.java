package in.handyman.raven.lib.model.krypton.jsonparser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KryptonJsonParsedResponse {
    private String sorContainerName;
    private String sorItemName;
    private String answer;
}
