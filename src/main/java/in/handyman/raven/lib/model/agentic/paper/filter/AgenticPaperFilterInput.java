package in.handyman.raven.lib.model.agentic.paper.filter;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgenticPaperFilterInput  implements CoproProcessor.Entity {
    public String process;
    private String originId;
    private Integer groupId;
    private String filePath;
    private Integer paperNo;
    private Long tenantId;
    private String templateId;
    private Long processId;
    private Long rootPipelineId;
    private String templateName;
    private String batchId;
    private Timestamp createdOn;
    private String userPrompt;
    private String systemPrompt;
    private String base64Img;

    @Override
    public List<Object> getRowData() {
        return Collections.emptyList();
    }

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }

}
