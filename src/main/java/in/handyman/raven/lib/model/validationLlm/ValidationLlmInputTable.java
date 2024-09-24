package in.handyman.raven.lib.model.validationLlm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ValidationLlmInputTable {
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String input;
    private String outputDir;
    private String task;
    private String sectionHeader;
    private String prompt;
    private String originId;
    private Integer paperNo;
    private Integer groupId;
    private Integer processId;
    private Integer sorItemId;
    private String sorItemName;
    private String sorQuestion;
    private Integer questionId;
    private Integer synonymId;
    private String answer;
    private Float vqaScore;
    private Integer weight;
    private Integer createdUserId;
    private Timestamp createdOn;
    private Long tenantId;
    private String validationName;
    private String bBox;
    private String modelRegistry;
    private String text;
}
