package in.handyman.raven.lib.model.paragraph.detection;

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
