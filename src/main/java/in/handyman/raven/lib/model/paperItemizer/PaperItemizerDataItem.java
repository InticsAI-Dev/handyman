package in.handyman.raven.lib.model.paperItemizer;

import jakarta.json.Json;
import lombok.*;

import java.util.List;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaperItemizerDataItem {
        private List<String> itemizedPapers;


        }



