package in.handyman.raven.lib;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SorItemMappingInputTable {
    private Timestamp createdOn;
    private String createdUserId;
    private Timestamp lastUpdatedOn;
    private String lastUpdatedUserId;
    private String inputFilePath;
    private String inputResponseJson;
    private String totalResponseJson;
    private Integer paperNo;
    private String originId;
    private Long processId;
    private Long actionId;
    private String process;
    private Integer groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private String status;
    private String stage;
    private String message;
    private String category;
    private String request;
    private String answer;
    private String endpoint;
    private String sorItemName;
}
