package in.handyman.raven.lib.elv.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerEntityDetailsDto {
    private String sorContainerName;
    private List<String> truthEntityNames; // Storing JSON array as a List

}
