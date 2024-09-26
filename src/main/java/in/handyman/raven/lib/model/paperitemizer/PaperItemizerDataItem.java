package in.handyman.raven.lib.model.paperitemizer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaperItemizerDataItem {
        @JsonProperty("itemizedPath")
        private String itemizedPapers;
        private Long paperNumber;
        private String originId;
        private Long processId;
        private Integer groupId;
        private Long tenantId;
        private String batchId;
}



