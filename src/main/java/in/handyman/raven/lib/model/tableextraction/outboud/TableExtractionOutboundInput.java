package in.handyman.raven.lib.model.tableextraction.outboud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableExtractionOutboundInput {

    private Long processId;
    private Long groupId;
    private Long tenantId;
    private String templateId;
    private String originId;
    private Long paperNo;
    private String processedFilePath;
    private String tableResponse;
    private String status;
    private String stage;
    private String message;
    private Timestamp createdOn;
    private Long rootPipelineId;
    private String bboxes;
    private String croppedimage;
    private String columnHeader;
    private String truthEntityName;
    private String modelName;
    private Long sorItemId;
    private String tableAggregateFunction;

}
