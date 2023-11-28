package in.handyman.raven.lib.model.integratedNoiseModel;

import com.fasterxml.jackson.annotation.JsonProperty;
//import in.handyman.raven.lib.model.qrExtraction.QrExtractionOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
public class NoiseModelResponse {


        @JsonProperty("origin_id")
        private String originId;
        @JsonProperty("paper_no")
        private Integer paperNo;
        @JsonProperty("process_id")
        private Integer processId;
        @JsonProperty("group_id")
        private Integer groupId;
        @JsonProperty("tenant_id")
        private Integer tenantId;
        @JsonProperty("input_file_path")
        private String inputFilePath;
        @JsonProperty("consolidated_class")
        private String consolidatedClass;
        @JsonProperty("consolidated_confidence_score")
        private String consolidatedConfidenceScore;
        @JsonProperty("noise_models_result")
        private String noiseModelsResult;







}
