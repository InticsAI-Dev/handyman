package in.handyman.raven.lib.model.deepSiftSearch;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeepSiftSearchInputTable implements CoproProcessor.Entity {
    private Long Id;
    private Long sorItemId;
    private String sorItemName;
    private Long sorContainerId;
    private String sorContainerName;
    private String sourceDocumentType;
    private String originId;
    private Long rootPipelineId;
    private String searchName;
    private Integer searchId;
    private String keywords;
    private String groupId;
    private Integer tenantId;
    private String batchId;
    private String extractedText;
    private Integer paperNo;
    private Timestamp createdOn;
    private String createdBy;

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }

    @Override
    public List<Object> getRowData() {
        return Stream.of(
                sorItemId,
                sorItemName,
                sorContainerId,
                sorContainerName,
                sourceDocumentType,
                originId,
                rootPipelineId,
                searchName,
                searchId,
                keywords != null ? String.join(",", keywords) : null,
                groupId,
                tenantId,
                batchId,
                extractedText,
                paperNo,
                createdOn,
                createdBy
        ).collect(Collectors.toList());
    }
}