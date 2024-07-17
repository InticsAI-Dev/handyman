package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ProductOutboundZipfile;
import in.handyman.raven.lib.model.ZipFileCreationOutbound;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class ProductOutboundZipfileActionTest {

    @Test
    public void unitTestCase() throws Exception {
        ProductOutboundZipfile productOutboundZipfile = ProductOutboundZipfile.builder()
                .name("product outbound zip file details")
                .outputDir("/home/anandh.andrews@zucisystems.com/intics-workspace/testing/output")
                .resultTable("product_outbound.product_outbound_zip_file_details")
                .processId("1234567")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT origin_id, root_pipeline_id, group_id, process_id, cleaned_pdf_path, origin_pdf_path, product_json, kvp_response, table_response, tenant_id,file_name\n" +
                        "FROM product_outbound.product_outbound_zip_file_input;")
                .condition(true)
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size","5"),
                Map.entry("gen_group_id.group_id","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("write.batch.size","5")));

        ProductOutboundZipfileAction productOutboundZipfileAction=new ProductOutboundZipfileAction(actionExecutionAudit,log,productOutboundZipfile);
        productOutboundZipfileAction.execute();
    }

    @Test
    public void unitTestForZip() throws Exception {

        ZipFileCreationOutbound zipFileCreationOutbound= ZipFileCreationOutbound.builder()
                .name("outbound zip ")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputDir("/home/anandh.andrews@zucisystems.com/intics-workspace/testing/output/")
                .processId("1246535")
                .querySet(" SELECT 'INT-1' as origin_id, 1 as root_pipeline_id,1 as group_id,1324354 process_id,'/home/anandh.andrews@zucisystems.com/intics-workspace/testing/input/1/transaction/TRZ-1/CM1882524/1/CM1882524.pdf' as cleaned_pdf_path," +
                        "'/home/anandh.andrews@zucisystems.com/intics-workspace/testing/input/1/transaction/TRZ-1/CM1882524/1/CM1882524.pdf' as origin_pdf_path, " +
                        "'{\"a\":\"b\"}' as product_json,'{\"a\":\"b\"}' as kvp_response,1 as tenant_id,'output.pdf' as file_name, 'ORIGIN-20' as alchemy_origin_id")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size","5"),
                Map.entry("gen_group_id.group_id","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("write.batch.size","5")));

        ZipFileCreationOutboundAction productOutboundZipfileAction=new ZipFileCreationOutboundAction(actionExecutionAudit,log,zipFileCreationOutbound);
        productOutboundZipfileAction.execute();
    }

}