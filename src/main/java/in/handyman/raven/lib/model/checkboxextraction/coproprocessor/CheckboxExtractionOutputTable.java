package in.handyman.raven.lib.model.checkboxextraction.coproprocessor;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//2. output pojo for checkbox extraction, which implements CoproProcessor.Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckboxExtractionOutputTable implements CoproProcessor.Entity {

    private String originId;
    private Long groupId;
    private Integer rootPipelineId;
    private Long tenantId;
    private Integer pageNumber;
    private String checkboxData; // JSON string containing checkbox extraction results
    private String status;
    private String modelName;
    private String errorMessage;
    private Double durationTime;
    private String batchId;
    private String processId;
    private Timestamp createdOn;
    private String stage;

    @Override
    public List<Object> getRowData() {
        return Stream.of(
                defaultString(this.originId),
                defaultLong(this.groupId),
                defaultInt(this.rootPipelineId),
                defaultLong(this.tenantId),
                defaultInt(this.pageNumber),
                defaultString(this.checkboxData),
                defaultString(this.stage),
                defaultString(this.status),
                defaultString(this.modelName),
                defaultString(this.errorMessage),
                defaultDouble(this.durationTime),
                defaultString(this.batchId),
                defaultString(this.processId),
                this.createdOn != null ? this.createdOn : new Timestamp(System.currentTimeMillis()))
                .collect(Collectors.toList());
    }

    private static String defaultString(String value) {
        return value != null ? value : "";
    }

    private static Integer defaultInt(Integer value) {
        return value != null ? value : 0;
    }

    private static Long defaultLong(Long value) {
        return value != null ? value : 0L;
    }

    private static Double defaultDouble(Double value) {
        return value != null ? value : 0.0;
    }
}
