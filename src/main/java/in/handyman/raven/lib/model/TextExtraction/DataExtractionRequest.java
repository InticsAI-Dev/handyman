package in.handyman.raven.lib.model.TextExtraction;

import in.handyman.raven.lib.model.autoRotation.AutoRotationData;
import in.handyman.raven.lib.model.triton.TritonRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DataExtractionRequest {


    private List<TritonRequest> inputs;
}