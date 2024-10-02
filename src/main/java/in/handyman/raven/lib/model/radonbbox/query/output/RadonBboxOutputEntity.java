package in.handyman.raven.lib.model.radonbbox.query.output;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder


public class RadonBboxOutputEntity implements CoproProcessor.Entity {

    private String sorContainerName;
    private String sorItemName;
    private String answer;
    private Long paperNo;
    private String originId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private String inputFilePath;
    private String bBox;
    private String valueType;
    private String status;
    private String stage;
    private String message;


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.sorContainerName, this.sorItemName, this.answer, this.valueType, this.bBox, this.paperNo, this.originId, this.inputFilePath,
                this.groupId, this.tenantId, this.rootPipelineId, this.batchId, this.modelRegistry, this.status, this.stage, this.message).collect(Collectors.toList());
    }
}
