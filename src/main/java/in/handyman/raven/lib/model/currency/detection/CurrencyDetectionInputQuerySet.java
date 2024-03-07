package in.handyman.raven.lib.model.currency.detection;

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
public class CurrencyDetectionInputQuerySet implements CoproProcessor.Entity {
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

    @Override
    public List<Object> getRowData() {
        return null;
    }



}

