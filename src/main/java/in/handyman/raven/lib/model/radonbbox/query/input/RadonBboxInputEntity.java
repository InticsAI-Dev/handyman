package in.handyman.raven.lib.model.radonbbox.query.input;

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

public class RadonBboxInputEntity implements CoproProcessor.Entity {

    private String sorContainerName;
    private Long paperNo;
    private String originId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private String inputFilePath;
    private String radonOutput;

    @Override
    public List<Object> getRowData() {
        return null;
    }

}
