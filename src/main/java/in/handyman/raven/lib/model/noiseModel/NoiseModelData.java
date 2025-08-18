package in.handyman.raven.lib.model.noiseModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoiseModelData {

        private Long rootPipelineId;
        private Long actionId;
        private String process;
        private String inputFilePath;
        private String outputDir;
        private String originId;
        private Integer paperNo;
        private Long processId;
        private Integer groupId;
        private Long tenantId;
        private String batchId;
}