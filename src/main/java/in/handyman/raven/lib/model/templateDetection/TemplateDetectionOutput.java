package in.handyman.raven.lib.model.templateDetection;

import in.handyman.raven.lib.model.NerAdaptors.NerAdapterDataItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TemplateDetectionOutput {

        private String name;
        private String datatype;
        private List<Integer> shape;
        private List<String> data;
    }

