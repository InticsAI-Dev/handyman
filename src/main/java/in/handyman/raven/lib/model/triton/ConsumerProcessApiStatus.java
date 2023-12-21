package in.handyman.raven.lib.model.triton;


import in.handyman.raven.lambda.doa.audit.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum ConsumerProcessApiStatus {

    COMPLETED("COMPLETED"),FAILED("FAILED");
    private final String statusDescription;

}
