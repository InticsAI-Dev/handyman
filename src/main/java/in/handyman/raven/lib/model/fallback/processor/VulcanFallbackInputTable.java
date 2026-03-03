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
public class VulcanFallbackInputTable implements CoproProcessor.Entity {
    private String originId;
    private String sorItemName;
    private String sorQuestion;
    private String documentType;
    private String sorContainer;
    private Long tenantId;
    private String allowedAdapter;
    private String restrictedAdapter;
    private String description;

    @Override
    public List<Object> getRowData() {
        return null; // Not used for producing to DB directly in this flow
    }

    @Override
    public String getStatus() {
        return null;
    }
}
