package in.handyman.raven.lib.model.outbound.table;

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
public class AlchemyTableOutputEntity implements CoproProcessor.Entity{
    private Integer processId;
    private Long groupId;
    private String alchemyOriginId;
    private String pipelineOriginId;
    private String tableResponse;
    private Long tenantId;
    private Long rootPipelineId;
    private Long paperNo;
    private String status;
    private String stage;
    private String message;
    private String batchId;


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.processId, this.groupId, this.alchemyOriginId, this.pipelineOriginId,this.tableResponse, this.tenantId, this.rootPipelineId,this.stage,this.stage,this.message,this.paperNo,this.batchId).collect(Collectors.toList());

    }
}
