package in.handyman.raven.lib.model.qrextraction;

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
public class QrInputEntity implements CoproProcessor.Entity {

    private String filePath;
    private Integer groupId;
    private String originId;
    private Long paperNo;
    private String fileId;
    private Long rootPipelineId;
    private Long tenantId;

    @Override
    public List<Object> getRowData() {
        return null;
    }


}


