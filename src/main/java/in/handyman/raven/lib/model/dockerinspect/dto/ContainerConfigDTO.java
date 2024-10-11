package in.handyman.raven.lib.model.dockerinspect.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContainerConfigDTO {
    @JsonProperty("Hostname")
    private String hostname;

    @JsonProperty("Domainname")
    private String domainname;

    @JsonProperty("User")
    private String user;

    @JsonProperty("Tty")
    private boolean tty;  // Change from String to boolean to match JSON

    @JsonProperty("AttachStdin")
    private boolean attachStdin;

    @JsonProperty("AttachStdout")
    private boolean attachStdout;

    @JsonProperty("AttachStderr")
    private boolean attachStderr;

    @JsonProperty("ExposedPorts")
    private Map<String, Object> exposedPorts;

    @JsonProperty("Env")
    private String[] env;

    @JsonProperty("Cmd")
    private String[] cmd;

    @JsonProperty("Image")
    private String image;

    @JsonProperty("Volumes")
    private Map<String, Object> volumes;

    @JsonProperty("WorkingDir")
    private String workingDir;

    @JsonProperty("Entrypoint")
    private String[] entrypoint;

    @JsonProperty("Labels")
    private Map<String, String> labels;

    // Add missing fields:

    @JsonProperty("OpenStdin")
    private boolean openStdin;

    @JsonProperty("StdinOnce")
    private boolean stdinOnce;

    @JsonProperty("OnBuild")
    private Object onBuild; // Since the JSON has null, Object type can be used.
}
