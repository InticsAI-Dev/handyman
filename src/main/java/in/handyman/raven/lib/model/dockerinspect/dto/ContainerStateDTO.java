package in.handyman.raven.lib.model.dockerinspect.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContainerStateDTO {
    @JsonProperty("Status")
    private String status;
    @JsonProperty("Running")
    private boolean running;
    @JsonProperty("Paused")
    private boolean paused;
    @JsonProperty("Restarting")
    private boolean restarting;
    @JsonProperty("OOMKilled")
    private boolean oOMKilled;
    @JsonProperty("Dead")
    private boolean dead;
    @JsonProperty("ExitCode")
    private int exitCode;
    @JsonProperty("Pid")
    private int pid;
    @JsonProperty("Error")
    private String error;
    @JsonProperty("StartedAt")
    private String startedAt;
    @JsonProperty("FinishedAt")
    private String finishedAt;


}
