package in.handyman.raven.lib.model.triton;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public enum ConsumerProcessApiStatus {

    COMPLETED("COMPLETED"),FAILED("FAILED");
    private final String statusDescription;

}
