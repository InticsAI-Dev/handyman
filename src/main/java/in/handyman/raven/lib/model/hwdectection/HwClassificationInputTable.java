package in.handyman.raven.lib.model.hwdectection;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

   @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public class HwClassificationInputTable implements CoproProcessor.Entity{
        private String createdUserId;
        private String lastUpdatedUserId;
        private Long tenantId;
        private Double modelScore;
        private String originId;
        private Integer paperNo;
        private Integer groupId;
        private String templateId;
        private Long modelId;
        private String filePath;
        private Long rootPipelineId;
        private String outputDir;
        private Long processId;
        private String batchId;
        private Timestamp createdOn;

       @Override
       public String getStatus() {
           return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
       }

        @Override
        public List<Object> getRowData() {
            return null;
        }
    }

