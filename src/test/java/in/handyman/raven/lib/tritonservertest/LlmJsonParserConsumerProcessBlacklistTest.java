package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lib.adapters.selections.BlacklistFilterAdapter;
import in.handyman.raven.lib.adapters.selections.ExtractedField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LlmJsonParserConsumerProcessBlacklistTest {

    private BlacklistFilterAdapter blacklistAdapter;

    @BeforeEach
    public void setUp() {
        blacklistAdapter = new BlacklistFilterAdapter();
    }

    // Helper method to create ExtractedField
    private ExtractedField createField(String label, String value) {
        ExtractedField field = new ExtractedField();
        field.setLabel(label);
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
}
