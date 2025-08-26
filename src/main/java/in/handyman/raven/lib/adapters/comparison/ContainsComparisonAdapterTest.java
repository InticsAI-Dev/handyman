package in.handyman.raven.lib.adapters.comparison;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.ControlDataComparisonAction;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ContainsComparisonAdapterTest {

    private final ContainsComparisonAdapter adapter = new ContainsComparisonAdapter();


    private ControlDataComparisonQueryInputTable buildInput(String actual, String extracted, String type) {
        ControlDataComparisonQueryInputTable table = new ControlDataComparisonQueryInputTable();
        table.setActualValue(actual);
        table.setExtractedValue(extracted);
        table.setLineItemType(type);
        table.setOriginId("origin-1");
        table.setPaperNo(101L);
        table.setSorItemName("test-sor");
        table.setAllowedAdapter("alphanumeric");
        table.setTenantId(99L);
        return table;
    }


    @Test
    void shouldReturnZeroWhenExactMatchWithMatchStatus() {
        ControlDataComparisonQueryInputTable input = buildInput("Hello123", "Hello123", "single_value");
        Long result = adapter.dataValidation(input, log, new ActionExecutionAudit());
        String matchStatus=ControlDataComparisonAction.calculateValidationScores(result,"1","3");

        assertEquals("NO TOUCH", matchStatus);
        System.out.println("Match Status: "+matchStatus+", Distance: "+result);
    }


    @Test
    void shouldOneTouchWhenExactHavingSingleDiffWithMatchStatus() {
        ControlDataComparisonQueryInputTable input = buildInput("Hello123", "Hello120", "single_value");
        Long result = adapter.dataValidation(input, log, new ActionExecutionAudit());
        String matchStatus=ControlDataComparisonAction.calculateValidationScores(result,"1","3");

        assertEquals("ONE TOUCH", matchStatus);
        System.out.println("Match Status: "+matchStatus+", Distance: "+result);
    }




    // ---- getNormalizedExtractedValue ----

    @Test
    void shouldNormalizeExtractedValuesToActualCasing() {
        String result = adapter.getNormalizedExtractedValue("H0014, H0020", "h0014,h0020", "multi_value");
        assertEquals("H0014,H0020", result);
    }

    @Test
    void shouldPreserveExtraExtractedValues() {
        String result = adapter.getNormalizedExtractedValue("H0014", "H0014,NEW1", "multi_value");
        assertEquals("H0014,NEW1", result);
    }

    @Test
    void shouldReturnExtractedWhenActualIsEmpty() {
        String result = adapter.getNormalizedExtractedValue("", "h0014,h0020", "multi_value");
        assertEquals("h0014,h0020", result);
    }

    @Test
    void shouldReturnEmptyWhenExtractedIsEmpty() {
        String result = adapter.getNormalizedExtractedValue("H0014, H0020", "", "multi_value");
        assertEquals("", result);
    }

    @Test
    void shouldReturnExtractedAsIsForNonMultiValueType() {
        String result = adapter.getNormalizedExtractedValue("H0014, H0020", "h0014,h0020", "single_value");
        assertEquals("h0014,h0020", result);
    }

    // ---- dataValidation ----

    @Test
    void shouldReturnZeroWhenExactMatch() {
        ControlDataComparisonQueryInputTable input = buildInput("Hello123", "Hello123", "single_value");
        Long result = adapter.dataValidation( input, log, new ActionExecutionAudit());
        assertEquals(0L, result);
    }

    @Test
    void shouldReturnZeroWhenExtractedContainsActual() {
        ControlDataComparisonQueryInputTable input = buildInput("abc", "xxabcxx", "single_value");
        Long result = adapter.dataValidation( input, log, new ActionExecutionAudit());
        assertEquals(0L, result);
    }

    @Test
    void shouldReturnZeroWhenWordSetsAreEqual() {
        ControlDataComparisonQueryInputTable input = buildInput("Hello World", "world hello", "single_value");
        Long result = adapter.dataValidation( input, log, new ActionExecutionAudit());
        assertEquals(0L, result);
    }

    @Test
    void shouldReturnDistanceForDifferentStrings() {
        ControlDataComparisonQueryInputTable input = buildInput("Hello", "Hxllo", "single_value");
        Long result = adapter.dataValidation( input, log, new ActionExecutionAudit());
        assertTrue(result > 0, "Expected positive distance");
    }

    @Test
    void shouldHandleEmptyExtracted() {
        ControlDataComparisonQueryInputTable input = buildInput("Hello", "", "single_value");
        Long result = adapter.dataValidation( input, log, new ActionExecutionAudit());
        assertEquals(5L, result); // length of actual
    }

    @Test
    void shouldHandleEmptyActual() {
        ControlDataComparisonQueryInputTable input = buildInput("", "Hello", "single_value");
        Long result = adapter.dataValidation( input, log, new ActionExecutionAudit());
        assertEquals(5L, result); // length of extracted
    }
}
