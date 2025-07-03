package in.handyman.raven.lib.model.errorResponseJsonTrigger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorJson {
    private String originId;
    private String pipelineStatus;
    private String failedStage;
    private ErrorDetail error;
    private String documentId;
    private Timestamp timestamp; // Or use java.time.Instant if preferred
}
