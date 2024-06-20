package in.handyman.raven.lib.model.pharseMatch;

import com.fasterxml.jackson.databind.JsonNode;
import in.handyman.raven.lib.CoproProcessor;
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
    public class PhraseMatchInputTable implements CoproProcessor.Entity {

        private String originId;
        private Integer paperNo;
        private String groupId;
        private String pageContent;
        private String truthPlaceholder;
        private String processId;
        private Long rootPipelineId;

        @Override
        public List<Object> getRowData() {
            return null;
        }
    }


