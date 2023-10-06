package in.handyman.raven.lib.model.autoRotation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutoRotationDataItem {
    private String processedFilePaths;

}

