package in.handyman.raven.lib.model.tableextraction.coproprocessor;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//2. output pojo for table, which implements CoproProcessor.Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableExtractionOutputTable implements CoproProcessor.Entity {

    private String originId;
    private Long tenantId;
    private String tableGroupId;
    private Integer pageNumber;
    private String markdownTable;
    private String status;
    private String modelName;
    private String errorMessage;
    private Double durationTime;
    private String batchId;
    private String processId;
    private Timestamp createdOn;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.tenantId, this.tableGroupId, this.pageNumber,
                this.markdownTable, this.status, this.modelName, this.errorMessage,
                this.durationTime, this.batchId, this.processId, this.createdOn).collect(Collectors.toList());
    }
}
