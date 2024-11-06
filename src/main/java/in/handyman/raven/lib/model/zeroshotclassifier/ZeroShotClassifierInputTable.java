package in.handyman.raven.lib.model.zeroshotclassifier;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;



    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class ZeroShotClassifierInputTable implements CoproProcessor.Entity {

        private String originId;
        private Integer paperNo;
        private String groupId;
        private String pageContent;
        private String truthPlaceholder;
        private String processId;
        private Long rootPipelineId;
        private String keyToFilter;
        private Long tenantId;
        private String batchId;
        private Timestamp createdOn;
        private String base64img;

        @Override
        public List<Object> getRowData() {
            return null;
        }
    }

