package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.adapters.comparison.ContainsComparisonAdapter;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContainsComparisonAdapterTest {

    private ContainsComparisonAdapter adapter;
    private Logger log;
    private ActionExecutionAudit audit;

    @BeforeEach
    void setUp() {
        adapter = new ContainsComparisonAdapter();
        log = LoggerFactory.getLogger(ContainsComparisonAdapterTest.class);
        audit = new ActionExecutionAudit();
    }

    private ControlDataComparisonQueryInputTable buildInput(String actual, String extracted, String type) {
        return ControlDataComparisonQueryInputTable.builder()
                .actualValue(actual)
                .extractedValue(extracted)
                .lineItemType(type)
                .originId("TEST_ORIGIN")
                .paperNo(1L)
                .sorItemName("TEST_ITEM")
                .tenantId(99L)
                .build();
    }

    @Test
    void testExactMatchSameOrder() {
        ControlDataComparisonQueryInputTable input = buildInput(
                "Dhanekula Manohar", "Dhanekula Manohar", "multi_value"
        );
        Long result = adapter.dataValidation(input, log, audit);
        assertEquals(0L, result, "Names should match exactly");
    }

    @Test
    void testExactMatchDifferentOrder() {
        ControlDataComparisonQueryInputTable input = buildInput(
                "Dhanekula Manohar", "Manohar Dhanekula", "single_value"
        );
        Long result = adapter.dataValidation(input, log, audit);
        assertEquals(0L, result, "Names should match even if swapped");
    }

    @Test
    void testThreeWordsReordered() {
        ControlDataComparisonQueryInputTable input = buildInput(
                "Dhanekula Giri Manohar, Dhanekula Manohar", "Dhanekula Manohar, Dhanekula Giri Manohar", "multi_value"
        );
        Long result = adapter.dataValidation(input, log, audit);
        assertEquals(0L, result, "Names should match when all tokens are present in different order");
    }

    @Test
    void testPartialMismatch() {
        ControlDataComparisonQueryInputTable input = buildInput(
                "Manohar Dhanekula", "Manohar Giri", "multi_value"
        );
        Long result = adapter.dataValidation(input, log, audit);
        // not equal sets → should return Levenshtein distance > 0
        assertEquals(true, result > 0, "Different tokens should not match");
    }

    @Test
    void testEmptyExtracted() {
        ControlDataComparisonQueryInputTable input = buildInput(
                "Some Value", "", "multi_value"
        );
        Long result = adapter.dataValidation(input, log, audit);
        assertEquals(input.getActualValue().length(), result);
    }

    @Test
    void testEmptyActual() {
        ControlDataComparisonQueryInputTable input = buildInput(
                "", "Some Value", "multi_value"
        );
        Long result = adapter.dataValidation(input, log, audit);
        assertEquals(input.getExtractedValue().length(), result);
    }
}
