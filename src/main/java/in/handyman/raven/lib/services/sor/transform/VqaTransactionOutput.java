package in.handyman.raven.lib.services.sor.transform;


import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@RegisterBeanMapper(VqaTransactionOutput.class)
public class VqaTransactionOutput extends VqaTransactionBase  {
    private Long vqaId;
    private Long sorContainerId;
    private String sorContainerName;
    private String sorContainerInstance;
    private String sorItemName;
    private Long sorItemId;
    private Integer sorItemAttributionId;
    private Long modelId;
    private String modelInfo;
    private String modelRegistry;
    private Long modelRegistryId;
    private String answer;
    private Double vqaScore;
    private Long score;
    private String bBox;
    private String label;
    private String sectionAlias;
    private Long synonymId;
    private String sorSynonym;
    private Long questionId;
    private String sorQuestion;
    private Integer weight;
    private String category;
    private String lineItemType;
    private String isMultiEntityEnabled;
    private String encryptionPolicy;
    private boolean isEncrypted;


    private String buildLoggerOutput() {
        return this.buildLoggerBaseInput().concat( " | Container name " + this.sorContainerName).concat( " | Container instance name " + this.sorContainerInstance).concat(" | Sor Item Name " + this.sorItemName);
    }
}
