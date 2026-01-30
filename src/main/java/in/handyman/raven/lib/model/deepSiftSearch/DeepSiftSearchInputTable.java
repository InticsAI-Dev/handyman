package in.handyman.raven.lib.model.deepSiftSearch;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
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
public class DeepSiftSearchInputTable implements CoproProcessor.Entity {
    private Long id;
    private String originId;
    private String groupId;
    private Timestamp createdOn;
    private String createdBy;
    private String extractedText;
    private Long rootPipelineId;
    private Long tenantId;
    private String batchId;
    private Integer paperNo;
    private String sourceDocumentType;
    private Long sorContainerId;
    private String sorContainerName;
    private Long sorItemId;
    private String sorItemName;
    private Long searchId;
    private String searchName;
    private String keywords;
    private Integer fieldPaperCount;
    private Boolean fieldConsiderBlankPages;
    private Boolean isBlankPage;
    private String pageRange;

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }

    @Override
    public List<Object> getRowData() {
        return null;
    }
}