package in.handyman.raven.lib.model.textextraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataExtractionDataItem {


    private String pageContent;
    private String filePath;
    private String fileName;

    }


