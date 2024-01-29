package in.handyman.raven.lib.model.tableextraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableRequestRoot {
    private String rootPipelineId;
    private String actionId;
    private String process;
    private String tenantId;
    private String originId;
    private String paperNo;
    private String groupId;
    private String inputFilePath;
    private String outputDir;
    private List<TableHeader> tableHeaders;
}
