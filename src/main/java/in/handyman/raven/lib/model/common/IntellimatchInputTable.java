package in.handyman.raven.lib.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class IntellimatchInputTable implements CoproProcessor.Entity {
        private String fileName;
        private String originId;
        private Integer groupId;
        private Long tenantId;
        private Long rootPipelineId;
        private String actualValue;
        private String extractedValue;
        private Double similarity;
        private Integer confidenceScore;
        @Override
        public List<Object> getRowData() {
            return null;
        }
    }

