package in.handyman.raven.lib.services.outbound.table;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.services.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AlchemyTableInputEntity implements CoproProcessor.Entity {
    private Integer processId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String alchemyOriginId;
    private String pipelineOriginId;
    private String fileName;
    private Long paperNo;
    private String batchId;

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }
    @Override
    public List<Object> getRowData() {
        return null;
    }
}
