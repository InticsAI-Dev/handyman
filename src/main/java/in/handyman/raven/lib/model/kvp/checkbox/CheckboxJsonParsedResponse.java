package in.handyman.raven.lib.model.kvp.checkbox;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckboxJsonParsedResponse {

    @JsonProperty("chbq_grps")
    private List<CheckboxGroup> groups;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CheckboxGroup {
        private String grid;
        @JsonProperty("sec_hdr")
        private String sectionHeader;
        @JsonProperty("qns_txt")
        private String questionText;
        @JsonProperty("grp_bbox")
        private List<Integer> groupBbox;
        private List<CheckboxOption> opts;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CheckboxOption {
        @JsonProperty("l")
        private String label;
        @JsonProperty("s")
        private String status;
    }
}
