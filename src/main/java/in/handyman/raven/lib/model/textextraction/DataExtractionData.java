package in.handyman.raven.lib.model.textextraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.ref.PhantomReference;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DataExtractionData {
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String originId;
    private Integer paperNumber;
    private Long processId;
    private String inputFilePath;
    private Integer groupId;
    private Long tenantId;
    private String templateName;
    private String batchId;

    }

