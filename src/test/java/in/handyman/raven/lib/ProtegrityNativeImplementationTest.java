package in.handyman.raven.lib;

import com.protegrity.ap.java.Protector;
import com.protegrity.ap.java.ProtectorException;
import com.protegrity.ap.java.SessionObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ProtegrityNativeImplementationTest {

    private final String policyUser = "BIRTHDATE_DATETIME_LP";  // must match ESA config
    private final String dataElement = "BIRTHDATE_DATETIME_LP";

    @Test
    void performEncryptionDecryption() {
        String inputData = "03-03-2025";

        String[] input = {inputData};
        String[] protectedOutput = new String[1];
        String[] unprotectedOutput = new String[1];

        try {
            // Set application identity before getting protector
            System.setProperty("protegrity.application.name", "");
            System.setProperty("protegrity.application.user", "");

            Protector protector = Protector.getProtector();
            SessionObject session = protector.createSession(policyUser);

            System.out.println("Java SDK Version: {}" + protector.getVersion());

            boolean encryptionSuccess = protector.protect(session, dataElement, input, protectedOutput);
            if (!encryptionSuccess) {
                String error = protector.getLastError(session);
                System.out.println("Encryption failed: {}" + error);
            } else {
                System.out.println("Encrypted data: {}" + protectedOutput[0]);
            }

            // Decrypt
            boolean decryptionSuccess = protector.unprotect(session, dataElement, protectedOutput, unprotectedOutput);
            if (!decryptionSuccess) {
                String error = protector.getLastError(session);
                System.out.println("Decryption failed: {}" + error);
            } else {
                System.out.println("Decrypted data: {}" + unprotectedOutput[0]);
            }

        } catch (ProtectorException ex) {
            System.out.println("Protegrity error occurred" + ex);
        }
    }
}

