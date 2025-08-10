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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeepSiftSearchOutputTable implements CoproProcessor.Entity {

    private Long id;
    private Long sorItemId;
    private String sorItemName;
    private Long sorContainerId;
    private String sorContainerName;
    private String sourceDocumentType;
    private String originId;
    private Long rootPipelineId;
    private Integer searchId;
    private String searchName;
    private String batchId;
    private Integer tenantId;
    private Timestamp createdOn;
    private String createdBy;
    private String status;
    private List<String> searchOutput;
    private Integer paperNo;
    private String groupId;

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
                searchId,
                searchName,
                batchId,
                tenantId,
                createdOn,
                createdBy != null ? createdBy : "-1",
                searchOutput != null ? String.join(",", searchOutput) : null,
                paperNo,
                groupId
        ).collect(Collectors.toList());
    }

    @Override
    public String getStatus() {
        return status != null ? status : ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }
}