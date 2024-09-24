package in.handyman.raven.lib.model.hwdectection.copro;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HwDetectionDataItemCopro {

    @JsonProperty("document_status")
    private String documentStatus;

    @JsonProperty("confidence_score")

    private Float confidenceScore;
}
