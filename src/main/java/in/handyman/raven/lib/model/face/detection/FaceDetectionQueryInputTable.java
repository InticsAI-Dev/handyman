package in.handyman.raven.lib.model.face.detection;

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
public class FaceDetectionQueryInputTable implements CoproProcessor.Entity  {

    private String originId;
    private String inputFilePath;
    private Integer paperNo;
    private Long rootPipelineId;
    private Long tenantId;
    private Long groupId;
    private Long processId;
    private String process;
    private String batchId;


    @Override
    public List<Object> getRowData() {
        return null;
    }
}