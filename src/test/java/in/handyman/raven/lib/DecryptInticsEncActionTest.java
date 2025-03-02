package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DecryptInticsEnc;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class DecryptInticsEncActionTest {

    @Test
    void execute() throws Exception {
        DecryptInticsEnc decryptInticsEnc=new DecryptInticsEnc();

        decryptInticsEnc.setName("decryption and convert to csv");
        decryptInticsEnc.setCondition(true);
        decryptInticsEnc.setEndpoint("http://localhost:8190/vulcan/api/encryption/decrypt");
        decryptInticsEnc.setSource("intics_zio_db_conn");
        decryptInticsEnc.setForkBatchSize("10");
        decryptInticsEnc.setQuerySet("select cr.root_pipeline_id ,cr.group_id ,cr.tenant_id ,sc.sor_container_name ,a.file_name ,cr.origin_id ,cr.paper_no , \n" +
                "jsonb_agg(jsonb_build_object('key',si.sor_item_name,'value',cr.extracted_value ,'policy',ep.encryption_policy,'isEncrypted',si.is_encrypted))::varchar as extracted_meta_detail\n" +
                "from voting.cummulative_result cr\n" +
                "join info.source_of_origin soo on soo.tenant_id =cr.tenant_id and soo.origin_id =cr.origin_id \n" +
                "join info.asset a on a.asset_id =soo.asset_id and a.tenant_id =soo.tenant_id \n" +
                "join sor_meta.asset_info ai on ai.tenant_id =cr.tenant_id \n" +
                "join sor_meta.sor_container sc on sc.tenant_id =cr.tenant_id and sc.document_type=ai.document_type \n" +
                "join sor_meta.sor_item si on si.sor_container_id =sc.sor_container_id and si.sor_item_name=cr.sor_item_name and si.tenant_id =cr.tenant_id \n" +
                "join sor_meta.encryption_policies ep on ep.encryption_policy_id =si.encryption_policy_id \n" +
                "where cr.root_pipeline_id in (2776) \n" +
                "and cr.tenant_id =1\n" +
                "and ai.document_type ='HEALTH_CARE' and ai.template_name ='MULTIVERSE'\n" +
                "group by cr.root_pipeline_id ,cr.group_id ,cr.tenant_id ,sc.sor_container_name,a.file_name ,cr.origin_id ,cr.paper_no;");
        decryptInticsEnc.setOutputPath("/home/anandh.andrews@zucisystems.com/intics-workspace/Asgard/ANTHEM-docs/AUMI/build/testing/cs-upload-alchemy/outputs/1.csv");

        ActionExecutionAudit action=new ActionExecutionAudit();
        action.setActionId(1234L);
        action.setRootPipelineId(2134L);
        action.getContext().put("inbound.rootpipelineid.list","");
        action.getContext().put("tenant_id","");
        action.getContext().put("document_type","");
        DecryptInticsEncAction decryptInticsEncAction=new DecryptInticsEncAction(action,log,decryptInticsEnc);
        decryptInticsEncAction.execute();
    }

    @Test
    void executeIf() {
    }
}