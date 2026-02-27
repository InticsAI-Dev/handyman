package in.handyman.raven.actor;


import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;

public class QueueListenerActor implements ActorCore {

    private final ActorRef workerRouter;

    public QueueListenerActor(final ActorRef workerRouter) {
        this.workerRouter = workerRouter;
    }

    @Override
    public void onReceive(Message msg) {

        ActionExecutionAudit action =
                HandymanActorSystemAccess.lease(
                        Thread.currentThread().getName()
                );

        if (action != null && workerRouter != null) {
            workerRouter.tell(
                    new ExecuteActionMessage(action)
            );
        }
    }
}