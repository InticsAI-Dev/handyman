package in.handyman.raven.lib.model.paragraph.detection;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParagraphExtractionResponse {
    private String sectionHeader;
    private List<String> sectionPoints;
}
