package in.handyman.raven.lib.replicate;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ReplicateResponse {

    private String id;
    private String model;
    private String version;
    private JsonNode input;
    private String logs;
    private JsonNode output;
    @JsonProperty("data_removed")
    private Boolean dataRemoved;
    private String error;
    private String status;
    @JsonProperty("created_at")
    private String createdAt;
    private JsonNode urls;
    @JsonProperty("statusCode")
    private Long statusCode;
    @JsonProperty("errorMessage")
    private String errorMessage;
    @JsonProperty("detail")
    private String detail;
}