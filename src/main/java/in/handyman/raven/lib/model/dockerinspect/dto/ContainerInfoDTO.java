package in.handyman.raven.lib.model.dockerinspect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContainerInfoDTO {
    private String id;
    private String name;
    private String[] image;
    private String status;
    private String gateWay;
    private String dockerIp;
    private String created;
    private ContainerStateDTO state;
    private ContainerConfigDTO config;
    @JsonProperty("host_config")
    private Map<String, Object> hostConfig;
    @JsonProperty("network_settings")
    private Map<String, Object> networkSettings;
    private Object[] mounts;
    private Map<String, Object> volumes;
    private Map<String, Object> ports;
    private String[] environment;
    private Map<String, String> labels;
    private String[] command;

    @JsonProperty("working_dir")
    private String workingDir;
    private String[] entrypoint;

    @JsonProperty("log_path")
    private String logPath;

    // Getters and Setters
}
