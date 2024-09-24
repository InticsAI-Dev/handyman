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
    private String originId;
    private Integer paperNumber;
    private String inputFilePath;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private String templateName;
    private Long rootPipelineId;

    }


