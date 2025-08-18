package in.handyman.raven.lib.model.noiseModel;

import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
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
    private String batchId;
    private Timestamp createdOn;
    @Override
    public List<Object> getRowData() {
        return null;
    }

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }
}