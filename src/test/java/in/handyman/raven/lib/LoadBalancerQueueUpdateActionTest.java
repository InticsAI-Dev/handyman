package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.LoadBalancerQueueUpdate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class LoadBalancerQueueUpdateActionTest {

    @Test
    void execute() throws Exception {

        LoadBalancerQueueUpdate loadBalancerQueueUpdate = LoadBalancerQueueUpdate.builder()
                .name("load balancer queue update")
                .condition(true)
                .resourceConn("intics_agadia_db_conn")
                .ipAddress("http://localhost")
                .port("50005")
                .querySet("SELECT id, tenant_id, group_id, transaction_id, load_balancer_batch_id, endpoint_id, completed_files, failed_files, user_name\n" +
                        "FROM outbound.load_balancer_queue_update_info;")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size","5"),
                Map.entry("gen_group_id.group_id","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("write.batch.size","5")));

        LoadBalancerQueueUpdateAction loadBalancerQueueUpdateAction=new LoadBalancerQueueUpdateAction(actionExecutionAudit,log, loadBalancerQueueUpdate);
        loadBalancerQueueUpdateAction.execute();

    }
}