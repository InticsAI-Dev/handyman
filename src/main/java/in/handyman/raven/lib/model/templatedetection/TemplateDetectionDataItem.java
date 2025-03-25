package in.handyman.raven.lib.model.templatedetection;

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
public class TemplateDetectionDataItem {
    private List<TemplateDetectionArgonAttributes> attributes;
    private Integer imageDPI;
    private Integer imageWidth;
    private Integer imageHeight;
    private String extractedImageUnit;
    @JsonIgnore
    private String inputFilePath;
    private String qnCategory;
    private String modelRegistry;
    private String paperType;
    private String originId;
    private Long processId;
    private Integer paperNo;
    private Integer groupId;
    private Long tenantId;
    private String batchId;
    private Long rootPipelineId;
    private Long modelRegistryId;
}