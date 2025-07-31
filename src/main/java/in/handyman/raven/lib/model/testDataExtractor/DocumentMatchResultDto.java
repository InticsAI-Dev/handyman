package in.handyman.raven.lib.model.testDataExtractor;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DocumentMatchResultDto {
    private String fileName;
    private int totalPages;
    private List<PageMatchGroupDto> paperNoMatches;
    private String message;
}
