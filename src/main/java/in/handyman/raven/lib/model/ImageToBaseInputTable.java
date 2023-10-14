package in.handyman.raven.lib.model;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder


public class ImageToBaseInputTable implements CoproProcessor.Entity {


    private String originId;
    private Long paperNo;
    private Integer groupId;
    private String processedFilePath;
    private Long tenantId;
public List<Object> getRowData() {
            return null;
        }



}
