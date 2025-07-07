package in.handyman.raven.lib.model.pharsematch;

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
public class PhraseMatchInputTable implements CoproProcessor.Entity {

    private String originId;
    private Integer paperNo;
    private String groupId;
    private String pageContent;
    private String truthPlaceholder;
    private String processId;
    private Long rootPipelineId;
    private Long tenantId;
    private String batchId;
    private Timestamp createdOn;

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }
    @Override
    public List<Object> getRowData() {
        return null;
    }
}


