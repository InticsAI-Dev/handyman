package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lib.LlmJsonParserConsumerProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import in.handyman.raven.lib.LlmJsonParserConsumerProcess.LlmJsonKvpKryptonParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LlmJsonParserConsumerProcessBlacklistTest {

    private LlmJsonParserConsumerProcess processor;

    @BeforeEach
    public void setUp() {
        // Initialize with null dependencies for simplicity; mock if needed
        processor = new LlmJsonParserConsumerProcess(null, null, null, null);
    }

    // Helper method to create LlmJsonKvpKryptonParser instance
    private LlmJsonKvpKryptonParser createParser(String label, String value) {
        return LlmJsonKvpKryptonParser.builder()
                .label(label)
                .value(value)
                .build();
    }

    @Test
    public void testExactBlacklistedLabel() {
        // Test Case 1: Extracted label exactly matches a blacklisted keyword
        List<String> blackListLabels = Arrays.asList("FirstName", "LastName", "Email");
        LlmJsonKvpKryptonParser response = createParser("FirstName!@#", "John");
        LlmJsonKvpKryptonParser result = processor.isLabelValueMatching(blackListLabels, response);

        assertFalse(result.isLabelMatching(), "Label should be blacklisted");
        assertEquals("Label is blacklisted and not allowed.", result.getLabelMatchMessage());
        assertEquals("FirstName!@#", result.getLabel(), "Label should remain unchanged");
    }

    @Test
    public void testBlacklistedLabelWithSpecialCharacters() {
        // Test Case 2: Blacklisted keyword and extracted label have different special characters
        List<String> blackListLabels = Arrays.asList("FirstName!", "LastName#", "Email@");
        LlmJsonKvpKryptonParser response = createParser("FirstName$%^", "John");
        LlmJsonKvpKryptonParser result = processor.isLabelValueMatching(blackListLabels, response);

        assertFalse(result.isLabelMatching(), "Label should be blacklisted after removing special characters");
        assertEquals("Label is blacklisted and not allowed.", result.getLabelMatchMessage());
        assertEquals("FirstName$%^", result.getLabel(), "Label should remain unchanged");
    }

    @Test
    public void testBlacklistedLabelAfterValueRemoval() {
        // Test Case 3: Label contains value, and remaining part is blacklisted
        List<String> blackListLabels = Arrays.asList("Customer", "OtherLabel");
        LlmJsonKvpKryptonParser response = createParser("CustomerName!@#", "Name$%^");
        LlmJsonKvpKryptonParser result = processor.isLabelValueMatching(blackListLabels, response);

        assertFalse(result.isLabelMatching(), "Updated label should be blacklisted");
        assertEquals("Updated label is blacklisted after removing value.", result.getLabelMatchMessage());
        assertEquals("Customer", result.getLabel(), "Label should be updated to 'Customer'");
    }

    @Test
    public void testNonBlacklistedLabel() {
        // Test Case 4: Extracted label is not in blacklist
        List<String> blackListLabels = Arrays.asList("FirstName", "LastName");
        LlmJsonKvpKryptonParser response = createParser("Address!@#", "123 Main St");
        LlmJsonKvpKryptonParser result = processor.isLabelValueMatching(blackListLabels, response);

        assertTrue(result.isLabelMatching(), "Label should not be blacklisted");
        assertEquals("No match found between label and value.", result.getLabelMatchMessage());
        assertEquals("Address!@#", result.getLabel(), "Label should remain unchanged");
    }

    @Test
    public void testEmptyBlacklist() {
        // Test Case 5: Empty blacklist
        List<String> blackListLabels = Collections.emptyList();
        LlmJsonKvpKryptonParser response = createParser("FirstName!@#", "John");
        LlmJsonKvpKryptonParser result = processor.isLabelValueMatching(blackListLabels, response);

        assertTrue(result.isLabelMatching(), "No blacklist should result in true");
        assertEquals("No match found between label and value.", result.getLabelMatchMessage());
        assertEquals("FirstName!@#", result.getLabel(), "Label should remain unchanged");
    }

    @Test
    public void testEmptyLabelWithEmptyBlacklist() {
        // Test Case 6: Empty label with empty string in blacklist
        List<String> blackListLabels = Arrays.asList("FirstName", "");
        LlmJsonKvpKryptonParser response = createParser("", "John");
        LlmJsonKvpKryptonParser result = processor.isLabelValueMatching(blackListLabels, response);

        assertFalse(result.isLabelMatching(), "Empty label should be blacklisted");
        assertEquals("Label is blacklisted and not allowed.", result.getLabelMatchMessage());
        assertEquals("", result.getLabel(), "Label should remain empty");
    }

    @Test
    public void testLabelEqualsValueInBlacklist() {
        // Test Case 7: Label equals value and is blacklisted
        List<String> blackListLabels = Arrays.asList("Name", "Email");
        LlmJsonKvpKryptonParser response = createParser("Name!@#", "Name$%^");
        LlmJsonKvpKryptonParser result = processor.isLabelValueMatching(blackListLabels, response);

        assertFalse(result.isLabelMatching(), "Label should be blacklisted despite matching value");
        assertEquals("Label is blacklisted and not allowed.", result.getLabelMatchMessage());
        assertEquals("Name!@#", result.getLabel(), "Label should remain unchanged");
    }

    @Test
    public void testLabelWithOnlySpecialCharacters() {
        // Test Case 8: Label contains only special characters, not in blacklist
        List<String> blackListLabels = Arrays.asList("FirstName", "LastName");
        LlmJsonKvpKryptonParser response = createParser("!@#$%^", "John");
        LlmJsonKvpKryptonParser result = processor.isLabelValueMatching(blackListLabels, response);

        assertTrue(result.isLabelMatching(), "Label with only special characters should not be blacklisted");
        assertEquals("No match found between label and value.", result.getLabelMatchMessage());
        assertEquals("!@#$%^", result.getLabel(), "Label should remain unchanged");
    }

    @Test
    public void testBlacklistedLabelWithPunctuation() {
        // Test Case 9: Blacklisted label with punctuation
        List<String> blackListLabels = Arrays.asList("FirstName,", "LastName:");
        LlmJsonKvpKryptonParser response = createParser("FirstName;!", "John");
        LlmJsonKvpKryptonParser result = processor.isLabelValueMatching(blackListLabels, response);

        assertFalse(result.isLabelMatching(), "Label should be blacklisted after removing punctuation");
        assertEquals("Label is blacklisted and not allowed.", result.getLabelMatchMessage());
        assertEquals("FirstName;!", result.getLabel(), "Label should remain unchanged");
    }
}