package in.handyman.raven.lib.model.retry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CoproRetryErrorAuditTable {
    private Long id;
    private String originId;
    private Integer groupId;
    private Long tenantId;
    private String templateId;
    private Long processId;
    private String filePath;
    private String fileName;
    private Integer paperNo;
    private String status;
    private String stage;
    private String message;
    private Timestamp createdOn;
    private Long rootPipelineId;
    private String batchId;
    private Timestamp lastUpdatedOn;
    private String request;
    private String response;
    private String endpoint;
    @Builder.Default
    private int attempt = 0;
    private String coproServiceId;
    private String computationDetails;
    private Integer coproStatusCode;
    private String coproLog;
    private String coproDetails;
    private String requestId;
    private Boolean criticalDataPresent;
    
    // Before metrics data fields
    private Double beforeCpuUsage;
    private Integer beforeTotalCores;
    private Integer beforeAvailableCores;
    private Double beforeUsedCores;
    private Double beforeCoreUtilizationPercent;
    private Double beforeRamUsage;
    private String beforeRamUsedMb;
    private String beforeRamTotalMb;
    private String beforeRamAvailableMb;
    private Double beforeDiskUsage;
    private String beforeDiskTotalGb;
    private String beforeDiskFreeGb;
    private String beforeDiskTotalMb;
    private String beforeDiskFreeMb;
    private String beforeSource;
    
    // After metrics data fields
    private Double afterCpuUsage;
    private Integer afterTotalCores;
    private Integer afterAvailableCores;
    private Double afterUsedCores;
    private Double afterCoreUtilizationPercent;
    private Double afterRamUsage;
    private String afterRamUsedMb;
    private String afterRamTotalMb;
    private String afterRamAvailableMb;
    private Double afterDiskUsage;
    private String afterDiskTotalGb;
    private String afterDiskFreeGb;
    private String afterDiskTotalMb;
    private String afterDiskFreeMb;
    private String afterSource;
    
    // Duration field
    private Double duration;

}
