package in.handyman.raven.lib.model.zeroshotclassifier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZeroShotClassifierData {
        private String rootPipelineId;
        private Long actionId;
        private String processId;
        private String process;
        private String originId;
        private String paperNo;
        private String groupId;
        private Map<String, List<String>> keysToFilter;
        private String pageContent;
        private String batchId;

    }

