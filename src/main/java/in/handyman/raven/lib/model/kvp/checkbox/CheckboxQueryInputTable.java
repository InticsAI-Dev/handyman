package in.handyman.raven.lib.model.kvp.checkbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckboxQueryInputTable {
    private String response;
    private Integer paperNo;
    private String originId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private String extractedImageUnit;
    private Long imageDpi;
    private Long imageWidth;
    private Long imageHeight;
    private Timestamp createdOn;
    private String process;
    private String sorMetaDetail;
    private Long sorContainerId;
    private String sorItemLabel;
    private String sorItemName;
    private String checkboxKeywords;
}
