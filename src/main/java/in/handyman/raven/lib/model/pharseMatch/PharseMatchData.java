package in.handyman.raven.lib.model.pharseMatch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PharseMatchData {


        private String rootPipelineId;
        private Long actionId;
        private String process;
        private String originId;
        private String paperNo;
        private String groupId;
        private PharseMatchKeyFilter keysToFilter;
        private String pageContent;

    }
