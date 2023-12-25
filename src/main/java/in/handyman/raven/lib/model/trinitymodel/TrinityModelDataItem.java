package in.handyman.raven.lib.model.trinitymodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrinityModelDataItem {
    private List<TrinityModelDataLineItem> attributes;
    private Integer imageDPI;
    private Integer imageWidth;
    private Integer imageHeight;
    private String extractedImageUnit;
    @JsonIgnore
    private String inputFilePath;

}
