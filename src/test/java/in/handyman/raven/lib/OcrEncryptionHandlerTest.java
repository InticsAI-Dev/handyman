package in.handyman.raven.lib;

import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.services.sor.transform.OcrTextComparisonInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class OcrEncryptionHandlerTest {

    @Mock
    private Logger log;
    @Mock
    private Marker marker;
    @Mock
    private InticsIntegrity encryption;

    private OcrEncryptionHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new OcrEncryptionHandler(log, marker, encryption);
    }

    @Test
    void performDecryption_shouldDecryptPageContent() {
        // Arrange
        OcrTextComparisonInput input = new OcrTextComparisonInput();
        input.setOcrFieldId(123);
        input.setExtractedText("enc_val");
        input.setEncrypted(true);
        input.setLineItemType("single");
        List<OcrTextComparisonInput> records = Collections.singletonList(input);

        EncryptionRequestClass response = EncryptionRequestClass.builder()
                .key("123")
                .value("dec_val")
                .build();
        when(encryption.decrypt(anyList())).thenReturn(Collections.singletonList(response));

        // Act
        handler.performDecryption(records, false, true); // deepSiftOutputActivator=true for page content

        // Assert
        assertEquals("dec_val", input.getExtractedText());
        verify(encryption).decrypt(anyList());
    }

    @Test
    void performDecryption_shouldDecryptItemWise() {
        // Arrange
        OcrTextComparisonInput input = new OcrTextComparisonInput();
        input.setOcrFieldId(123);
        input.setAnswer("enc_val");
        input.setEncrypted(true);
        input.setLineItemType("single");
        List<OcrTextComparisonInput> records = Collections.singletonList(input);

        EncryptionRequestClass response = EncryptionRequestClass.builder()
                .key("123")
                .value("dec_val")
                .build();
        when(encryption.decrypt(anyList())).thenReturn(Collections.singletonList(response));

        // Act
        handler.performDecryption(records, true, false); // pipelineEndToEndEncryptionActivator=true for answer

        // Assert
        assertEquals("dec_val", input.getAnswer());
        verify(encryption).decrypt(anyList());
    }

    @Test
    void performDecryption_shouldHandleMultiValue() {
        // Arrange
        OcrTextComparisonInput input = new OcrTextComparisonInput();
        input.setOcrFieldId(100);
        input.setExtractedText("enc1, enc2");
        input.setEncrypted(true);
        input.setLineItemType("multi_value");
        List<OcrTextComparisonInput> records = Collections.singletonList(input);

        List<EncryptionRequestClass> responses = new ArrayList<>();
        responses.add(EncryptionRequestClass.builder().key("100_0").value("dec1").build());
        responses.add(EncryptionRequestClass.builder().key("100_1").value("dec2").build());

        when(encryption.decrypt(anyList())).thenReturn(responses);

        // Act
        handler.performDecryption(records, false, true);

        // Assert
        assertEquals("dec1,dec2", input.getExtractedText());
    }

    @Test
    void performEncryption_shouldEncryptPageContent() {
        // Arrange
        OcrTextComparisonInput input = new OcrTextComparisonInput();
        input.setOcrFieldId(123);
        input.setExtractedText("raw_val");
        input.setEncrypted(true);
        input.setLineItemType("single");
        List<OcrTextComparisonInput> records = Collections.singletonList(input);

        EncryptionRequestClass response = EncryptionRequestClass.builder()
                .key("123")
                .value("enc_val")
                .build();
        when(encryption.encrypt(anyList())).thenReturn(Collections.singletonList(response));

        // Act
        handler.performEncryption(records, false, true);

        // Assert
        assertEquals("enc_val", input.getExtractedText());
        verify(encryption).encrypt(anyList());
    }

}
