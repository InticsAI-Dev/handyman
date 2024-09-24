package in.handyman.raven.lib.model.filemergerpdf;

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
    private String outputFileName;
    private String originId;
    private Long processId;
    private Long groupId;
    private Long tenantId;
}
