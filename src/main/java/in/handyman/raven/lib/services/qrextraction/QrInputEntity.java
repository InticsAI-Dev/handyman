package in.handyman.raven.lib.services.qrextraction;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.services.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QrInputEntity implements CoproProcessor.Entity {

    private String filePath;
    private Integer groupId;
    private String originId;
    private Long paperNo;
    private String fileId;
    private Long rootPipelineId;
    private Long tenantId;
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


