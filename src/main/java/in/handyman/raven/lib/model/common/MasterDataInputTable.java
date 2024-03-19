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
    public class MasterDataInputTable implements CoproProcessor.Entity {
        private String originId;
        private Integer paperNo;
        private Integer groupId;
        private Long tenantId;
        private String eocIdentifier;
        private String extractedValue;
        private String actualValue;
        private Long rootPipelineId;
        private String batchId;

        @Override
        public List<Object> getRowData() {
            return null;
        }
    }


