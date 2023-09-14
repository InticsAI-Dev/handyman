package in.handyman.raven.lib.model.pharseMatch;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhraseMatchOutputTable implements CoproProcessor.Entity {
    private String originId;
    private Integer paperNo;
    private String groupId;
    private String isKeyPresent;
    private String entity;
    private String truthEntity;

    private String status;

    private String stage;

    private String message;
    private Long rootPipelineId;


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.groupId, this.paperNo, this.truthEntity, this.entity,
                this.isKeyPresent, this.status, this.stage, this.message,this.rootPipelineId).collect(Collectors.toList());
    }
}


