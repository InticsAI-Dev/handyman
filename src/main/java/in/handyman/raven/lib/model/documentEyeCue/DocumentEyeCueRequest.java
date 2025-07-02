package in.handyman.raven.lib.model.documentEyeCue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DocumentEyeCueRequest {
    private String originId;
    private String batchId;
    private String processId;
    private String groupId;
    private int tenantId;
    private int rootPipelineId;
    private String process;
    private Integer actionId;
    private String inputFilePath;
    private String outputDir;
    private String base64Pdf;
    private String language = "eng";
    private Boolean forceOcr = false;
    private Boolean skipTextPages = true;
    private Integer maxPaperCount;
}
