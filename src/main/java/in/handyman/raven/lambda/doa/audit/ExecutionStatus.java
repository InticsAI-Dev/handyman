package in.handyman.raven.lambda.doa.audit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum ExecutionStatus {

    STARTED(1), RUNNING(2), STOPPED(3),
    FAILED(4), COMPLETED(5), STAGED(6),
    WARNING(7), WAITING_FOR_ASYNC(8);

    private final Integer id;

    public static ExecutionStatus get(final Integer id) {
        return Stream.of(ExecutionStatus.values()).filter(status -> Objects.equals(status.id, id))
                .findFirst().orElse(null);
    }

}
