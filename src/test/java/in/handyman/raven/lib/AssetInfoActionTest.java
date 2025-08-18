package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AssetInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class AssetInfoActionTest {

    @Test
    void testingAssetInfo() throws Exception {
        AssetInfo assetInfo= AssetInfo.builder()
                .name("info")
                .resourceConn("intics_zio_db_conn")
                .assetTable("macro.file_details_truth_audit")
                .auditTable("info.sanitizer_summary_audit")
                .values("SELECT a.file_path, b.batch_id\n" +
                        "FROM info.data_extraction a\n" +
                        "join preprocess.preprocess_payload_queue_archive b on a.origin_id=b.origin_id and a.tenant_id=b.tenant_id\n" +
                        "where a.status='COMPLETED'\n" +
                        "and a.is_blank_page='no' and a.group_id='55' and a.tenant_id = 1 and b.batch_id ='BATCH-55_0';\n")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.setRootPipelineId(1L);
        action.getContext().put("asset.info.consumer.API.count","10");
        action.getContext().put("read.batch.size","100");
        action.getContext().put("write.batch.size","100");
        action.getContext().put("tenant_id","1");
        action.getContext().put("copro.processor.thread.creator","FIXED_THREAD");
        action.getContext().put("pipeline.text.extraction.encryption", "false");
        AssetInfoAction assetInfoAction=new AssetInfoAction(action, log, assetInfo);
        assetInfoAction.execute();
    }

}