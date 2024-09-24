package in.handyman.raven.lib.model.face.detection;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FaceDetectionExtractionRequest {
    private String inputFilePath;
}
