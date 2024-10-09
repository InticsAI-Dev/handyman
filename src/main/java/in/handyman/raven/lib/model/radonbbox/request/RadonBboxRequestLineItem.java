package in.handyman.raven.lib.model.radonbbox.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadonBboxRequestLineItem {

    private String sorItemName;
    private String answer;
    private String valueType;
}
