package in.handyman.raven.lib.model.qrExtraction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QrReader {
    private String value;
    private String type;
    @JsonProperty("decode_type")
    private String decodeType;
    private Integer confidenceScore;
    private QrExtractionBoundingBox boundingBox;
    private Integer angle;
}
