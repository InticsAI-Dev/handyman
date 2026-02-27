package in.handyman.raven.actor;

import in.handyman.raven.lambda.process.LambdaEngine;

public class WorkerActor implements ActorCore {

    @Override
    public void onReceive(Message message) {

        if (message instanceof ExecuteActionMessage) {

            ExecuteActionMessage m =
                    (ExecuteActionMessage) message;

            LambdaEngine.executeDistributed(
                    m.getAudit()
            );
        }
    }
}
