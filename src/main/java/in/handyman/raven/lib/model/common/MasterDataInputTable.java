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
        String originId;
        Integer paperNo;
        Integer groupId;
        Long tenantId;
        String eocIdentifier;
        String extractedValue;
        String actualValue;
        private Long rootPipelineId;

        @Override
        public List<Object> getRowData() {
            return null;
        }
    }


