package in.handyman.raven.lib.adapters.comparison;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SimilarityComparisonAdapterTest {
    private SimilarityComparisonAdapter adapter;
    private ControlDataComparisonQueryInputTable record;
    private ActionExecutionAudit mockAction;
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        adapter = new SimilarityComparisonAdapter();
        record = new ControlDataComparisonQueryInputTable();
        mockAction = Mockito.mock(ActionExecutionAudit.class);
        mockLogger = Mockito.mock(Logger.class);

        Mockito.when(mockAction.getContext()).thenReturn(Map.of(
                "controldata.comparision.similarity.score", "70"
        ));
    }

    @Test
    void testExactMatch() {
        record.setActualValue("Hello World");
        record.setExtractedValue("Hello World");
        record.setAllowedAdapter("alphanumeric");

        Long mismatch = adapter.validate(record, mockAction, mockLogger);

        assertEquals(0L, mismatch);
    }

    @Test
    void testCaseInsensitiveMatch() {
        record.setActualValue("Hello World");
        record.setExtractedValue("hello world");
        record.setAllowedAdapter("string");

        Long mismatch = adapter.validate(record, mockAction, mockLogger);

        assertEquals(0L, mismatch); // normalized match
    }

    @Test
    void testMultiValueNormalization() {
        record.setActualValue("Apple,Banana");
        record.setExtractedValue("banana,apple"); // unordered
        record.setLineItemType("multi_value");
        record.setAllowedAdapter("string");

        Long mismatch = adapter.validate(record, mockAction, mockLogger);

        assertEquals(0L, mismatch); // normalized to same
    }

    @Test
    void testDifferentStrings() {
        record.setActualValue("Hello");
        record.setExtractedValue("World");
        record.setAllowedAdapter("string");

        Long mismatch = adapter.validate(record, mockAction, mockLogger);

        assertTrue(mismatch > 0);
    }
    @Test
    void testCase1() {
        assertNormalizedExtractedValue("H0015TG , H2036HI ,H0014", "h0014,H0015TG,H2036HI", "H0015TG,H2036HI,H0014");
    }

    @Test
    void testCase2() {
        assertNormalizedExtractedValue("90791, 90837,90834, 90832", "90791,90832,90834,90837,90567", "90791,90834,90832,90837");
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

    @Test
    void testCase11() {
        assertNormalizedExtractedValue("H0015TG , H2036HI ,H0014", "H0014,H0015TG,H2036HI", "H0015TG,H2036HI,H0014");
    }

    @Test
    void shouldPreserveActualOrderWhenExtractedMatchesInDifferentOrder() {
        assertNormalizedExtractedValue(
                "H0015TG , H2036HI , H0014",
                "h0014,H0015TG,H2036HI",
                "H0014,H0015TG,H2036HI"
        );
    }

    @Test
    void shouldNormalizeWhenExtractedHasDifferentOrdering() {
        assertNormalizedExtractedValue(
                "X1234, X5678, X9101, X1121",
                "X1234,X1121,X9101,X5678",
                "X1234,X9101,X5678,X1121"
        );
    }

    @Test
    void shouldAppendExtraTokensNotInActual() {
        assertNormalizedExtractedValue(
                "aBcDeF222, X3333, xX3334",
                "ABCDeF222,X3333,X4444",
                "aBcDeF222,X3333,X4444"
        );
    }

    @Test
    void shouldReturnOnlyExtrasWhenActualDoesNotContainThem() {
        assertNormalizedExtractedValue(
                "X5555, X6666",
                "x5555",
                "x5555"   // corrected, extracted wins
        );
    }

    @Test
    void shouldReturnExtractedWhenNoOverlapWithActual() {
        assertNormalizedExtractedValue(
                "X8888, X9999, X1010",
                "X1111, X1212",
                "X1111,X1212"
        );
    }

    @Test
    void shouldReturnEmptyWhenExtractedIsEmpty() {
        assertNormalizedExtractedValue(
                "X1313, X1414, X1515",
                "",
                ""
        );
    }

    @Test
    void shouldReturnExtractedWhenActualIsEmpty() {
        assertNormalizedExtractedValue(
                "",
                "X1616, X1717, X1818",
                "X1616,X1717,X1818"
        );
    }

    @Test
    void shouldKeepExtractedWhenActualDoesNotContainAnyMatch() {
        assertNormalizedExtractedValue(
                "X1919",
                "X2020, X2121, X2222",
                "X2020,X2121,X2222"
        );
    }

    @Test
    void shouldReturnNullLiteralWhenExtractedIsNullString() {
        assertNormalizedExtractedValue(
                "X2323, X2424, X2525",
                "null",
                "null"
        );
    }

    @Test
    void shouldPreserveCaseAndFormattingFromActual() {
        assertNormalizedExtractedValue(
                "xAB12 , Xcd34 , XEf56",
                "xab12,XCd34,Xef56",
                "xAB12,Xcd34,XEf56"
        );
    }
    private void assertNormalizedExtractedValue(String actual, String extracted, String expected) {
        String normalized =adapter.getNormalizedExtractedValue(actual, extracted, "multi_value");

        System.out.println("Case -> Result: " + normalized + " | Expected: " + expected);

        assertEquals(expected, normalized, "Mismatch in normalization case");
    }

}

