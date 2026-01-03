package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AfterMetricsData {
    private Float cpuUsage;
    private Integer totalCores;
    private Integer availableCores;
    private Float usedCores;
    private Float coreUtilizationPercent;
    private Integer ramUsage;
    private String ramUsedMb;
    private String ramTotalMb;
    private String ramAvailableMb;
    private Float diskUsage;
    private String diskTotalGb;
    private String diskFreeGb;
    private String diskTotalMb;
    private String diskFreeMb;
    private List<GPU> gpus;
    private String source;
}
