package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;

import in.handyman.raven.lib.FileMergerPdfAction;
import in.handyman.raven.lib.model.FileMergerPdf;
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
                .outputDir("/data/output/")
                .endPoint("http://192.168.10.248:9300/v2/models/merger-service/versions/1/infer")
                .querySet("SELECT sot.origin_id, concat(a2.file_name, '_001.pdf') as output_file_name, sot.group_id, array_agg(a.file_path ORDER BY paper_no ASC) as file_paths, sot.tenant_id, sot.root_pipeline_id, sot.root_pipeline_id as process_id, sot.batch_id\n" +
                        "FROM outbound.outbound_payload_queue opq\n" +
                        "inner join info.source_of_truth sot on sot.origin_id = opq.origin_id and sot.batch_id = opq.batch_id\n" +
                        "inner join info.asset a on a.file_id =sot.preprocessed_file_id\n" +
                        "left join info.source_of_origin soo on soo.origin_id = opq.origin_id\n" +
                        "left join info.asset a2 on a2.asset_id=soo.asset_id\n" +
                        "where sot.group_id= 61 and sot.tenant_id = 81 and opq.status='STAGED' and opq.stage='OUTBOUND'  and sot.batch_id = 'BATCH-61_0'\n" +
                        "group by sot.origin_id, a2.file_name, sot.group_id, sot.tenant_id, sot.root_pipeline_id, sot.batch_id\n")
                .resourceConn("intics_zio_db_conn")
                .outputTable("alchemy_response.cleaned_pdf_info")

                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.file-merger.url","http://192.168.10.245:9300/v2/models/merger-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size","1"),
                Map.entry("file.merger.consumer.API.count","1"),
                Map.entry("merger.response.download.url","http://192.168.10.248:10001/multipart-download"),
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
                .outputDir("/data/output/")
                .endPoint("http://192.168.10.248:9300/v2/models/merger-service/versions/1/infer")
                .outputTable("alchemy_response.cleaned_pdf_info")
                .querySet("SELECT sot.origin_id, concat(a2.file_name, '_001.pdf') as output_file_name, sot.group_id, array_agg(a.file_path ORDER BY paper_no ASC) as file_paths, sot.tenant_id, sot.root_pipeline_id, sot.root_pipeline_id as process_id, sot.batch_id\n" +
                        "FROM outbound.outbound_payload_queue opq\n" +
                        "inner join info.source_of_truth sot on sot.origin_id = opq.origin_id and sot.batch_id = opq.batch_id\n" +
                        "inner join info.asset a on a.file_id =sot.preprocessed_file_id\n" +
                        "left join info.source_of_origin soo on soo.origin_id = opq.origin_id\n" +
                        "left join info.asset a2 on a2.asset_id=soo.asset_id\n" +
                        "where sot.group_id= 61 and sot.tenant_id = 81 and opq.status='STAGED' and opq.stage='OUTBOUND'  and sot.batch_id = 'BATCH-61_0'\n" +
                        "group by sot.origin_id, a2.file_name, sot.group_id, sot.tenant_id, sot.root_pipeline_id, sot.batch_id;\n")
                .resourceConn("intics_zio_db_conn")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.file-merger.url","http://192.168.10.248:9300/v2/models/merger-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","1"),
                Map.entry("file.merger.consumer.API.count","1"),
                Map.entry("gen_group_id.group_id","1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("actionId", "1"),
                Map.entry("merger.response.download.url","false"),
                Map.entry("write.batch.size","1")));



        FileMergerPdfAction fileMergerPdfAction=new FileMergerPdfAction(actionExecutionAudit,log,filemergerPdf);
        fileMergerPdfAction.execute();
    }

}
