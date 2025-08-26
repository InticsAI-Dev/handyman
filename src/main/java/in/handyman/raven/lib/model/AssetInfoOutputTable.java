package in.handyman.raven.lib.model;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssetInfoOutputTable implements CoproProcessor.Entity {
    private String fileId;
    private Long processId;
    private Long tenantId;
    private Long rootPipelineId;
    private String fileChecksum;
    private String fileExtension;
    private String fileName;
    private String decodedFileName;
    private String filePath;
    private String fileSize;
    private String encode;
    private Float width;
    private Float height;
    private Integer dpi;
    private String batchId;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.fileId, this.processId, this.tenantId, this.rootPipelineId,
                this.fileChecksum, this.fileExtension, this.fileName, this.decodedFileName,
                this.filePath, this.fileSize, this.encode, this.width, this.height, this.dpi,
                this.batchId).collect(Collectors.toList());
    }

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }
}
