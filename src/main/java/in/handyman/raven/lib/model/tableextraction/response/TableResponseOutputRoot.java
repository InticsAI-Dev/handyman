package in.handyman.raven.lib.model.tableextraction.response;

import com.fasterxml.jackson.annotation.JsonProperty;

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
