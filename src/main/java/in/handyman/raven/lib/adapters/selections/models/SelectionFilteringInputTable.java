package in.handyman.raven.lib.adapters.selections.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SelectionFilteringInputTable {
    private Long id;
    private Timestamp createdOn;
    private String createdUserId;
    private Timestamp lastUpdatedOn;
    private String lastUpdatedUserId;
    private Long tenantId;
    private Long groupId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private Long sorContainerId;
    private String sorContainerName;
    private String sorItemName;
    private String sorItemLabel;
    private String sectionAlias;
    private String answer;
    private String confidence;
    private String bbox;
    private String bboxAsIs;
    private Long paperNo;
    private String originId;
    private String extractedImageUnit;
    private Long imageDpi;
    private Long imageHeight;
    private Long imageWidth;
    private String blacklistedLabels;
    private String blacklistedSections;
    private Boolean isEncrypted;
    private String encryptionPolicy;
    private boolean labelMatching;
    private String labelMatchMessage;
}
