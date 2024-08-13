package in.handyman.raven.lib.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Validator {

    private String inputValue;
    private String adapter;
    private int threshold;
    private String allowedSpecialChar;
    private String comparableChar;
}
