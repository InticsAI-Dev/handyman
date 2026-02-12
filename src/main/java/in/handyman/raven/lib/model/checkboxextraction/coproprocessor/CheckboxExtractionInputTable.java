package in.handyman.raven.lib.model.checkboxextraction.coproprocessor;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
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
public class CheckboxExtractionInputTable implements CoproProcessor.Entity {
    private String originId;
    private Long tenantId;
    private String checkboxGroupId;
    private Integer pageNumber;
    private String inputFilePath;
    private String systemPrompt;
    private String userPrompt;
    private String processId;
    private String batchId;
    private Long rootPipelineId;

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }
    @Override
    public List<Object> getRowData() {
        return null;
    }
}
