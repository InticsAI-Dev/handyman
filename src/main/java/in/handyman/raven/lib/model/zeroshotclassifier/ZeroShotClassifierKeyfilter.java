package in.handyman.raven.lib.model.zeroshotclassifier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZeroShotClassifierKeyfilter {

        private Map<String,List<String>> keysToFilter;


}
