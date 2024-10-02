package in.handyman.raven.lib.model.trinitymodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrinityModelLineItem {

    private String filePath;
    private List<TrinityInputAttribute> attributes;
    private String paperType;
    private String modelRegistry;
    private Long tenantId;
    private String originId;
    private Long groupId;
    private Long paperNo;
    private String qnCategory;
    private Long rootPipelineId;
    private Long processId;
    private Long modelRegistryId;
    private String batchId;


}
