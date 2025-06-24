package in.handyman.raven.lib;

import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//1. input pojo from select query, which implements CoproProcessor.Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlankPageRemoverInputTable implements CoproProcessor.Entity {
    private String originId;
    private Integer groupId;
    private String inboundId;
    private Long tenantId;
    private String filePath;
    private String outputDir;
    private String documentId;


    @Override
    public List<Object> getRowData() {
        return null;
    }
    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }
}
