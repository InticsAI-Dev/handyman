package in.handyman.raven.outbound.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimpleValueField {
    private String value;
}
