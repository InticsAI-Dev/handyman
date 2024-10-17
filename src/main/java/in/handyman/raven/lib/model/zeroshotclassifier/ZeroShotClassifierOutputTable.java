package in.handyman.raven.lib.model.zeroshotclassifier;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class ZeroShotClassifierOutputTable implements CoproProcessor.Entity {
        private String originId;
        private Integer paperNo;
        private String groupId;
        private String truthEntity;

        private String confidenceScore;
        private String entity;

        private String status;

        private String stage;

        private String message;
        private Long rootPipelineId;
        private String modelName;
        private String modelVersion;
        private Long tenantId;
        private String batchId;
        private Timestamp createdOn;
        private Timestamp lastUpdatedOn;
        private String request;
        private String response;
        private String endpoint;

        @Override
        public List<Object> getRowData() {
            return Stream.of(this.originId, this.groupId, this.paperNo, this.entity, this.confidenceScore,
                    this.truthEntity, this.status, this.stage, this.message,this.createdOn, this.rootPipelineId,
                    this.modelName, this.modelVersion,this.tenantId,this.batchId, this.lastUpdatedOn, this.request, this.response, this.endpoint).collect(Collectors.toList());
        }
    }

