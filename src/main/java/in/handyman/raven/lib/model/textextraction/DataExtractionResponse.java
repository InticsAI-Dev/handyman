package in.handyman.raven.lib.model.textextraction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataExtractionResponse {
    @JsonProperty("model_name")
    private String modelName;
    @JsonProperty("model_version")
    private String modelVersion;
    @JsonProperty("statusCode")
    private Long statusCode;
    @JsonProperty("errorMessage")
    private String errorMessage;
    @JsonProperty("detail")
    private String detail;
    private List<DataExtractionOutput> outputs;


    }

