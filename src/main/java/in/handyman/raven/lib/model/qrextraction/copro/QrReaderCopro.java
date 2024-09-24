package in.handyman.raven.lib.model.qrextraction.copro;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.handyman.raven.lib.model.qrextraction.QrExtractionBoundingBox;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QrReaderCopro {
    private String value;
    private String type;
    private Double confidenceScore;
    private QrExtractionBoundingBox boundingBox;
    private Integer angle;
    @JsonProperty("decode_type")
    private String decodeType;
}
