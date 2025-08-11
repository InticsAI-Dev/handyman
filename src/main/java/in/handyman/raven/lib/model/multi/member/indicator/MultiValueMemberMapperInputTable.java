package in.handyman.raven.lib.model.multi.member.indicator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MultiValueMemberMapperInputTable {

    private Long rootPipelineId;
    private String batchId;
    private Long groupId;
    private String originId;
    private String sorContainerName;
    private String sorItemName;
    private String predictedValue;
    private Long paperNo;
    private Long tenantId;
    private String encryptionPolicy;
    private String isEncrypted;
    private String lineItemType;
    private Long synonymId;
    private Long questionId;
    private Long frequency;
    private double vqaScore;
    private String bBox;
    private String modelRegistry;
    private Long scoreId;
    private Long cummulativeScore;
    private Long weightScore;
}