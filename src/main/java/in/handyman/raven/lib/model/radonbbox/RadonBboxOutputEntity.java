package in.handyman.raven.lib.model.radonbbox;

import in.handyman.raven.lib.CoproProcessor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder


public class RadonBboxOutputEntity implements CoproProcessor.Entity{

    private String originId;
    private Long paperNo;
    private Long processId;
    private Long groupId;
    private Long tenantId;
    private String inputFilePath;

    private LocalDateTime createdOn;
    private Long rootPipelineId;
    private String status;
    private String stage;
    private String message;
    private String batchId;
    @Override
    public List<Object> getRowData() {
        return Stream.of( this.originId, this.paperNo,
                this.processId, this.groupId, this.tenantId,this.inputFilePath, this.createdOn,this.rootPipelineId,
                this.status,this.stage,this.message,this.batchId).collect(Collectors.toList());
    }
}
