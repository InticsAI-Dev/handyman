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
       return Stream.of(this.tenantId, this.rootPipelineId, this.createdDate, this.createdBy,
               this.lastModifiedBy, this.lastModifiedDate, this.status, this.message, this.type, this.lastProcessedOn,
               this.ftpFolderPath, this.destinationPath, this.filePath
                ).collect(Collectors.toList());
    }
}
