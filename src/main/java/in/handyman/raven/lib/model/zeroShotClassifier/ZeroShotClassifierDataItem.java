package in.handyman.raven.lib.model.zeroShotClassifier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZeroShotClassifierDataItem {
    private String originId;
    private String groupId;
    private String isKeyPresent;
    private Integer paperNo;
    private String truthEntity;
    private String entity;

}
