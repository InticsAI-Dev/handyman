package in.handyman.raven.lib.model.pharsematch;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class PhraseMatchInputTable implements CoproProcessor.Entity {

        private String originId;
        private Integer paperNo;
        private String groupId;
        private String pageContent;
        private String truthPlaceholder;
        private String processId;
        private Long rootPipelineId;
        private Long tenantId;
        private String batchId;

        @Override
        public List<Object> getRowData() {
            return null;
        }
    }


