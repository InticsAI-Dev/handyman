package in.handyman.raven.lib.model.testDataExtractor;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageMatchGroupDto {
    private int paperNo;
    private List<KeywordMatchDto> matches;
}
