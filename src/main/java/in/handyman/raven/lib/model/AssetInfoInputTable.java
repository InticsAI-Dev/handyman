package in.handyman.raven.lib.model;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

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
        return Collections.emptyList();
    }

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }

}
