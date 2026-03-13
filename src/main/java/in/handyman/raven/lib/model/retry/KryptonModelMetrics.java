package in.handyman.raven.lib.model.retry;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KryptonModelMetrics {
    private Double execTime;

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
    private String beforeGpuName;
    private Integer beforeGpuLoadPercent;
    private Long beforeGpuRamUsedMb;
    private Long beforeGpuRamTotalMb;
    private Double beforeGpuRamUtilizationPercent;

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
    private String afterGpuName;
    private Integer afterGpuLoadPercent;
    private Long afterGpuRamUsedMb;
    private Long afterGpuRamTotalMb;
    private Double afterGpuRamUtilizationPercent;
}
