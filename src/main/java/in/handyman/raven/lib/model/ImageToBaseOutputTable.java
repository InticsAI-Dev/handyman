package in.handyman.raven.lib.model;


import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageToBaseOutputTable implements CoproProcessor.Entity {

    private Long Id;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private String originId;
    private Long paperNo;
    private String processedFilePath;
    private String status;
    private String stage;
    private String message;
    private Long rootPipelineId;
    private String templateId;
    private String filePath;
    private String inputFilePath;

    @Override
    public List<Object> getRowData() {
        return null;
    }
}
