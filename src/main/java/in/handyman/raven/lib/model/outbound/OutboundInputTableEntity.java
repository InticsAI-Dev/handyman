package in.handyman.raven.lib.model.outbound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class OutboundInputTableEntity {

    private String originId;
    private Long rootPipelineId;
    private Long groupId;
    private Long processId;
    private String cleanedPdfPath;
    private String originPdfPath;
    private String productJson;
    private String kvpResponse;
    private String tableResponse;
    private Long tenantId;
    private String fileName;
    private String alchemyOriginId;
    private String batchId;



}
