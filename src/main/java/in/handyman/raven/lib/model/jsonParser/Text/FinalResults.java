package in.handyman.raven.lib.model.jsonParser.Text;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class FinalResults {
    private List<ContentNode> textLines;
}
