package in.handyman.raven.util;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import in.handyman.raven.core.encryption.InticsDataEncryptionApi;
import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.core.encryption.impl.ProtegrityApiEncryptionImpl;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class InticsIntegrityTest {

    private InticsDataEncryptionApi mockEncryptionImpl;
    private InticsIntegrity inticsIntegrity;
    private static final String ENC_URL = "http://localhost:8190/vulcan/api/encryption/encrypt-1";
    private static final String DEC_URL = "http://localhost:8190/vulcan/api/encryption/decrypt-1";

    private ActionExecutionAudit createAudit(boolean retryActivated, String retryCount, String retryIntervalSecs, String retryStatusCodes, String timeoutSecs) {
        ActionExecutionAudit audit = new ActionExecutionAudit();
        Map<String, String> ctx = new HashMap<>();
        ctx.put("protegrity.api.retry.activated", String.valueOf(retryActivated));
        ctx.put("protegrity.api.retry.count", retryCount);
        ctx.put("protegrity.api.retry.interval.secs", retryIntervalSecs);
        ctx.put("protegrity.api.retry.status.codes", retryStatusCodes);
        ctx.put("protegrity.timeout.seconds", timeoutSecs);
        audit.setContext(ctx);
        audit.setActionId(1L);
        audit.setRootPipelineId(1L);
        return audit;
    }

    private List<EncryptionRequestClass> createRequests() {
        return Arrays.asList(
                new EncryptionRequestClass("AES256", "15, patti street", "key123"),
                new EncryptionRequestClass("AES256", "15, T Nagar", "key456")
        );
    }

    @BeforeEach
    void setUp() {
        mockEncryptionImpl = Mockito.mock(InticsDataEncryptionApi.class);
        inticsIntegrity = new InticsIntegrity(mockEncryptionImpl);
    }

    @Test
    void testProtegrityEncryptionApi(){
        String encryptionUrl = "http://localhost:8190/vulcan/api/encryption/encrypt";
        String decryptionUrl = "http://localhost:8190/vulcan/api/encryption/decrypt";
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setContext(Collections.singletonMap("protegrity.timeout.seconds", "10"));
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.setRootPipelineId(1L);
        // Create an instance of ProtegrityApiEncryptionImpl
        // with the necessary parameters
        List<EncryptionRequestClass> requestList = Arrays.asList(
                new EncryptionRequestClass("AES256", "{\n" +
                        "  \"member_address_line1\": \"15, patti street\"\n" +
                        "}", "key123"),
                new EncryptionRequestClass("AES256", "{\n" +
                        "  \"member_address_line1\": \"15, T Nagar\"\n" +
                        "}", "key456")
        );
        ProtegrityApiEncryptionImpl protegrityApiEncryption=new ProtegrityApiEncryptionImpl(encryptionUrl,decryptionUrl, actionExecutionAudit, log);
        String encryptedString = protegrityApiEncryption.encrypt("test-encryption","AES256","COPRO_REQUEST");
    }


    @Test
    void testProtegrityEncryptionSingleRetry(){

        int randomPortEnc = ThreadLocalRandom.current().nextInt(10000, 100001);

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("protegrity.timeout.seconds", "10");
        contextMap.put("protegrity.api.retry.activated", "true");
        contextMap.put("protegrity.api.retry.count", "3");
        contextMap.put("protegrity.api.retry.interval.secs", "1");
        contextMap.put("protegrity.api.retry.status.codes", "400,408,429,500,502,503,504");

        actionExecutionAudit.setContext(contextMap);

        actionExecutionAudit.setActionId((long) randomPortEnc);
        actionExecutionAudit.setRootPipelineId((long) randomPortEnc);
        actionExecutionAudit.setActionName("TestProtegrityEncryptionSingleRetry");
        // Create an instance of ProtegrityApiEncryptionImpl
        // with the necessary parameters
        List<EncryptionRequestClass> requestList = Arrays.asList(
                new EncryptionRequestClass("AES256", "15, patti street", "key123"),
                new EncryptionRequestClass("AES256", "15, T Nagar", "key456")
        );
        ProtegrityApiEncryptionImpl protegrityApiEncryption=new ProtegrityApiEncryptionImpl(ENC_URL,DEC_URL, actionExecutionAudit, log);
        String encryptedString = protegrityApiEncryption.encrypt(requestList.get(0).getValue(), requestList.get(0).getPolicy(), requestList.get(0).getKey());
        System.out.println(randomPortEnc);
        System.out.println(encryptedString);
    }

    @Test
    void testProtegrityEncryptionApiListRetry(){

        int randomPortEnc = ThreadLocalRandom.current().nextInt(10000, 100001);

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("protegrity.timeout.seconds", "10");
        contextMap.put("protegrity.api.retry.activated", "true");
        contextMap.put("protegrity.api.retry.count", "2");
        contextMap.put("protegrity.api.retry.interval.secs", "3");
        contextMap.put("protegrity.api.retry.status.codes", "404,400,408,429,500,502,503,504");

        actionExecutionAudit.setContext(contextMap);

        actionExecutionAudit.setActionId((long) randomPortEnc);
        actionExecutionAudit.setRootPipelineId((long) randomPortEnc);
        // Create an instance of ProtegrityApiEncryptionImpl
        // with the necessary parameters
        List<EncryptionRequestClass> requestList = Arrays.asList(
                new EncryptionRequestClass("AES256", "15, patti street", "key123"),
                new EncryptionRequestClass("AES256", "15, T Nagar", "key456")
        );
        ProtegrityApiEncryptionImpl protegrityApiEncryption=new ProtegrityApiEncryptionImpl(ENC_URL,DEC_URL, actionExecutionAudit, log);
        List<EncryptionRequestClass> encryptedString = protegrityApiEncryption.encrypt(requestList);
        System.out.println(randomPortEnc);
        System.out.println(encryptedString);
    }


    @Test
    void testProtegrityEncryptionApiRetryForEmptyResponse(){

        int randomPortEnc = ThreadLocalRandom.current().nextInt(10000, 100001);

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("protegrity.timeout.seconds", "10");
        contextMap.put("protegrity.api.retry.activated", "true");
        contextMap.put("protegrity.api.retry.count", "3");
        contextMap.put("protegrity.api.retry.interval.secs", "1");
        contextMap.put("protegrity.api.retry.status.codes", "404,400,408,429,500,502,503,504");

        actionExecutionAudit.setContext(contextMap);

        actionExecutionAudit.setActionId((long) randomPortEnc);
        actionExecutionAudit.setRootPipelineId((long) randomPortEnc);
        // Create an instance of ProtegrityApiEncryptionImpl
        // with the necessary parameters
        List<EncryptionRequestClass> requestList = Arrays.asList(
                new EncryptionRequestClass("AES256", "15, patti street", "key123"),
                new EncryptionRequestClass("AES256", "15, T Nagar", "key456")
        );
        ProtegrityApiEncryptionImpl protegrityApiEncryption=new ProtegrityApiEncryptionImpl(ENC_URL,DEC_URL, actionExecutionAudit, log);
        List<EncryptionRequestClass> encryptedString = protegrityApiEncryption.encrypt(requestList);
        System.out.println(randomPortEnc);
        System.out.println(encryptedString);
    }

    @Test
    void testRetryEnabled_AllDefaults() {
        ActionExecutionAudit audit = createAudit(true, "3", "1", "404,500,503", "10");
        List<EncryptionRequestClass> requests = createRequests();
        ProtegrityApiEncryptionImpl api = new ProtegrityApiEncryptionImpl(ENC_URL, DEC_URL, audit, log);

        List<EncryptionRequestClass> result = api.encrypt(requests);
        assertNotNull(result);
    }

    @Test
    void testRetryDisabled_NoRetry() {
        ActionExecutionAudit audit = createAudit(false, "3", "1", "500,503", "10");
        List<EncryptionRequestClass> requests = createRequests();
        ProtegrityApiEncryptionImpl api = new ProtegrityApiEncryptionImpl(ENC_URL, DEC_URL, audit, log);

        List<EncryptionRequestClass> result = api.encrypt(requests);
        // Verify no retry attempts triggered
        // (could check internal audit count or logs)
        assertNotNull(result);
    }

    @Test
    void testRetryEnabledWithZeroCount() {
        ActionExecutionAudit audit = createAudit(true, "0", "1", "500,503", "10");
        List<EncryptionRequestClass> requests = createRequests();
        ProtegrityApiEncryptionImpl api = new ProtegrityApiEncryptionImpl(ENC_URL, DEC_URL, audit, log);

        List<EncryptionRequestClass> result = api.encrypt(requests);
        assertNotNull(result);
    }

    @Test
    void testRetryWithInvalidStatusCodes() {
        ActionExecutionAudit audit = createAudit(true, "3", "1", "abc,xyz,500", "10");
        List<EncryptionRequestClass> requests = createRequests();
        ProtegrityApiEncryptionImpl api = new ProtegrityApiEncryptionImpl(ENC_URL, DEC_URL, audit, log);

        List<EncryptionRequestClass> result = api.encrypt(requests);
        assertNotNull(result);
    }

    @Test
    void testMissingRetryContextKeys() {
        ActionExecutionAudit audit = new ActionExecutionAudit();
        audit.setContext(Collections.emptyMap()); // nothing set
        List<EncryptionRequestClass> requests = createRequests();
        ProtegrityApiEncryptionImpl api = new ProtegrityApiEncryptionImpl(ENC_URL, DEC_URL, audit, log);

        List<EncryptionRequestClass> result = api.encrypt(requests);
        assertNotNull(result);
    }

    @Test
    void testTimeoutTooShort() {
        ActionExecutionAudit audit = createAudit(true, "3", "1", "500,503", "1");
        List<EncryptionRequestClass> requests = createRequests();
        ProtegrityApiEncryptionImpl api = new ProtegrityApiEncryptionImpl(ENC_URL, DEC_URL, audit, log);

        List<EncryptionRequestClass> result = api.encrypt(requests);
        assertNotNull(result);
    }
    @Test
    void testHighRetryCount() {
        ActionExecutionAudit audit = createAudit(true, "10", "1", "500,503", "10");
        List<EncryptionRequestClass> requests = createRequests();
        ProtegrityApiEncryptionImpl api = new ProtegrityApiEncryptionImpl(ENC_URL, DEC_URL, audit, log);

        List<EncryptionRequestClass> result = api.encrypt(requests);
        assertNotNull(result);
    }

    @Test
    void testRetryWithLongInterval() {
        ActionExecutionAudit audit = createAudit(true, "2", "5", "500,503", "15");
        List<EncryptionRequestClass> requests = createRequests();
        ProtegrityApiEncryptionImpl api = new ProtegrityApiEncryptionImpl(ENC_URL, DEC_URL, audit, log);

        List<EncryptionRequestClass> result = api.encrypt(requests);
        assertNotNull(result);
    }


    @Test
    void testProtegrityDecryptionApi(){
        String encryptionUrl = "http://localhost:8199/vulcan/api/encryption/encrypt";
        String decryptionUrl = "http://localhost:8199/vulcan/api/encryption/decrypt";
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setContext(Collections.singletonMap("protegrity.timeout.seconds", "10"));
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

