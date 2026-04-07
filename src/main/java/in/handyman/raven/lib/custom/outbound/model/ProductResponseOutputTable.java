package in.handyman.raven.lib.custom.outbound.model;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Data
@Builder
public class ProductResponseOutputTable implements CoproProcessor.Entity {
    private Integer processId;
    private Long groupId;
    private String productResponse;
    private String originId;
    private Long tenantId;
    private Long rootPipelineId;
    private String status;
    private String stage;
    private String message;
    private String triggeredUrl;
    private String feature;
    private String batchId;
    private String inboundTransactionId;
    private LocalDateTime createdOn;
    private LocalDateTime lastUpdatedOn;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.processId, this.groupId, this.originId, this.productResponse, this.tenantId, this.rootPipelineId,
                this.status, this.stage, this.message, this.feature, this.triggeredUrl,
                this.batchId, this.inboundTransactionId, this.createdOn, this.lastUpdatedOn).collect(Collectors.toList());
    }

    @Override
    public String getStatus() {
        return this.status;
    }
}
