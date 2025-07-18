package in.handyman.raven.lib.alchemy.error;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ErrorResponseOutputTable implements CoproProcessor.Entity {

    private String batchId;                 // varchar NULL
    private Long processId;                 // int8 NULL
    private Integer groupId;                // int4 NULL
    private String productResponse;         // varchar NULL
    private String fileName;                // varchar NULL
    private LocalDateTime createdOn;        // timestamp DEFAULT now() NULL
    private LocalDateTime lastUpdatedOn;    // timestamp DEFAULT now() NULL
    private Long tenantId;                  // int8 NULL
    private Long rootPipelineId;            // int8 NULL
    private String originId;                // varchar NULL
    private String status;                  // varchar NULL
    private String stage;                   // varchar NULL
    private String message;                 // varchar NULL
    private String feature;                 // varchar NULL
    private String triggeredUrl;            // varchar NULL
    private String request;                 // text NULL
    private String response;                // text NULL
    private String endpoint;                // text NULL

    @Override
    public List<Object> getRowData() {
        return Stream.of(
                this.batchId,
                this.processId,
                this.groupId,
                this.productResponse,
                this.fileName,
                this.createdOn,
                this.lastUpdatedOn,
                this.tenantId,
                this.rootPipelineId,
                this.originId,
                this.status,
                this.stage,
                this.message,
                this.feature,
                this.triggeredUrl,
                this.request,
                this.response,
                this.endpoint
        ).collect(Collectors.toList());
    }
}
