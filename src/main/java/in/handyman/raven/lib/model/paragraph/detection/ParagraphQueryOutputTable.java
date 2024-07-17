package in.handyman.raven.lib.model.paragraph.detection;

import com.fasterxml.jackson.databind.JsonNode;
import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.net.ntp.TimeStamp;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParagraphQueryOutputTable  implements CoproProcessor.Entity{

    private String originId;
    private Integer paperNo;
    private Integer groupId;
    private String filePath;
    private Long tenantId;
    private String templateId;
    private Long processId;
    private String outputDir;
    private Long rootPipelineId;
    private String process;
    private String status;
    private String stage;
    private String message;
    private String modelName;
    private String modelVersion;
    private Timestamp createdOn;
    private Integer synonymId;
    private String paragraphSection;
    private String paragraphPoints;


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.paperNo, this.groupId, this.filePath, this.tenantId,
                this.processId, this.outputDir, this.rootPipelineId, this.process, this.status,
                this.stage, this.message,this.modelName, this.modelVersion,this.synonymId,
                this.paragraphSection,this.paragraphPoints).collect(Collectors.toList());


    }



}
