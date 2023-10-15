package in.handyman.raven.lib.ftpdownload;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FtpDownloadOutputTable implements CoproProcessor.Entity {

    private Long id;
    private Long tenantId;
    private Long rootPipelineId;
    private LocalDateTime createdDate;
    private Long createdBy;
    private Long lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private String status;
    private String message;
    private String type;
    private LocalDateTime lastProcessedOn;
    private String ftpFolderPath;
    private String destinationPath;
    private String filePath;


    @Override
    public List<Object> getRowData() {
       return Stream.of(this.filePath, this.destinationPath, this.tenantId, this.ftpFolderPath, this.status, this.message
                , this.type, this.status, this.createdDate, this.rootPipelineId, this.createdBy, this.lastModifiedBy, this.lastModifiedDate).collect(Collectors.toList());
    }
}
