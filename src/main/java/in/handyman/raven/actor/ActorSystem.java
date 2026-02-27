package in.handyman.raven.actor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActorSystem {

    private final ExecutorService executor =
            Executors.newCachedThreadPool();

    public ActorRef actorOf(ActorCore actorCore) {

        ActorCell cell = new ActorCell(actorCore);

        executor.submit(cell);

        return cell.ref();
    }

    public void shutdown() {
        executor.shutdown();
    }
}
