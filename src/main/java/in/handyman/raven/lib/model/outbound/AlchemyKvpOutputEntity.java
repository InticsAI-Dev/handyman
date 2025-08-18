package in.handyman.raven.lib.model.outbound;

import com.fasterxml.jackson.databind.JsonNode;
import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlchemyKvpOutputEntity  implements CoproProcessor.Entity{
    private Integer processId;
    private Long groupId;
    private String alchemyOriginId;
    private String pipelineOriginId;
    private String kvpResponse;
    private Long tenantId;
    private String fileName;
    private Long rootPipelineId;
    private String status;
    private String stage;
    private String message;
    private String batchId;


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.processId, this.groupId, this.alchemyOriginId, this.pipelineOriginId,this.kvpResponse, this.tenantId, this.fileName, this.rootPipelineId,this.stage,this.stage,this.message,this.batchId).collect(Collectors.toList());

    }
}
