package in.handyman.raven.lib.model.fallback.processor;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VulcanFallbackOutputTable implements CoproProcessor.Entity {
    private String originId;
    private String sorItemName;
    private String status;
    private String message;

    @Override
    public List<Object> getRowData() {
        return List.of(originId, sorItemName, status, message);
    }

    @Override
    public String getStatus() {
        return status;
    }
}
