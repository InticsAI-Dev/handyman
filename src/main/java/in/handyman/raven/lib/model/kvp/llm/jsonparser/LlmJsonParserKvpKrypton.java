package in.handyman.raven.lib.model.kvp.llm.jsonparser;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LlmJsonParserKvpKrypton {

    private String key;
    private String value;
    private String label;
    @JsonProperty("section_alias")
    private String sectionAlias;
    private double confidence;
    private JsonNode boundingBox ;
}
