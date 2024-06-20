package in.handyman.raven.lib.model.textextraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DataExtractionData {

        private Long rootPipelineId;
        private Long actionId;
        private String process;
        private String inputFilePath;

    }

