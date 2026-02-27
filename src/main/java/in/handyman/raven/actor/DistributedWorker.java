package in.handyman.raven.actor;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.audit.ExecutionStatus;
import in.handyman.raven.lambda.process.LambdaEngine;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class DistributedWorker {

    private final String workerId =
            UUID.randomUUID().toString();

    private final ExecutorService pool =
            Executors.newFixedThreadPool(8);

    public void start() throws InterruptedException {

        startSupervisor();

        while (true) {

            ActionExecutionAudit audit =
                    HandymanActorSystemAccess
                            .lease(workerId);

            if (audit != null) {

                pool.submit(() ->
                        execute(audit));
            } else {
                sleep(200);
            }
        }
    }
    private void execute(ActionExecutionAudit audit) {

        try {

            audit.updateExecutionStatusId(
                    ExecutionStatus.RUNNING.getId());

            HandymanActorSystemAccess.update(audit);

            LambdaEngine.executeDistributed(audit);

            audit.updateExecutionStatusId(
                    ExecutionStatus.COMPLETED.getId());

        } catch (Exception e) {

            audit.updateExecutionStatusId(
                    ExecutionStatus.FAILED.getId());
        }
        finally {

            HandymanActorSystemAccess.update(audit);
        }
    }
    private void heartbeat(ActionExecutionAudit audit){

        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(() -> {

                    HandymanActorSystemAccess
                            .extendLease(
                                    audit.getActionId(),
                                    workerId);

                },10,10, TimeUnit.SECONDS);
    }

    private void startSupervisor(){

        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(() -> {

                    HandymanActorSystemAccess
                            .recoverLeases();

                    HandymanActorSystemAccess
                            .retryFailed();

                },30,30,TimeUnit.SECONDS);
    }

}
