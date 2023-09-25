package in.handyman.raven.lib.model.tableExtraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableExtractionPayload {
    private Long actionId;
    private String process;
    private String inputFilePath;
    private String outputDirectory;
    private Long rootPipelineId;
}


