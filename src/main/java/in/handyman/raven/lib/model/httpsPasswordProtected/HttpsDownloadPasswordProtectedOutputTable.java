package in.handyman.raven.lib.model.httpsPasswordProtected;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HttpsDownloadPasswordProtectedOutputTable {
    private Long tenantId;
    private Long rootPipelineId;
    private LocalDateTime createdOn;
    private Long createdUserId;
    private Long lastUpdatedUserId;
    private LocalDateTime lastUpdatedOn;
    private String status;
    private String message;
    private String type;
    private LocalDateTime lastProcessedOn;
    private String localFilePath;
    private String extractedLocalDirectoryPath;
    private String extractedFilePaths;
    private String executionStatus;
    private Integer version;
    private String info;
}
