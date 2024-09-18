package in.handyman.raven.lib.model.common;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class CreateTimeStamp {

    private CreateTimeStamp() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static Timestamp currentTimestamp() {
        return Timestamp.valueOf(LocalDateTime.now());
    }
}
