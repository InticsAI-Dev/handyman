package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CloudDownload;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;
@Slf4j
public class CloudDownloadActionTest {
@Test
    public void cloudTest() throws Exception {

        CloudDownload cloudDownload= CloudDownload.builder()
                .name("Cloud Download")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .resultTable("")
                .querySet("SELECT '' as bucketName, '' as objectKey")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("tenant_id","1"),
                Map.entry("rootPipelineId","1")));

    CloudDownloadAction cloudDownloadAction=new CloudDownloadAction(actionExecutionAudit,log,cloudDownload);
    cloudDownloadAction.execute();


    }

}

