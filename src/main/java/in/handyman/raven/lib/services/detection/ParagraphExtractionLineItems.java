package in.handyman.raven.lib.services.detection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParagraphExtractionLineItems {
    private String sectionHeader;
    private String prompt;
}
