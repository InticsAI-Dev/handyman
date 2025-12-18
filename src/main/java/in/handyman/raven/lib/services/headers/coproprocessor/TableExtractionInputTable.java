package in.handyman.raven.lib.services.headers.coproprocessor;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.services.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//1. input pojo from select query, which implements CoproProcessor.Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableExtractionInputTable implements CoproProcessor.Entity {
    private Long tenantId;
    private Long rootPipelineId;
    private Long groupId;
    private String originId;
    private Long paperNo;
    private String documentType;
    private String templateName;
    private String filePath;
    private String tableHeaders;
    private String modelName;
    private Long truthEntityId;
    private Long sorContainerId;
    private Long channelId;
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
