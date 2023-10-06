package in.handyman.raven.lib.model.fileMergerPdf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileMergerPayload {

    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private List<String> inputFilePaths;
    private String outputDir;
    private String outputFileName;
}
