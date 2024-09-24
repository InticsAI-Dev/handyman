package in.handyman.raven.util;

import com.fasterxml.uuid.Generators;

import java.util.concurrent.atomic.AtomicInteger;

public class UniqueID {
    private UniqueID() {
    }

    public static Long getId() {
        var uuid = Generators.timeBasedGenerator().generate();
        return uuid.timestamp();
    }


}


