package in.handyman.raven.lib.model.trinityModel;

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
public class TrinityModelResultLineItem {

        private List<TrinityModelResult> attributes;
        private double imageDPI;
        private double imageWidth;
        private double imageHeight;
        private String extractedImageUnit;
    }


