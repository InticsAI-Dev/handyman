package in.handyman.raven.lib.model.dockerinspect.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DockerInspectInputTable {

    private Long machineId;
    private String machineName;
    private String machineIp;
    private String machineHostname;
    private String etcHostName;
    private Long clusterId;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private Long createdUserId;
    private Long updatedUserId;
    private Integer version;
    private String status;
    private String utilsUrl;
    private Integer tenantId;
    private Integer rootPipelineId;
    private Integer groupId;

}
