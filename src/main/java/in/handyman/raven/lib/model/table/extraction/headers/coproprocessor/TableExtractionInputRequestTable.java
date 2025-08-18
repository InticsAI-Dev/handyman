package in.handyman.raven.lib.model.table.extraction.headers.coproprocessor;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.tableextraction.TableHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//1. input pojo from select query, which implements CoproProcessor.Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableExtractionInputRequestTable {
    private Long tenantId;
    private Long rootPipelineId;
    private Long groupId;
    private String originId;
    private Long paperNo;
    private String outputDir;
    private String inputFilePath;
    private List<TableHeader> tableHeaders;
    private Long actionId;
    private String process;

}
