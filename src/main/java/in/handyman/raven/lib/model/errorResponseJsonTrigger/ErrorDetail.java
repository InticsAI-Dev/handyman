package in.handyman.raven.lib.model.errorResponseJsonTrigger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetail {
    private String errorCode;
    private String errorMessage;
    private ErrorDetailsStackTrace errorDetails;
}
