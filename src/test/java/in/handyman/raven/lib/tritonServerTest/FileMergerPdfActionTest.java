package in.handyman.raven.lib.tritonServerTest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;

import in.handyman.raven.lib.model.FileMergerPdf;
import in.handyman.raven.lib.FileMergerPdfAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class FileMergerPdfActionTest {

    @Test
    void tritonServer() throws Exception {
        FileMergerPdf filemergerPdf = FileMergerPdf.builder()
                .condition(true)
                .name("FileMergerPdf")
                .outputDir("/data/output")
                .outputTable("alchemy_response.cleaned_pdf_info")
                .querySet("SELECT 'oxy_org1' as origin_id ,replace(concat('cleaned_pdf_','INT-1','.pdf'),'-','_') as output_file_name, 1 as group_id, ARRAY['/data/output/pdf_to_image/test/test_0.jpg','/data/output/pdf_to_image/test/test_1.jpg'] as filePaths, 1 as tenant_id, 1 as root_pipeline_id, 138980184199100180 as process_id;")
                .resourceConn("intics_zio_db_conn")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.file-merger.url","http://192.168.10.245:9300/v2/models/merger-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","1"),
                Map.entry("file.merger.consumer.API.count","1"),
                Map.entry("gen_group_id.group_id","1"),
                Map.entry("write.batch.size","1")));



        FileMergerPdfAction fileMergerPdfAction=new FileMergerPdfAction(actionExecutionAudit,log,filemergerPdf);
        fileMergerPdfAction.execute();
    }

    @Test
    void tritonServer1() throws Exception {
        FileMergerPdf filemergerPdf = FileMergerPdf.builder()
                .condition(true)
                .name("FileMergerPdf")
                .outputDir("/data/output")
                .outputTable("alchemy_response.cleaned_pdf_info")
                .querySet("SELECT origin_id, output_file_name, group_id, filepaths, tenant_id, root_pipeline_id, process_id\n" +
                        "FROM macro.cleaned_pdf_input;")
                .resourceConn("intics_zio_db_conn")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.file-merger.url","http://192.168.10.245:9300/v2/models/merger-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","1"),
                Map.entry("file.merger.consumer.API.count","1"),
                Map.entry("gen_group_id.group_id","1"),
                Map.entry("write.batch.size","1")));



        FileMergerPdfAction fileMergerPdfAction=new FileMergerPdfAction(actionExecutionAudit,log,filemergerPdf);
        fileMergerPdfAction.execute();
    }

}
