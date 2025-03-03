package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DecryptInticsEnc;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class DecryptInticsEncActionTest {

    @Test
    void execute() throws Exception {
        DecryptInticsEnc decryptInticsEnc = new DecryptInticsEnc();

        decryptInticsEnc.setName("decryption and convert to csv");
        decryptInticsEnc.setCondition(true);
        decryptInticsEnc.setEndpoint("http://localhost:8190/vulcan/api/encryption/decrypt");
        decryptInticsEnc.setSource("intics_zio_db_conn_azure");
        decryptInticsEnc.setForkBatchSize("10");
        decryptInticsEnc.setQuerySet("select cr.root_pipeline_id ,cr.group_id,idfd.document_id ,cr.tenant_id ,sc.sor_container_name ,a.file_name ,cr.origin_id ,cr.paper_no , \n" +
                "jsonb_agg(jsonb_build_object('key',si.sor_item_name,'value',cr.extracted_value ,'policy',ep.encryption_policy,'isEncrypted',si.is_encrypted))::varchar as extracted_meta_detail\n" +
                "from voting.cummulative_result cr\n" +
                "join info.source_of_origin soo on soo.tenant_id =cr.tenant_id and soo.origin_id =cr.origin_id \n" +
                "join info.asset a on a.asset_id =soo.asset_id and a.tenant_id =soo.tenant_id \n" +
                "join sor_meta.asset_info ai on ai.tenant_id =cr.tenant_id \n" +
                "join sor_meta.sor_container sc on sc.tenant_id =cr.tenant_id and sc.document_type=ai.document_type \n" +
                "join sor_meta.sor_item si on si.sor_container_id =sc.sor_container_id and si.sor_item_name=cr.sor_item_name and si.tenant_id =cr.tenant_id \n" +
                "join sor_meta.encryption_policies ep on ep.encryption_policy_id =si.encryption_policy_id \n" +
                "join inbound_config.ingestion_downloaded_file_details idfd on idfd.transaction_id =soo.transaction_id and idfd.tenant_id =soo.tenant_id \n" +
                "join inbound_config.ingestion_file_details ifd on ifd.inbound_transaction_id =idfd.inbound_transaction_id \n" +
                "where cr.root_pipeline_id in (19105,19225) \n" +
                "and cr.tenant_id =1\n" +
                "and ai.document_type ='HEALTH_CARE' and ai.template_name ='MULTIVERSE'\n" +
                "group by cr.root_pipeline_id ,cr.group_id ,cr.tenant_id ,idfd.document_id,sc.sor_container_name,a.file_name ,cr.origin_id ,cr.paper_no;");
        decryptInticsEnc.setOutputPath("1.csv");

        ActionExecutionAudit action = new ActionExecutionAudit();
        action.setActionId(1234L);
        action.setRootPipelineId(2134L);
        action.getContext().put("inbound.rootpipelineid.list", "");
        action.getContext().put("csv.headers.list", "file_name,document_id,auth_admit_date,auth_discharge_date,auth_id,diagnosis_code,level_of_service,member_address_line1,member_city,member_date_of_birth,member_first_name,member_gender,member_group_id,member_id,member_last_name,member_medicaid_id,member_state,member_zipcode,referring_provider_city,referring_provider_first_name,referring_provider_last_name,referring_provider_npi,referring_provider_state,referring_provider_zipcode,service_code,service_from_date,service_to_date,servicing_facility_address_line1,servicing_facility_address_line2,servicing_facility_city,servicing_facility_first_name,servicing_facility_last_name,servicing_facility_npi,servicing_facility_specialty,servicing_facility_state,servicing_facility_tin,servicing_facility_zipcode,servicing_provider_address_line1,servicing_provider_city,servicing_provider_first_name,servicing_provider_last_name,servicing_provider_npi,servicing_provider_state,servicing_provider_tin,servicing_provider_zipcode");
        action.getContext().put("tenant_id", "");
        action.getContext().put("target_directory_path", "/home/anandh.andrews@zucisystems.com/intics-workspace/Asgard/ANTHEM-docs/AUMI/build/testing/cs-upload-alchemy/outputs/");
        action.getContext().put("document_type", "");
        DecryptInticsEncAction decryptInticsEncAction = new DecryptInticsEncAction(action, log, decryptInticsEnc);
        decryptInticsEncAction.execute();
    }

    @Test
    void executeCustomTable() throws Exception {
        DecryptInticsEnc decryptInticsEnc = new DecryptInticsEnc();

        decryptInticsEnc.setName("decryption and convert to csv");
        decryptInticsEnc.setCondition(true);
        decryptInticsEnc.setEndpoint("http://localhost:8190/vulcan/api/encryption/decrypt");
        decryptInticsEnc.setSource("intics_zio_db_conn");
        decryptInticsEnc.setForkBatchSize("10");
        decryptInticsEnc.setQuerySet("SELECT root_pipeline_id, group_id, tenant_id, sor_container_name, file_name, origin_id, paper_no, extracted_meta_detail FROM voting.csv_export_data_18738;");
        decryptInticsEnc.setOutputPath("/home/anandh.andrews@zucisystems.com/intics-workspace/Asgard/ANTHEM-docs/AUMI/build/testing/cs-upload-alchemy/outputs/1.csv");

        ActionExecutionAudit action = new ActionExecutionAudit();
        action.setActionId(1234L);
        action.setRootPipelineId(2134L);
        action.getContext().put("inbound.rootpipelineid.list", "");
        action.getContext().put("tenant_id", "");
        action.getContext().put("document_type", "");
        DecryptInticsEncAction decryptInticsEncAction = new DecryptInticsEncAction(action, log, decryptInticsEnc);
        decryptInticsEncAction.execute();
    }


    @Test
    void executeIf() {
    }
}