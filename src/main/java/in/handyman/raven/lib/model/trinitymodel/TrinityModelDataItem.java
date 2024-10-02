package in.handyman.raven.lib.model.trinitymodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrinityModelDataItem {
    private List<TrinityModelDataLineItem> attributes;
    private String inputFilePath;
    private Integer imageDPI;
    private Integer imageWidth;
    private Integer imageHeight;
    private String extractedImageUnit;
    private String qnCategory;
    private String batchId;
    private String modelRegistry;
    private String paperType;
    private String originId;
    private Long processId;
    private Long paperNo;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private Long modelRegistryId;
}
