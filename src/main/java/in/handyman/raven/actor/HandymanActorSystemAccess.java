package in.handyman.raven.actor;

import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lambda.access.repo.HandymanRepoImpl;
import in.handyman.raven.lambda.doa.audit.*;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.Props;

public class HandymanActorSystemAccess {

    private static final HandymanRepo HANDYMAN_REPO = new HandymanRepoImpl();
    private static final ActorSystem SYSTEM = ActorSystem.create("handyman");
    private static final ActorRef INSERT_ACTOR = SYSTEM.actorOf(Props.create(AuditInsertActor.class), "audit-insert");
    private static final ActorRef UPDATE_ACTOR = SYSTEM.actorOf(Props.create(AuditUpdateActor.class), "audit-update");

    public static void insert(final PipelineExecutionAudit pipelineExecutionAudit) {

        //HANDYMAN_REPO.insertPipeline(pipelineExecutionAudit);
        INSERT_ACTOR.tell(pipelineExecutionAudit, ActorRef.noSender());
    }

    public static void insert(final ActionExecutionAudit actionExecutionAudit) {

        //HANDYMAN_REPO.insertAction(actionExecutionAudit);
        INSERT_ACTOR.tell(actionExecutionAudit, ActorRef.noSender());
    }

    public static void insert(final StatementExecutionAudit statementExecutionAudit) {

        //HANDYMAN_REPO.insertStatement(statementExecutionAudit);
        INSERT_ACTOR.tell(statementExecutionAudit, ActorRef.noSender());
    }

    public static void insert(final PipelineExecutionStatusAudit pipelineExecutionStatusAudit) {

        //HANDYMAN_REPO.save(pipelineExecutionStatusAudit);
        INSERT_ACTOR.tell(pipelineExecutionStatusAudit, ActorRef.noSender());
    }

    public static void insert(final ActionExecutionStatusAudit actionExecutionStatusAudit) {

        //HANDYMAN_REPO.save(actionExecutionStatusAudit);
        INSERT_ACTOR.tell(actionExecutionStatusAudit, ActorRef.noSender());
    }


    public static void update(final PipelineExecutionAudit pipelineExecutionAudit) {

        //HANDYMAN_REPO.update(pipelineExecutionAudit);
        UPDATE_ACTOR.tell(pipelineExecutionAudit, ActorRef.noSender());
    }

    public static void update(final ActionExecutionAudit actionExecutionAudit) {

        //HANDYMAN_REPO.update(actionExecutionAudit);
        UPDATE_ACTOR.tell(actionExecutionAudit, ActorRef.noSender());

    }
}
