package in.handyman.raven.lib.model;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor

public class FtpConnectionCheckTable{
    private Long tenantId;
    private String type;
    private String info;
    private Long rootPipelineId;
    private String status;
    private String message;
    private LocalDateTime lastProcessedOn;
    private boolean ftpconnected;
    private LocalDateTime createdDate = LocalDateTime.now();
    private LocalDateTime lastModifiedDate;
    private Long createdBy = -1L;
    private Long lastModifiedBy = -1L;


//    public FtpConnectionCheckTable(Long tenantId, Long rootPipelineId,String status, String message, String type,
//                                   String info, LocalDateTime lastProcessedOn, boolean isFtpConnected) {
//        this.tenantId = tenantId;
//        this.rootPipelineId = rootPipelineId;
//        this.status = status;
//        this.message = message;
//        this.type = type;
//        this.info = info;
//        this.lastProcessedOn = lastProcessedOn;
//        this.isFtpConnected = isFtpConnected;
//    }

//    @Override
//    public String toString() {
//        return "YourTableData{" +
//                "tenantId=" + tenantId +
//                ", rootPipelineId=" + rootPipelineId +
//                ", status='" + status + '\'' +
//                ", message='" + message + '\'' +
//                ", type='" + type + '\'' +
//                ", info='" + info + '\'' +
//                ", lastProcessedOn=" + lastProcessedOn +
//                ", isFtpConnected=" + isFtpConnected +
//                '}';
//    }
}


