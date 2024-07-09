package in.handyman.raven.lib.model.face.detection;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FaceDetectionExtractionResponse {
    private boolean success;
    private List<FaceDetectionExtractionPayload> payload;
}
