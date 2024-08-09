package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadonKvpLineItem {
    private String model;
    private Map<String, Object> inferOutput;
    private String originId;
    private Integer paperNo;
    private Long processId;
    private Long groupId;
    private Long tenantId;
    private String inputFilePath;
}
