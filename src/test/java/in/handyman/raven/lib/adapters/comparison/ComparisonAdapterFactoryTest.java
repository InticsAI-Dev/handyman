package in.handyman.raven.lib.adapters.comparison;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ComparisonAdapterFactoryTest {

    @Test
    void testGetDateAdapter() {
        ComparisonAdapter adapter = ComparisonAdapterFactory.getAdapter("date");
        assertNotNull(adapter);
        assertTrue(adapter instanceof DateComparisonAdapter);
        assertEquals("date", adapter.getName());
    }

    @Test
    void testGetGenderAdapter() {
        ComparisonAdapter adapter = ComparisonAdapterFactory.getAdapter("gender");
        assertNotNull(adapter);
        assertTrue(adapter instanceof GenderComparisonAdapter);
        assertEquals("gender", adapter.getName());
    }

    @Test
    void testGetStringAdapter() {
        ComparisonAdapter adapter = ComparisonAdapterFactory.getAdapter("string");
        assertNotNull(adapter);
        assertTrue(adapter instanceof SimilarityComparisonAdapter);
        assertEquals("string", adapter.getName());
    }

    @Test
    void testGetUnknownAdapterDefaultsToString() {
        ComparisonAdapter adapter = ComparisonAdapterFactory.getAdapter("somethingElse");
        assertNotNull(adapter);
        assertTrue(adapter instanceof SimilarityComparisonAdapter);
        assertEquals("string", adapter.getName());
    }

    @Test
    void testRegisterCustomAdapter() {
        // Custom dummy adapter for testing
        ComparisonAdapter custom = new ComparisonAdapter() {
            @Override
            public Long validate(ControlDataComparisonQueryInputTable record, ActionExecutionAudit action, Logger log) {
                return 42L;
            }
            @Override
            public String getName() {
                return "custom";
            }
        };

        ComparisonAdapterFactory.register(custom);

        ComparisonAdapter fetched = ComparisonAdapterFactory.getAdapter("custom");
        assertNotNull(fetched);
        assertEquals("custom", fetched.getName());
        assertEquals(42L, fetched.validate(new ControlDataComparisonQueryInputTable(), null, null));
    }
}
