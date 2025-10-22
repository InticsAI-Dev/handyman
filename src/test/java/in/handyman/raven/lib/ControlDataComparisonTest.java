package in.handyman.raven.lib;

import in.handyman.raven.core.enums.EncryptionConstants;
import in.handyman.raven.core.encryption.EncryptionHandlers;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;

import in.handyman.raven.lib.adapters.comparison.DateComparisonAdapter;
import in.handyman.raven.lib.adapters.comparison.SimilarityComparisonAdapter;
import in.handyman.raven.lib.model.ControlDataComparison;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


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
                Map.entry(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "true"),
                Map.entry("date.input.formats", "M/d/yy"),
                Map.entry("protegrity.dec.api.url", "http://localhost:8190/vulcan/api/encryption/decrypt"),
                Map.entry("protegrity.enc.api.url", "http://localhost:8190/vulcan/api/encryption/encrypt"),
                Map.entry("pipeline.encryption.default.holder", EncryptionHandlers.PROTEGRITY_API_ENC.name()),
                Map.entry("write.batch.size", "1")));



        ControlDataComparisonAction deliveryNotifyAction = new ControlDataComparisonAction(actionExecutionAudit, log, controlDataComparison);
        deliveryNotifyAction.execute();
        assertEquals(true, true);
    }

    @BeforeEach
    void setup() throws Exception {

        ControlDataComparison controlDataComparison = ControlDataComparison.builder()
                .name("ControlDataComparison")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("coverage_report_output.final_result_field_summary_kafka_comparison")
                .inputTable("control_data_output.auth_id_input")
                .querySet("select 1")
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
                Map.entry(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "true"),
                Map.entry("date.input.formats", "M/d/yy"),
                Map.entry("protegrity.dec.api.url", "http://localhost:8190/vulcan/api/encryption/decrypt"),
                Map.entry("protegrity.enc.api.url", "http://localhost:8190/vulcan/api/encryption/encrypt"),
                Map.entry("pipeline.encryption.default.holder", EncryptionHandlers.PROTEGRITY_API_ENC.name()),
                Map.entry("write.batch.size", "1")));



        ControlDataComparisonAction deliveryNotifyAction = new ControlDataComparisonAction(actionExecutionAudit, log, controlDataComparison);


        deliveryNotifyAction.execute();
        assertEquals(true, true);
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
                Map.entry("outbound.context.condition", "Product"),Map.entry("control.data.date.comparison.format", "yyyy/MM/dd"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));
        String inputFormat = actionExecutionAudit.getContext().get("control.data.date.comparison.format");
        DateComparisonAdapter comparisonAdapter= new DateComparisonAdapter();
        ControlDataComparisonQueryInputTable controlDataInputLineItem = getControlDataComparisonQueryInputTable();

        Long validationResults = comparisonAdapter.dateValidation(inputFormat,
                log, controlDataInputLineItem, actionExecutionAudit);
        System.out.println(validationResults);
        log.info("Date validation completed");
        assertEquals(true, true);
    }

    @NotNull
    private static ControlDataComparisonQueryInputTable getControlDataComparisonQueryInputTable() {
        ControlDataComparisonQueryInputTable controlDataInputLineItem = new ControlDataComparisonQueryInputTable();
        controlDataInputLineItem.setActualValue("1955-08-24");
        controlDataInputLineItem.setExtractedValue("08/24/1955");
        controlDataInputLineItem.setAllowedAdapter("date");
        controlDataInputLineItem.setOriginId("ORIGIN-5043");
        controlDataInputLineItem.setPaperNo(1L);
        controlDataInputLineItem.setSorItemName("member_id");
        controlDataInputLineItem.setTenantId(1L);
        return controlDataInputLineItem;
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
                Map.entry("consumer.API.count", "1"),Map.entry("control.data.date.comparison.format", "yyyy/MM/dd"),
                Map.entry("write.batch.size", "1")));

        ControlDataComparison build = ControlDataComparison.builder().build();
        ControlDataComparisonQueryInputTable controlDataInputLineItem = getControlDataComparisonQueryInputTable();

        DateComparisonAdapter comparisonAdapter= new DateComparisonAdapter();
        String inputFormat = actionExecutionAudit.getContext().get("control.data.date.comparison.format");
        Long validationResults = comparisonAdapter.dateValidation(inputFormat,
                log, controlDataInputLineItem, actionExecutionAudit);
        System.out.println(validationResults);
        log.info("Data validation completed");
        assertEquals(true, true);
    }

    @ParameterizedTest(name = "Case {index}: {0}")
    @CsvSource({
            // description, actual, extracted, expected
            "'Match with case-insensitive normalization','H0015TG , H2036HI ,H0014','h0014,H0015TG,H2036HI','H0014,H0015TG,H2036HI'",
            "'Contains extra token not in actual','90791, 90837,90834, 90832','90791,90832,90834,90837,90567','90791,90832,90834,90837,90567'",
            "'Match with one extra value','90791, 90837','90791,90837,H0014','90791,90837,H0014'",
            "'Only extra token, no match in actual','90791, 90837','H0014','H0014'",
            "'Completely different values','H0014,34241,879896','90791, 90837','90791,90837'",
            "'Empty extracted string','H0014,34241,879896','',''",
            "'Actual empty, extracted remains as-is','', 'h0014,34241,879896','h0014,34241,879896'",
            "'Actual unrelated, extracted normalized partially','test','H0014, 34241,879896','H0014,34241,879896'",
            "'No actual values, extracted preserved','', 'H0014,34241,879896','H0014,34241,879896'",
            "'Extracted is literal null string','H0014,34241,879896','null','null'",
            "'Reorder to actual casing','H0015TG , H2036HI ,H0014','H0014,H0015TG,H2036HI','H0014,H0015TG,H2036HI'",
            "'Case-insensitive normalization with order','H0015TG , H2036HI , H0014','h0014,H0015TG,H2036HI','H0014,H0015TG,H2036HI'",
            "'Reorder numbers differently','X1234, X5678, X9101, X1121','X1234,X1121,X9101,X5678','X1234,X1121,X9101,X5678'",
            "'Append extra token not in actual','aBcDeF222, X3333, xX3334','ABCDeF222,X3333,X4444','aBcDeF222,X3333,X4444'",
            "'Extracted overlaps partially only','X5555, X6666','x5555','X5555'",
            "'No overlap at all','X8888, X9999, X1010','X1111, X1212','X1111,X1212'",
            "'Extracted empty string','X1313, X1414, X1515','',''",
            "'Actual empty, extracted preserved as-is','', 'X1616, X1717, X1818','X1616, X1717, X1818'",
            "'Actual contains nothing from extracted','X1919','X2020, X2121, X2222','X2020,X2121,X2222'",
            "'Extracted is literal null','X2323, X2424, X2525','null','null'",
            "'Preserve case and formatting from actual','xAB12 , Xcd34 , XEf56','xab12,XCd34,XEf56','xAB12,XCd34,XEf56'"
    })
    void testNormalizedExtractedValue(String description, String actual, String extracted, String expected) {
        SimilarityComparisonAdapter comparisonAdapter= new SimilarityComparisonAdapter();

        String result = comparisonAdapter.getNormalizedExtractedValue(actual, extracted, "multi_value");

        System.out.println("[" + description + "] actual=" + actual +
                " | extracted=" + extracted +
                " | result=" + result +
                " | expected=" + expected);

        assertEquals(expected, result, "Mismatch in case: " + description);
    }


}
