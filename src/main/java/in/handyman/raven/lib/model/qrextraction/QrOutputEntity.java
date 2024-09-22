package in.handyman.raven.lib.model.qrextraction;

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
public class QrOutputEntity implements CoproProcessor.Entity {
    private String originId;
    private Integer groupId;
    private Long paperNo;
    private String fileId;
    private String qrFormat;
    private Integer qrFormatId;
    private String extractedValue;
    private Timestamp createdOn;
    private Double confidenceScore;
    private Integer angle;
    private String b_box;
    private Long rootPipelineId;
    private String status;
    private String stage;
    private String message;
    private String decodeType;
    private String modelName;
    private String modelVersion;
    private Long tenantId;
    private String batchId;


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.groupId, this.paperNo, this.createdOn, this.qrFormat, this.qrFormatId, this.extractedValue,
                this.fileId, this.b_box, this.angle, this.confidenceScore, this.status, this.stage, this.message,this.decodeType, this.modelName,
                this.modelVersion,this.rootPipelineId,this.tenantId,this.batchId).collect(Collectors.toList());
    }

}

