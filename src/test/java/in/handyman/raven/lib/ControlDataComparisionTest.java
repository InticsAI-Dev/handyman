package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ControlDataComparison;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.logging.Logger;

@Slf4j
public class ControlDataComparisionTest {
    @Test
    void executeProduct() throws Exception {

        ControlDataComparison controlDataComparison = ControlDataComparison.builder()
                .name("ControlDataComparison")
                .condition(true)
                .resourceConn("intics_zio_db_conn_test")
                .outputTable("coverage_report_output_1.member_date_of_birth_result")
                .inputTable("control_data_output.member_date_of_birth_input_test")
                .querySet("select a.root_pipeline_id ,now(),a.group_id ,a.file_name ,a.origin_id ,a.paper_no,\n" +
                        "a.actual_value as  actual_value , a.extracted_value as extracted_value, \n" +
                        "a.tenant_id, si.allowed_adapter, si.char_limit, si.restricted_adapter\n" +
                        "from control_data_output.member_date_of_birth_input_test a \n" +
                        "join sor_meta.sor_container sc on sc.document_type = a.document_type and sc.tenant_id = a.tenant_id and sc.status = 'ACTIVE'\n" +
                        "join sor_meta.sor_item si on si.sor_container_id = sc.sor_container_id and si.tenant_id = sc.tenant_id and si.status = 'ACTIVE' and si.sor_item_name = 'member_date_of_birth'\n" +
                        "where a.root_pipeline_id = 245687  and a.tenant_id = 115;")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "1"),
                Map.entry("outbound.doc.delivery.notify.url", "https://oxygenfinance.kissflow.com/integration/2/Ac8izeyQlKt7/webhook/YJ81YHEn8NDSqznEsNVezPsZBrpS5c9zvj4i4rSSbt2fHYpi2-ZwIgzbd4lTdRipsmT6FFVz2CrhLQI6xd6g"),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("agadia.secretKey", ""),
                Map.entry("outbound.context.condition", "Product"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        ControlDataComparisonAction deliveryNotifyAction = new ControlDataComparisonAction(actionExecutionAudit, log, controlDataComparison );
        deliveryNotifyAction.execute();
    }

    @Test
    void dateValidation() throws Exception{
        log.info("Date validation started");
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "1"),
                Map.entry("outbound.doc.delivery.notify.url", "https://oxygenfinance.kissflow.com/integration/2/Ac8izeyQlKt7/webhook/YJ81YHEn8NDSqznEsNVezPsZBrpS5c9zvj4i4rSSbt2fHYpi2-ZwIgzbd4lTdRipsmT6FFVz2CrhLQI6xd6g"),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("agadia.secretKey", ""),
                Map.entry("outbound.context.condition", "Product"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        ControlDataComparisonAction deliveryNotifyAction = new ControlDataComparisonAction(actionExecutionAudit, log, ControlDataComparison.builder().build() );
        Long validationResults = deliveryNotifyAction.dateValidation("20000217", "17/02/2000", "yyyy/MM/dd");
        System.out.println(validationResults);
        log.info("Date validation completed");
    }

    @Test
    void dataValidation() throws Exception{
        log.info("Data validation started");
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "1"),
                Map.entry("outbound.doc.delivery.notify.url", "https://oxygenfinance.kissflow.com/integration/2/Ac8izeyQlKt7/webhook/YJ81YHEn8NDSqznEsNVezPsZBrpS5c9zvj4i4rSSbt2fHYpi2-ZwIgzbd4lTdRipsmT6FFVz2CrhLQI6xd6g"),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("agadia.secretKey", ""),
                Map.entry("outbound.context.condition", "Product"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        ControlDataComparisonAction deliveryNotifyAction = new ControlDataComparisonAction(actionExecutionAudit, log, ControlDataComparison.builder().build() );
        Long validationResults = deliveryNotifyAction.dataValidation("Jon D", "John");
        System.out.println(validationResults);
        log.info("Data validation completed");
    }
}
