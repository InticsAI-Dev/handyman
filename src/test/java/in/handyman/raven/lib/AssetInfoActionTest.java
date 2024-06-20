package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AssetInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class AssetInfoActionTest {

    @Test
    void testingAssetInfo() throws Exception {
        AssetInfo assetInfo= AssetInfo.builder()
                .name("info")
                .resourceConn("intics_agadia_db_conn")
                .assetTable("macro.file_details_truth_138978990615970048")
                .auditTable("info.sanitizer_summary_audit_138978990615970048")
                .values("select '/home/anandh.andrews@zucisystems.com/W-space/pr1-lambdas/agadia-gatekeeper/test.jpg' as file_path;")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.setRootPipelineId(11011L);
        action.getContext().put("process-id","1234567");
        AssetInfoAction assetInfoAction=new AssetInfoAction(action ,log,assetInfo);
        assetInfoAction.execute();
    }

}