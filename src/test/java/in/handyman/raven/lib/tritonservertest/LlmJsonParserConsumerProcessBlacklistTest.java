package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lib.adapters.selections.BlacklistFilterAdapter;
import in.handyman.raven.lib.adapters.selections.ExtractedField;
import in.handyman.raven.lib.adapters.selections.WhitelistFilterAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LlmJsonParserConsumerProcessBlacklistTest {

    private BlacklistFilterAdapter blacklistAdapter;

    private WhitelistFilterAdapter adapter ;
    @BeforeEach
    public void setUp() {
        blacklistAdapter = new BlacklistFilterAdapter();
        adapter = new WhitelistFilterAdapter();
    }

    // Helper method to create ExtractedField
    private ExtractedField createField(String label, String value) {
        ExtractedField field = new ExtractedField();
        field.setLabel(label);
        field.setValue(value);
        return field;
    }

    private ExtractedField createSections(String section, String value) {
        ExtractedField field = new ExtractedField();
        field.setSectionAlias(section);
        field.setValue(value);
        return field;
    }

    @Test
    public void testExactBlacklistedLabel() {
        Set<String> blackListLabels = Set.of("FirstName", "LastName", "Email");
        ExtractedField response = createField("FirstName!@#", "John");

        ExtractedField result = blacklistAdapter.isLabelValueMatching(blackListLabels, response, "LABELS");

        assertFalse(result.isLabelMatching(), "Label should be blacklisted");
        assertEquals("LABELS is blacklisted and not allowed.", result.getLabelMatchMessage());
        assertEquals("FirstName!@#", result.getLabel());
    }

    @Test
    public void testBlacklistedLabelWithSpecialCharacters() {
        Set<String> blackListLabels = Set.of("FirstName!", "LastName#", "Email@");
        ExtractedField response = createField("FirstName$%^", "John");

        ExtractedField result = blacklistAdapter.isLabelValueMatching(blackListLabels, response, "LABELS");

        assertFalse(result.isLabelMatching(), "Label should be blacklisted after sanitization");
        assertEquals("LABELS is blacklisted and not allowed.", result.getLabelMatchMessage());
    }
    @Test
    public void testBlacklistedLabelSubstringOfSectionAlias() {
        // Arrange
        Set<String> blackListLabels = Set.of("RECIPIENT INFORMATION");
        ExtractedField response = createSections("A. RECIPIENT INFORMATION", "John");

        // Act
        ExtractedField result = blacklistAdapter.isLabelValueMatching(blackListLabels, response, "SECTIONS");

        // Assert
        assertFalse(result.isLabelMatching(), "Section alias contains a blacklisted term and should not match.");
        assertEquals("SECTIONS contains a blacklisted term and is not allowed.",
                result.getLabelMatchMessage(),
                "Incorrect message when section alias contains a blacklisted term.");
    }
    @Test
    public void testBlacklistedLabelAfterValueRemoval() {
        Set<String> blackListLabels = Set.of("Customer", "OtherLabel");
        ExtractedField response = createField("CustomerName!@#", "Name$%^");

        ExtractedField result = blacklistAdapter.isLabelValueMatching(blackListLabels, response, "LABELS");

        assertFalse(result.isLabelMatching(), "Updated label should be blacklisted");
        assertEquals("Updated LABELS is blacklisted after removing value.", result.getLabelMatchMessage());
        assertEquals("Customer", result.getLabel());
    }

    @Test
    public void testNonBlacklistedLabel() {
        Set<String> blackListLabels = Set.of("FirstName", "LastName");
        ExtractedField response = createField("Address!@#", "123 Main St");

        ExtractedField result = blacklistAdapter.isLabelValueMatching(blackListLabels, response, "LABELS");

        assertTrue(result.isLabelMatching(), "Label should not be blacklisted");
        assertEquals("No match found between LABELS and value.", result.getLabelMatchMessage());
    }

    @Test
    public void testEmptyBlacklist() {
        Set<String> blackListLabels = Set.of();
        ExtractedField response = createField("FirstName!@#", "John");

        ExtractedField result = blacklistAdapter.isLabelValueMatching(blackListLabels, response, "LABELS");

        assertTrue(result.isLabelMatching(), "Empty blacklist should not blacklist label");
        assertEquals("No match found between LABELS and value.", result.getLabelMatchMessage());
    }

    @Test
    public void testEmptyLabelWithEmptyBlacklist() {
        Set<String> blackListLabels = Set.of("FirstName", "");
        ExtractedField response = createField("", "John");

        ExtractedField result = blacklistAdapter.isLabelValueMatching(blackListLabels, response, "LABELS");

        assertFalse(result.isLabelMatching(), "Empty label should be blacklisted");
        assertEquals("LABELS is blacklisted and not allowed.", result.getLabelMatchMessage());
    }

    @Test
    public void testLabelEqualsValueInBlacklist() {
        Set<String> blackListLabels = Set.of("Name", "Email");
        ExtractedField response = createField("Name!@#", "Name$%^");

        ExtractedField result = blacklistAdapter.isLabelValueMatching(blackListLabels, response, "LABELS");

        assertFalse(result.isLabelMatching(), "Label should be blacklisted");
        assertEquals("LABELS is blacklisted and not allowed.", result.getLabelMatchMessage());
    }

    @Test
    public void testLabelWithOnlySpecialCharacters() {
        Set<String> blackListLabels = Set.of("FirstName", "LastName");
        ExtractedField response = createField("!@#$%^", "John");

        ExtractedField result = blacklistAdapter.isLabelValueMatching(blackListLabels, response, "LABELS");

        assertTrue(result.isLabelMatching(), "Only special chars shouldn't be blacklisted");
        assertEquals("No match found between LABELS and value.", result.getLabelMatchMessage());
    }

    @Test
    public void testBlacklistedLabelWithPunctuation() {
        Set<String> blackListLabels = Set.of("FirstName,", "LastName:");
        ExtractedField response = createField("FirstName;!", "John");

        ExtractedField result = blacklistAdapter.isLabelValueMatching(blackListLabels, response, "LABELS");

        assertFalse(result.isLabelMatching(), "Should detect blacklisted label ignoring punctuation");
        assertEquals("LABELS is blacklisted and not allowed.", result.getLabelMatchMessage());
    }



    private ExtractedField createField(String section, String label, String value,
                                       Set<String> whitelistedSections,Set<String> whitelistedLabels) {
        ExtractedField field = new ExtractedField();
        field.setSectionAlias(section);
        field.setLabel(label);
        field.setValue(value);
        field.setLabelMatching(true);
        field.setWhitelistedLabels(whitelistedLabels);
        return field;
    }

    @Test
    void testExactLabelMatchInWhitelist() {
        ExtractedField field = createField("SectionA", "Patient Name", "John",
                Set.of("SectionA"), Set.of("Patient Name"));

        List<ExtractedField> result = adapter.filter(List.of(field));
        ExtractedField processed = result.get(0);

        assertTrue(processed.isLabelMatching(), "Label should be matched because it is whitelisted");
        assertTrue(processed.getLabelMatchMessage().contains("whitelisted"), "Message should mention whitelist");
    }

    @Test
    void testPartialMatchInWhitelist() {
        ExtractedField field = createField("SectionA", "Patient Name", "John",
                Set.of("Patient"), Set.of("Name"));

        List<ExtractedField> result = adapter.filter(List.of(field));
        ExtractedField processed = result.get(0);

        assertTrue(processed.isLabelMatching(), "Label should be matched partially by whitelist keyword");
        assertEquals("LABELS contains a whitelisted keyword.", processed.getLabelMatchMessage());
    }

    @Test
    void testValueOnlyMatchInWhitelist() {
        ExtractedField field = createField("SectionA", "ID", "DOB",
                Set.of(), Set.of("dob"));

        List<ExtractedField> result = adapter.filter(List.of(field));
        ExtractedField processed = result.get(0);

        assertTrue(processed.isLabelMatching(), "Value should be matched because it is whitelisted");
        assertEquals("LABELS value is explicitly whitelisted.", processed.getLabelMatchMessage());
    }

    @Test
    void testNoMatchFound() {
        ExtractedField field = createField("Authorization", "ID", "12345",
                Set.of("Patient"), Set.of("Name"));

        List<ExtractedField> result = adapter.filter(List.of(field));
        ExtractedField processed = result.get(0);

        assertFalse(processed.isLabelMatching(), "No whitelist match should result in false");
        assertEquals("No match found between LABELS and whitelist.", processed.getLabelMatchMessage());
    }

    @Test
    void testEmptyWhitelist() {
        ExtractedField field = createField("SectionA", "Address1", "123 Street",
                Set.of(), Set.of());

        List<ExtractedField> result = adapter.filter(List.of(field));
        ExtractedField processed = result.get(0);

        assertFalse(processed.isLabelMatching(), "Empty whitelist should not match anything");
    }


    @Test
    void testEmptyLabel() {
        ExtractedField field = createField("SectionA", "", "123 Street",
                Set.of(), Set.of("Address"));

        List<ExtractedField> result = adapter.filter(List.of(field));
        ExtractedField processed = result.get(0);

        assertFalse(processed.isLabelMatching(), "Empty label should not match anything");
    }
}
