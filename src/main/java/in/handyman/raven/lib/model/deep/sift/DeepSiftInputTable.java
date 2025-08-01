package in.handyman.raven.lib.model.deep.sift;

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
public class DeepSiftInputTable implements CoproProcessor.Entity {
    private Long id;
    private String originId;
    private Integer groupId;
    private String inputFilePath;
    private Timestamp createdOn;
    private String createdBy;
    private Long rootPipelineId;
    private Long tenantId;
    private String batchId;
    private Integer paperNo;
    private String sourceDocumentType;
    private Long sorItemId;
    private String sorItemName;
    private Integer sorContainerId;
    private String containerDocumentType;
    private String sorContainerName;
    private Integer modelId;
    private String modelName;
    private String searchName;
    private String basePrompt;
    private String systemPrompt;

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }

    @Override
    public List<Object> getRowData() {
        return null;
    }
}