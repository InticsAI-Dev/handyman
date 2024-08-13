package in.handyman.raven.lib.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class MasterDataOutputTable implements CoproProcessor.Entity {
        String originId;
        Integer paperNo;
        String eocIdentifier;
        private Timestamp createdOn;
        String extractedValue;
        String actualValue;
        double intelliMatch;
        String status;
        String stage;
        String message;
        private Long rootPipelineId;
        private String modelName;
        private String modelVersion;
        private Long tenantId;
        private String batchId;


        @Override
        public List<Object> getRowData() {
            return Stream.of(this.originId, this.paperNo, this.eocIdentifier, this.createdOn, this.actualValue, this.extractedValue,
                    this.intelliMatch, this.status, this.stage, this.message, this.rootPipelineId,  this.modelName, this.modelVersion, this.tenantId,this.batchId
            ).collect(Collectors.toList());
        }
    }

