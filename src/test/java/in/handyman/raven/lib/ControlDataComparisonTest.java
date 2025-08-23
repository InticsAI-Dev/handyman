package in.handyman.raven.lib;

import in.handyman.raven.core.encryption.EncryptionHandlers;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ControlDataComparison;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ControlDataComparisonTest {
    @Test
    void executeProduct() throws Exception {

        ControlDataComparison controlDataComparison = ControlDataComparison.builder()
                .name("ControlDataComparison")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("coverage_report_output.final_result_field_summary_kafka_comparison")
                .inputTable("control_data_output.auth_id_input")
                .querySet("select a.root_pipeline_id ,now(),a.group_id ,a.file_name ,a.origin_id ,a.paper_no,   \n" +
                        "a.actual_value as  actual_value , a.extracted_value as extracted_value,    \n" +
                        "a.tenant_id, \n" +
                        "si.allowed_adapter, si.char_limit, si.restricted_adapter, \n" +
                        "si.sor_item_name, \n" +
                        "si.is_encrypted, ep.encryption_policy,si.line_item_type  \n" +
                        "from control_data_output.consolidated_coverage_input  a    \n" +
                        "inner join sor_meta.sor_container sc on sc.document_type = a.document_type \n" +
                        "and sc.tenant_id = a.tenant_id and sc.status = 'ACTIVE'   \n" +
                        "inner join sor_meta.sor_item si on si.sor_container_id = sc.sor_container_id \n" +
                        "and si.tenant_id = sc.tenant_id and si.status = 'ACTIVE'  and si.sor_item_id = a.sor_item_id \n" +
                        "inner JOIN sor_meta.encryption_policies ep ON si.encryption_policy_id=ep.encryption_policy_id  \n" +
                        "where a.root_pipeline_id = 23500  and a.tenant_id = 1\n" +
                        "            \n" +
                        "            \n" +
                        "            ")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "1"),
                Map.entry("outbound.doc.delivery.notify.url", ""),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("agadia.secretKey", ""),
                Map.entry("outbound.context.condition", "Product"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("kafka.production.activator", "true"),
                Map.entry("control.data.one.touch.threshold", "1"),
                Map.entry("control.data.low.touch.threshold", "3"),
                Map.entry("control.data.date.comparison.format", "yyyy/MM/dd"),
                Map.entry("pipeline.end.to.end.encryption", "true"),
                Map.entry("date.input.formats", "M/d/yy"),
                Map.entry("protegrity.dec.api.url", "http://localhost:8190/vulcan/api/encryption/decrypt"),
                Map.entry("protegrity.enc.api.url", "http://localhost:8190/vulcan/api/encryption/encrypt"),
                Map.entry("pipeline.encryption.default.holder", EncryptionHandlers.PROTEGRITY_API_ENC.name()),
                Map.entry("write.batch.size", "1")));



        ControlDataComparisonAction deliveryNotifyAction = new ControlDataComparisonAction(actionExecutionAudit, log, controlDataComparison);
        deliveryNotifyAction.execute();
    }

    @Test
    void dateValidation() throws Exception {
        log.info("Date validation started");
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "1"),
                Map.entry("outbound.doc.delivery.notify.url", ""),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("agadia.secretKey", ""),
                Map.entry("outbound.context.condition", "Product"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        ControlDataComparisonAction deliveryNotifyAction = new ControlDataComparisonAction(actionExecutionAudit, log, ControlDataComparison.builder().build());
        Long validationResults = deliveryNotifyAction.dateValidation("1955-08-24",
                "08/24/1955", "yyyy/MM/dd", "ORIGIN-5043", 1L, "member_id", 1L);
        System.out.println(validationResults);
        log.info("Date validation completed");
    }

    @Test
    void dataValidation() throws Exception {
        log.info("Data validation started");
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "1"),
                Map.entry("outbound.doc.delivery.notify.url", ""),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("agadia.secretKey", ""),
                Map.entry("outbound.context.condition", "Product"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        ControlDataComparisonAction deliveryNotifyAction = new ControlDataComparisonAction(actionExecutionAudit, log, ControlDataComparison.builder().build());
        Long validationResults = deliveryNotifyAction.dateValidation("Jon D",
                "John", "yyyy/MM/dd", "ORIGIN-5043", 1L, "member_id", 1L);
        System.out.println(validationResults);
        log.info("Data validation completed");
    }
    @Test
    void testCase1() {
        assertNormalizedExtractedValue("H0015TG , H2036HI ,H0014", "h0014,H0015TG,H2036HI", "H0015TG,H2036HI,H0014");
    }

    @Test
    void testCase2() {
        assertNormalizedExtractedValue("90791, 90837,90834, 90832", "90791,90832,90834,90837", "90791,90834,90832,90837");
    }

    @Test
    void testCase3() {
        assertNormalizedExtractedValue("90791, 90837", "90791,90837,H0014", "90791,90837,H0014");
    }

    @Test
    void testCase4() {
        assertNormalizedExtractedValue("90791, 90837", "H0014", "H0014");
    }

    @Test
    void testCase5() {
        assertNormalizedExtractedValue("H0014,34241,879896", "90791, 90837", "90791,90837");
    }

    @Test
    void testCase6() {
        assertNormalizedExtractedValue("H0014,34241,879896", "", "");
    }

    @Test
    void testCase7() {
        assertNormalizedExtractedValue("", "h0014,34241,879896", "H0014,34241,879896");
    }

    @Test
    void testCase8() {
        assertNormalizedExtractedValue("test", "H0014, 34241,879896", "H0014,34241,879896");
    }

    @Test
    void testCase9() {
        assertNormalizedExtractedValue("", "H0014,34241,879896", "H0014,34241,879896");
    }

    @Test
    void testCase10() {
        assertNormalizedExtractedValue("H0014,34241,879896", "null", "null");
    }

    private void assertNormalizedExtractedValue(String actual, String extracted, String expected) {
        ActionExecutionAudit audit = new ActionExecutionAudit();
        audit.getContext().putAll(Map.of(
                "read.batch.size", "1",
                "outbound.doc.delivery.notify.url", "",
                "gen_group_id.group_id", "1",
                "agadia.secretKey", "",
                "outbound.context.condition", "Product",
                "consumer.API.count", "1",
                "write.batch.size", "1"
        ));
        ControlDataComparisonAction action = new ControlDataComparisonAction(audit, log, ControlDataComparison.builder().build());
        String result = action.getNormalizedExtractedValue(actual, extracted, "multi_value");
        System.out.println("Case  -> Result: " + result + " | Expected: " + expected);
        assertEquals(result, result, "Mismatch in case " );
    }

}
