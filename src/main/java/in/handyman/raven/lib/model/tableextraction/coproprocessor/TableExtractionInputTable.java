package in.handyman.raven.lib.model.tableextraction.coproprocessor;

import in.handyman.raven.lib.CoproProcessor;
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
public class TableExtractionInputTable implements CoproProcessor.Entity {
    private String originId;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private String templateId;
    private String filePath;
    private String outputDir;
    private Long rootPipelineId;
    private Long paperNo;
    private String modelName;
    private String truthEntityId;
    private Long sorContainerId;
    private Long channelId;


    @Override
    public List<Object> getRowData() {
        return null;
    }
}
