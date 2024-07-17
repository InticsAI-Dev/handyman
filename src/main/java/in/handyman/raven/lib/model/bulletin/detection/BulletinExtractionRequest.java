package in.handyman.raven.lib.model.bulletin.detection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulletinExtractionRequest {
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String input;
    private String task;
    private String outputDir;
    private List<BulletinExtractionLineItems> prompt;

}
