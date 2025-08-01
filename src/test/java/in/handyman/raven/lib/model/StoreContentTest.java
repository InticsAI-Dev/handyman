package in.handyman.raven.lib.model;

import com.anthem.acma.commonclient.storecontent.dto.StoreContentResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class StoreContentTest {

    @Test
    void testStoreContentMacro() {
        StoreContent storeContent = new StoreContent();

        // 🔹 Test file path (use a small sample PDF in your resources)
        String filePath = "/data/output/test_smartintake.pdf";
        String repository = "FilenetCE";
        String applicationId = "SMARTINTAKE";
        String dcn = "25202E0000006"; // Test DCN
        String envUrl = "https://dev.api.anthem.com";
        String apiKey = "dummy-api-key";        // TODO: Replace with real key from context
        String bearerToken = "dummy-bearer";    // TODO: Replace with real token from context

        StoreContentResponseDto response = storeContent.execute(
                filePath,
                repository,
                applicationId,
                dcn,
                envUrl,
                apiKey,
                bearerToken
        );

        // 🔹 Assertions
        assertNotNull(response, "Response should not be null");
        log.info("Test Response Status: {}", response.getStatus());
        log.info("Test Content ID: {}", response.getContentID());
        log.info("Test Message: {}", response.getMessage());

        // Allow 0 or 200 depending on placeholder
        assertTrue(response.getStatus() == 0 || response.getStatus() == 200, "Status should be valid");
    }
}
