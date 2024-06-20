package in.handyman.raven.lib.model.fileMergerPdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileMergerDataItem {
    private String processedFilePath;
}
