package in.handyman.raven.lib.model.table.extraction.headers.copro.legacy.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableResponseOutputRoot {
    @JsonProperty("TruthEntity")
    public String truthEntity;
    public String originId;
    public String paperNo;
    public String groupId;
    public String tenantId;
    public String csvTablesPath;
    public String croppedImage;
    public Bboxes bboxes;
    public TableResponse tableResponse;
}
