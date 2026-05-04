package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lib.model.deepSiftSearch.DeepSiftSearchConsumerProcess;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class DeepSiftSearchConsumerProcessTest {

    /**
     * Helper method to invoke private method:
     * isInsideAddress(String text, String keyword)
     */
    private boolean invokeIsInsideAddress(String text, String keyword) throws Exception {

        DeepSiftSearchConsumerProcess process =
                DeepSiftSearchConsumerProcess.builder()
                        .log(null)
                        .marker(null)
                        .action(null)
                        .pageContentMinLength(10)
                        .build();

        Method method = DeepSiftSearchConsumerProcess.class
                .getDeclaredMethod("isInsideAddress", String.class, String.class);

        method.setAccessible(true);

        return (boolean) method.invoke(process, text, keyword);
    }

    // ==================================================
    // VALID ADDRESS TEST CASES
    // ==================================================

    @Test
    public void testStreetAddress_ST() throws Exception {
        assertTrue(invokeIsInsideAddress(
                "1400 16th St, San Francisco, CA 94103",
                "ST"));
    }

    @Test
    public void testRoadAddress_RD() throws Exception {
        assertTrue(invokeIsInsideAddress(
                "123 Main Rd, Dallas TX 75001",
                "RD"));
    }

    @Test
    public void testDriveAddress_IA() throws Exception {
        assertTrue(invokeIsInsideAddress(
                "6445 Corporate Dr., Johnston, IA 50131",
                "IA"));
    }

    @Test
    public void testWalkerStreet() throws Exception {
        assertTrue(invokeIsInsideAddress(
                "7901 Walker St",
                "Walker"));
    }

    // ==================================================
    // NON ADDRESS CASES
    // ==================================================

    @Test
    public void testRollingWalker_ShouldBeFalse() throws Exception {
        assertFalse(invokeIsInsideAddress(
                "Assistive Devices: Platform Rolling Walker, Rolling Walker",
                "Walker"));
    }

    @Test
    public void testTelemetry_ShouldBeFalse() throws Exception {
        assertFalse(invokeIsInsideAddress(
                "Telemetry service required",
                "Telemetry"));
    }

    @Test
    public void testInpatient_ShouldBeFalse() throws Exception {
        assertFalse(invokeIsInsideAddress(
                "Planned inpatient service",
                "inpatient"));
    }

    @Test
    public void testUrgent_ShouldBeFalse() throws Exception {
        assertFalse(invokeIsInsideAddress(
                "Urgent authorization request",
                "urgent"));
    }

    // ==================================================
    // KEYWORD NOT FOUND
    // ==================================================

    @Test
    public void testKeywordNotPresent() throws Exception {
        assertFalse(invokeIsInsideAddress(
                "1400 16th St San Francisco",
                "Walker"));
    }

    // ==================================================
    // CASE INSENSITIVE
    // ==================================================

    @Test
    public void testCaseInsensitive() throws Exception {
        assertTrue(invokeIsInsideAddress(
                "1400 16TH st SAN FRANCISCO ca 94103",
                "St"));
    }

    // ==================================================
    // EDGE CASES
    // ==================================================

    @Test
    public void testOnlyStreet_NoNumber() throws Exception {
        assertFalse(invokeIsInsideAddress(
                "Main Street",
                "Street"));
    }

    @Test
    public void testOnlyNumber() throws Exception {
        assertFalse(invokeIsInsideAddress(
                "12345",
                "12345"));
    }

    @Test
    public void testHospitalName_StMary() throws Exception {
        assertFalse(invokeIsInsideAddress(
                "St Mary Hospital",
                "St"));
    }

    @Test
    public void testEmptyText() throws Exception {
        assertFalse(invokeIsInsideAddress(
                "",
                "ST"));
    }

    // ==================================================
    // MULTILINE ADDRESS
    // ==================================================

    @Test
    public void testMultilineAddress() throws Exception {
        assertTrue(invokeIsInsideAddress(
                "Labcorp Genetics Inc\n1400 16th St\nSan Francisco CA 94103",
                "ST"));
    }
}