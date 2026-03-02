package in.handyman.raven.lib.custom.outbound.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimpleValueField {
    private String value;
}
