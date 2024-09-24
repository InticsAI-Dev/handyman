package in.handyman.raven.lib.model.pharsematch;

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
public class PharseMatchData {


        private Integer rootPipelineId;
        private Long actionId;
        private String process;
        private String originId;
        private Long paperNo;
        private String groupId;
        private Map<String, List<String>> keysToFilter;
        private String pageContent;

    }

