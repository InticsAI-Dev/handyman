package in.handyman.raven.lib.adapters.comparison;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;


import static org.junit.jupiter.api.Assertions.*;

class GenderComparisonAdapterTest {
    private GenderComparisonAdapter adapter;
    private ControlDataComparisonQueryInputTable record;
    private ActionExecutionAudit mockAction;
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        adapter = new GenderComparisonAdapter();
        record = new ControlDataComparisonQueryInputTable();
        mockAction = Mockito.mock(ActionExecutionAudit.class);
        mockLogger = Mockito.mock(Logger.class);
    }

    @Test
    void testMaleVsM() {
        record.setActualValue("Male");
        record.setExtractedValue("M");
        record.setAllowedAdapter("gender");

        Long mismatch = adapter.validate(record, mockAction, mockLogger);

        assertEquals(0L, mismatch);
    }

    @Test
    void testFemaleMismatch() {
        record.setActualValue("Female");
        record.setExtractedValue("Male");
        record.setAllowedAdapter("gender");

        Long mismatch = adapter.validate(record, mockAction, mockLogger);

        assertTrue(mismatch > 0);
    }

    @Test
    void testInvalidGender() {
        record.setActualValue("Other");
        record.setExtractedValue("F");
        record.setAllowedAdapter("gender");

        Long mismatch = adapter.validate(record, mockAction, mockLogger);

        assertEquals(1L, mismatch); // invalid normalization
    }
}

