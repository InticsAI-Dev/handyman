package in.handyman.raven.lib.model.trinityModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.lib.TrinityModelAction;
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
public class TrinityModelResultLineItem {

        private List<TrinityModelAction.TrinityModelResult> attributes;
        private double imageDPI;
        private double imageWidth;
        private double imageHeight;
        private String extractedImageUnit;
    }


