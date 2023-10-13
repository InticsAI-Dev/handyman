package in.handyman.raven.lib.model.fileMergerPdf;

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


    public class FileMergerpdfInputEntity implements CoproProcessor.Entity {
    private List<String> filePaths;
    private Integer groupId;
    private String originId;
    private Integer paperNo;
    private String fileId;
    private Long rootPipelineId;
    private String paperno;
    private String tenantId;
    private String templateId;
    private String actionId;
    private String outputFileName;


    @Override
    public List<Object> getRowData() {
        return null;
    }

}
