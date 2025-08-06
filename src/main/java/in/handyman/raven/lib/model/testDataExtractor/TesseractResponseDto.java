package in.handyman.raven.lib.model.testDataExtractor;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class TesseractResponseDto {
    private Map<String, String> textByPage;
}