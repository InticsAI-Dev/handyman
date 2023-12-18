package in.handyman.raven.lib.model.paperitemizer;

import lombok.*;

import java.util.List;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaperItemizerDataItem {
        private Integer paperNumber;
        private List<String> itemizedPapers;
        private String originId;
        private Long processId;
        private Integer groupId;
        private Long tenantId;
        }



