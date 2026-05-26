package in.handyman.raven.actor;

import org.apache.pekko.actor.AbstractActor;
import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lambda.access.repo.HandymanRepoImpl;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.audit.PipelineExecutionAudit;

public class AuditUpdateActor extends AbstractActor {

    private final HandymanRepo REPO = new HandymanRepoImpl();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PipelineExecutionAudit.class, REPO::update)
                .match(ActionExecutionAudit.class, REPO::update)
                .build();
    }
}
