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
                .resourceConn("intics_zio_db_conn")
                .assetTable("macro.file_details_truth_138978990615970048")
                .auditTable("info.sanitizer_summary_audit_138978990615970048")
                .values("select '/home/christopher.paulraj@zucisystems.com/Music/4a35b427-36.jpeg' as file_path , '1' as batch_id;")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.setRootPipelineId(11011L);
        action.getContext().put("process-id","1234567");
        AssetInfoAction assetInfoAction=new AssetInfoAction(action ,log,assetInfo);
        assetInfoAction.execute();
    }

}