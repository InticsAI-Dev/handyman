package in.handyman.raven.lambda.doa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.exception.HandymanException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractAudit extends Auditable {

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
    }

    private String lambdaName;
    private String pipelineName;
    private String processName;
    private Long rootPipelineId;

    private Long parentPipelineId = -1L;
    private String parentPipelineName = "";
    private Long parentActionId = -1L;
    private String parentActionName = "";

    private Integer executionStatusId;

    private Long processId;


    private Map<String, String> context = new HashMap<>();
    @JsonIgnore
    private String contextNode;

}
