package in.handyman.raven.lib.model.noiseModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import in.handyman.raven.lib.CoproProcessor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class NoiseModelInputEntity implements CoproProcessor.Entity {
    private Long rootPipelineId;
    private Long actionId;
    private String inputFilePath;
    private Integer groupId;
    private String originId;
    private Integer paperNo;
    private String fileId;
    private Long tenantId;
    private String outputDir;
    @Override
    public List<Object> getRowData() {
        return null;
    }


}