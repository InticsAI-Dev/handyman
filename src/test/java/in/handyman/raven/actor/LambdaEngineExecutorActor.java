package in.handyman.raven.actor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class LambdaEngineExecutorActor {

    @Test
    public void startWorker() throws InterruptedException {
        final DistributedWorker worker =
                new DistributedWorker();
        log.info("Starting Distributed Worker AFTER Spring Ready");

        worker.start();
    }
}
