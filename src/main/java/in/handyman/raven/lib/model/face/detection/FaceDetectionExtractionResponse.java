package in.handyman.raven.lib.model.face.detection;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FaceDetectionExtractionResponse {
    private String predictedValue;
    private Long precision;
    private Long leftPos;
    private Long upperPos;
    private Long rightPos;
    private Long lowerPos;
    private String encode;
}
