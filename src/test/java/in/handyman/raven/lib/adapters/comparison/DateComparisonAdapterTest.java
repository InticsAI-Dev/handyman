package in.handyman.raven.lib.adapters.comparison;

import static org.junit.jupiter.api.Assertions.*;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.Map;


class DateComparisonAdapterTest {
    private DateComparisonAdapter adapter;
    private ControlDataComparisonQueryInputTable record;
    private ActionExecutionAudit mockAction;
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        adapter = new DateComparisonAdapter();
        record = new ControlDataComparisonQueryInputTable();
        mockAction = Mockito.mock(ActionExecutionAudit.class);
        mockLogger = Mockito.mock(Logger.class);

        // set default context for date comparison format
        Mockito.when(mockAction.getContext()).thenReturn(Map.of(
                "control.data.date.comparison.format", "yyyy-MM-dd",
                "date.input.formats", "dd/MM/yyyy;MM-dd-yyyy;yyyyMMdd"
        ));
    }

    @Test
    void testValidDateMatch() {
        record.setActualValue("25/12/2023");
        record.setExtractedValue("2023-12-25");
        record.setAllowedAdapter("date");

        Long mismatch = adapter.validate(record, mockAction, mockLogger);

        assertEquals(0L, mismatch);
    }

    @Test
    void testInvalidDateFormat() {
        record.setActualValue("25-12-2023");
        record.setExtractedValue("abcd");
        record.setAllowedAdapter("date");

        Long mismatch = adapter.validate(record, mockAction, mockLogger);

        assertTrue(mismatch > 0); // fallback mismatch
    }

    @Test
    void testEightDigitDateParsing() {
        record.setActualValue("2023-12-25");
        record.setExtractedValue("25122023"); // ddMMyyyy
        record.setAllowedAdapter("date");

        Long mismatch = adapter.validate(record, mockAction, mockLogger);

        assertEquals(0L, mismatch); // should parse to same date
    }
}
