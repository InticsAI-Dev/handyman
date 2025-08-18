package in.handyman.raven.lib.model.qrextraction;

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
public class QrExtractionDataItem {

        @JsonProperty("decode_value")
        private List<QrReader> decodeValue;
        private String originId;
        private Integer groupId;
        private Long tenantId;
        private Long actionId;
        private Long rootPipelineId;


}

