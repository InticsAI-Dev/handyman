package in.handyman.raven.lib.model.visualextraction.coproprocessor;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.postgresql.util.PGobject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VisualExtractionOutputTable implements CoproProcessor.Entity {
    private String originId;
    private Long groupId;
    private Long rootPipelineId;
    private Long tenantId;
    private Long paperNo;
    private String documentType;
    private String stage;
    private String status;
    private String modelName;
    private String errorMessage;
    private Double durationTime;
    private String batchId;
    private Long processId;
    private String response;
    private String request;
    private String endpoint;

    private static PGobject toJsonb(String json) {
        if (json == null) return null;
        try {
            PGobject pgo = new PGobject();
            pgo.setType("jsonb");
            pgo.setValue(json);
            return pgo;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Object> getRowData() {
        return Stream.of(originId, groupId, rootPipelineId, tenantId, paperNo, documentType, stage, status, modelName,
                errorMessage, durationTime, batchId, processId, toJsonb(response), toJsonb(request), endpoint)
                .collect(Collectors.toList());
    }
}
