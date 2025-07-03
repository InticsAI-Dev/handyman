package in.handyman.raven.lib.model.errorResponseJsonTrigger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetailsStackTrace {
    private String exceptionType;
    private String stackTrace;
}
