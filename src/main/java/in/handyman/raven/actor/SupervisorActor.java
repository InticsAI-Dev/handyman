package in.handyman.raven.actor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SupervisorActor implements ActorCore {

    private ScheduledExecutorService scheduler;

    @Override
    public void preStart() {

        scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(
                () -> onReceive(new SupervisorTick()),
                5,
                5,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void onReceive(Message message) {

        if (message instanceof SupervisorTick) {

            recoverExpiredLeases();
            retryFailedActions();
        }
    }

    private void recoverExpiredLeases() {

        HandymanActorSystemAccess
                .recoverLeases();
    }

    private void retryFailedActions() {

        HandymanActorSystemAccess
                .retryFailed();
    }

    @Override
    public void postStop() {

        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}