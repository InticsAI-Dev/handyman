package in.handyman.raven.lib.model.sectionFilter;

import in.handyman.raven.lib.adapters.selections.ExtractedField;
import in.handyman.raven.lib.adapters.selections.WhitelistFilterAdapter;
import in.handyman.raven.lib.adapters.selections.models.WhitelistLabelConfig;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class WhitelistFilterAdapterTest {

    private final WhitelistFilterAdapter adapter = new WhitelistFilterAdapter();

    private ExtractedField createField(String section, String label, String value,
                                       Set<String> whitelistContains,
                                       Set<String> whitelistExact,List<WhitelistLabelConfig> configs) {

        ExtractedField field = new ExtractedField();
        field.setSectionAlias(section);
        field.setLabel(label);
        field.setValue(value);
        field.setLabelMatching(true);


        field.setWhitelistedLabels(configs);

        return field;
    }

    @Test
    void testExactLabelMatchInWhitelist() {
        ExtractedField field = createField(
                "SectionA",
                "Patient Name",
                "John",
                Set.of(),                 // contains rules
                Set.of("PatientName"),     // EXACT match after sanitization
                List.of(new WhitelistLabelConfig("PatientName", "EXACT"))
        );

        ExtractedField processed = adapter.filter(List.of(field)).get(0);

        assertTrue(processed.isLabelMatching());
        assertTrue(processed.getLabelMatchMessage().contains("(CONFIG: EXACT)"));
        assertTrue(processed.getLabelMatchMessage().contains("matched EXACT label"));
    }

    @Test
    void testPartialMatchInWhitelist() {
        ExtractedField field = createField(
                "SectionA",
                "Patient Name",
                "John",
                Set.of("patient"),  // CONTAINS
                Set.of(),
                List.of(new WhitelistLabelConfig("PatientName", "CONTAINS"))
        );

        ExtractedField processed = adapter.filter(List.of(field)).get(0);

        assertTrue(processed.isLabelMatching());
        assertTrue(processed.getLabelMatchMessage().contains("(CONFIG: CONTAINS)"));
        assertTrue(processed.getLabelMatchMessage().contains("matched in CONTAINS mode"));
    }

    @Test
    void testValueOnlyMatchInWhitelist() {
        ExtractedField field = createField(
                "SectionA",
                "ID",
                "DOB",
                Set.of(),
                Set.of("dob"),   // EXACT match on value
                List.of(new WhitelistLabelConfig("dob", "EXACT"))
        );

        ExtractedField processed = adapter.filter(List.of(field)).get(0);

        assertTrue(processed.isLabelMatching());
        assertTrue(processed.getLabelMatchMessage().contains("matched EXACT value"));
        assertTrue(processed.getLabelMatchMessage().contains("(CONFIG: EXACT)"));
    }

    @Test
    void testNoMatchFound() {
        ExtractedField field = createField(
                "Authorization",
                "ID",
                "12345",
                Set.of("Patient"),   // contains
                Set.of("Name"),       // exact
                List.of(new WhitelistLabelConfig("PatientName", "CONTAINS"))
        );

        ExtractedField processed = adapter.filter(List.of(field)).get(0);

        assertFalse(processed.isLabelMatching());
        assertEquals("No match found between LABELS and whitelist. (CONFIG: NONE MATCHED)",
                processed.getLabelMatchMessage());
    }

    @Test
    void testEmptyWhitelist() {
        ExtractedField field = createField(
                "SectionA",
                "Address1",
                "123 Street",
                Set.of(),
                Set.of(),
                List.of()
        );

        ExtractedField processed = adapter.filter(List.of(field)).get(0);

        // Because empty -> allowed
        assertTrue(processed.isLabelMatching());
        assertTrue(processed.getLabelMatchMessage().contains("No whitelist provided"));
    }

    @Test
    void testEmptyLabel() {
        ExtractedField field = createField(
                "SectionA",
                "",
                "123 Street",
                Set.of(),
                Set.of("Address"),   // exact match would not work since label is empty
                List.of(new WhitelistLabelConfig("PatientName", "EXACT"))
        );

        ExtractedField processed = adapter.filter(List.of(field)).get(0);

        // Nothing matches
        assertFalse(processed.isLabelMatching());
        assertTrue(processed.getLabelMatchMessage().contains("NONE MATCHED"));
    }
}
