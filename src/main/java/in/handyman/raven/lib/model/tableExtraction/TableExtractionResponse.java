package in.handyman.raven.lib.model.tableExtraction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;


import java.util.List;
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableExtractionResponse{

    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("model_version")
    private String modelVersion;

    private List<TableExtractionOutput> outputs;
}
