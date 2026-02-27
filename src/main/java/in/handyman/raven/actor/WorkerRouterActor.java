package in.handyman.raven.actor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerRouterActor implements ActorCore {

    private List<ActorRef> workers;
    private AtomicInteger index = new AtomicInteger();

    @Override
    public void onReceive(Message msg) {

        int i =
                index.getAndIncrement() % workers.size();

        workers.get(i).tell(msg);
    }
}
