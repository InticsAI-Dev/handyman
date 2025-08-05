package in.handyman.raven.lib.model.testDataExtractor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestDataExtractorInput {
    private Boolean condition = true;
    private String name;
    private List<String> inputFilePaths;
    private String mode;
    private String outputPath;
    private List<String> keywords;
    private String resourceConn;
    private String resultTable;
    private String endPoint;
    private String processId;
    private String querySet;
}