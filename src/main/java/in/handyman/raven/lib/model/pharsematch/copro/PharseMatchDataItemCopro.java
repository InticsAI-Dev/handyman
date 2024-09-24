package in.handyman.raven.lib.model.pharsematch.copro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PharseMatchDataItemCopro {
    private String originId;
    private String groupId;
    private String isKeyPresent;
    private Integer paperNo;
    private String truthEntity;
    private String entity;
}

