package in.handyman.raven.lib.model;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
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
public class AssetInfoInputTable implements CoproProcessor.Entity {
    private int inboundId;
    private String createdOn;
    private String createdUserId;
    private String lastUpdatedOn;
    private String lastUpdatedUserId;
    private Long tenantId;
    private String filePath;
    private String documentId;
    private String batchId;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.inboundId, this.createdOn, this.createdUserId, this.lastUpdatedOn,
                this.lastUpdatedUserId, this.tenantId, this.filePath, this.documentId, this.batchId).collect(Collectors.toList());
    }

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }

}
