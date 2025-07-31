package in.handyman.raven.lib.model.testDataExtractor;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KeywordMatchDto {
    private String originalKeyword;
    private String extractionFromDocument;
}
