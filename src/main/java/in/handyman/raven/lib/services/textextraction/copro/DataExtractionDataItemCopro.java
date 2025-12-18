package in.handyman.raven.lib.services.textextraction.copro;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataExtractionDataItemCopro {
    private String pageContent;
    private String filePath;
    private String fileName;
}
