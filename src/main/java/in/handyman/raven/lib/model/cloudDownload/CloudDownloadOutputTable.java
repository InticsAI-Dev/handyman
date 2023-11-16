package in.handyman.raven.lib.model.cloudDownload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class CloudDownloadOutputTable {
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
    private String filePath;
    private String executionStatus;
    private Integer version;
    private String info;

}
