package in.handyman.raven.lib.model.deep.sift;

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
public class DeepSiftInputTable implements CoproProcessor.Entity {
    private Long id;
    private Long rootPipelineId;
    private String originId;
    private String batchId;
    private String filePath;
    private Long tenantId;
    private Integer groupId;
    private Long processId;
    private String templateId;
    private Long sorItemId;
    private String sorItemName;
    private Long sorContainerId;
    private Integer paperNo;
    private String status;
    private String stage;
    private Long modelId;
    private Long searchId;
    private String userPrompt;
    private String templateName;
    private String systemPrompt;
    private Timestamp createdOn;
    private String base64Img;

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }

    @Override
    public List<Object> getRowData() {
        return null;
    }
}