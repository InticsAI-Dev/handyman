package in.handyman.raven.lib.model.dockerinspect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DockerInspectApiResponseDTO {
    @JsonProperty("running_containers")
    private List<ContainerInfoDTO> runningContainers;

}
