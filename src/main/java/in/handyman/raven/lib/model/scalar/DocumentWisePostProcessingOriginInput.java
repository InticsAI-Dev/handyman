package in.handyman.raven.lib.model.scalar;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.DocumentWisePostProcessingInput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for origin-based processing input
 * Implements CoproProcessor.Entity for use with CoproProcessor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentWisePostProcessingOriginInput implements CoproProcessor.Entity {
    
    private String originId;
    private List<DocumentWisePostProcessingInput> inputs;
    
    @Override
    public List<Object> getRowData() {
        List<Object> rowData = new ArrayList<>();
        rowData.add(originId);
        rowData.add(inputs != null ? inputs.size() : 0);
        return rowData;
    }
    
    @Override
    public String getStatus() {
        return inputs != null && !inputs.isEmpty() ? "ACTIVE" : "INACTIVE";
    }
}
