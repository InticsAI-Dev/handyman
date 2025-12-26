package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GPU {
    private String gpu;
    private Float gpuLoadPercent;
    private Float gpuRamUsedMb;
    private Float gpuRamTotalMb;
    private Float gpuRamUtilizationPercent;
}



