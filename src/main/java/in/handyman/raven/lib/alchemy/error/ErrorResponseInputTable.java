package in.handyman.raven.lib.alchemy.error;

import com.fasterxml.jackson.databind.JsonNode;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ErrorResponseInputTable implements CoproProcessor.Entity{
    private String originId;
    private String transactionId;
    private Integer processId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String baseUrl;
    private String feature;
    private String batchId;
    private String outboundStatus;
    private String errorMessage;
    private String errorCode;
    private String errorJson;
    private String documentId;

    @Override
    public List<Object> getRowData() {
        return null;
    }
    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }
}
