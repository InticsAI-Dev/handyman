package in.handyman.raven.util;

import in.handyman.raven.core.encryption.InticsDataEncryptionApi;
import in.handyman.raven.core.encryption.impl.ProtegrityApiEncryptionImpl;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.LoggerFactory;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class InticsIntegrityTest {

    private InticsDataEncryptionApi mockEncryptionImpl;
    private InticsIntegrity inticsIntegrity;

    @BeforeEach
    void setUp() {
        mockEncryptionImpl = Mockito.mock(InticsDataEncryptionApi.class);
        inticsIntegrity = new InticsIntegrity(mockEncryptionImpl);
    }

    @Test
    void testProtegrityEncryptionApi(){
        String encryptionUrl = "http://localhost:8199/vulcan/api/encryption/encrypt";
        String decryptionUrl = "http://localhost:8199/vulcan/api/encryption/decrypt";
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setContext(Collections.singletonMap("protegrity.timeout", "10"));
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.setRootPipelineId(1L);


        // Create an instance of ProtegrityApiEncryptionImpl
        // with the necessary parameters
        ProtegrityApiEncryptionImpl protegrityApiEncryption=new ProtegrityApiEncryptionImpl(encryptionUrl,decryptionUrl, actionExecutionAudit, log);
        String encryptedString = protegrityApiEncryption.encrypt("test-encryption","AES256","COPRO_REQUEST");
        System.out.println(encryptedString);

    }


    @Test
    void testProtegrityDecryptionApi(){
        String encryptionUrl = "http://localhost:8199/vulcan/api/encryption/encrypt";
        String decryptionUrl = "http://localhost:8199/vulcan/api/encryption/decrypt";
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setContext(Collections.singletonMap("protegrity.timeout", "10"));
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.setRootPipelineId(1L);

        // Create an instance of ProtegrityApiEncryptionImpl
        // with the necessary parameters
        ProtegrityApiEncryptionImpl protegrityApiEncryption=new ProtegrityApiEncryptionImpl(encryptionUrl,decryptionUrl, actionExecutionAudit, log);
        String encryptedString = protegrityApiEncryption.decrypt("test-encryption","AES256","COPRO_REQUEST");
        System.out.println(encryptedString);

    }

    @Test
    void testEncryptDelegation() throws HandymanException {
        when(mockEncryptionImpl.encrypt("plain", "AES265", "sorKey"))
                .thenReturn("encrypted-value");

        String result = inticsIntegrity.encrypt("plain", "AES265", "sorKey");
        System.out.println(result);
        assertEquals("encrypted-value", result);
        verify(mockEncryptionImpl, times(1)).encrypt("plain", "AES265", "sorKey");
    }

    @Test
    void testDecryptDelegation() throws HandymanException {
        when(mockEncryptionImpl.decrypt("encrypted-value", "policy", "sorKey"))
                .thenReturn("plain");

        String result = inticsIntegrity.decrypt("encrypted-value", "policy", "sorKey");

        assertEquals("plain", result);
        verify(mockEncryptionImpl, times(1)).decrypt("encrypted-value", "policy", "sorKey");
    }

    @Test
    void testGetEncryptionMethodDelegation() {
        when(mockEncryptionImpl.getEncryptionMethod()).thenReturn("PROTEGRITY_API_ENC");

        String method = inticsIntegrity.getEncryptionMethod();

        assertEquals("PROTEGRITY_API_ENC", method);
        verify(mockEncryptionImpl, times(1)).getEncryptionMethod();
    }

    @Test
    void testEncryptThrowsException() throws HandymanException {
        when(mockEncryptionImpl.encrypt(any(), any(), any()))
                .thenThrow(new HandymanException("Encryption failed"));

        assertThrows(HandymanException.class, () ->
                inticsIntegrity.encrypt("input", "policy", "sorItem"));
    }

    @Test
    void testDecryptThrowsException() throws HandymanException {
        when(mockEncryptionImpl.decrypt(any(), any(), any()))
                .thenThrow(new HandymanException("Decryption failed"));

        assertThrows(HandymanException.class, () ->
                inticsIntegrity.decrypt("input", "policy", "sorItem"));
    }
}

