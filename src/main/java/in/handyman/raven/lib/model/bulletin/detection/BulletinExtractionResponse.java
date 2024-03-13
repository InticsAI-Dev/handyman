package in.handyman.raven.lib.model.bulletin.detection;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulletinExtractionResponse {
    private String bulletinHeader;
    private List<String> bulletinPoints;
}
