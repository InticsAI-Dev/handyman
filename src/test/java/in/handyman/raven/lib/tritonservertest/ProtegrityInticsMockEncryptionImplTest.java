package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.core.encryption.impl.ProtegrityInticsMockEncryptionImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class ProtegrityInticsMockEncryptionImplTest {

    private final Logger logger = Mockito.mock(Logger.class);

    private ProtegrityInticsMockEncryptionImpl getInstance() {
        return new ProtegrityInticsMockEncryptionImpl(logger);
    }

    @Test
    void testBirthDateEncryptDecrypt() throws Exception {
        ProtegrityInticsMockEncryptionImpl impl = getInstance();

        String input = "1965-08-23";

        String encrypted = impl.encrypt(
                input,
                ProtegrityInticsMockEncryptionImpl.BIRTHDATE_DATETIME_LP,
                "member_dob"
        );

        String decrypted = impl.decrypt(
                encrypted,
                ProtegrityInticsMockEncryptionImpl.BIRTHDATE_DATETIME_LP,
                "member_dob"
        );

        assertEquals(input, decrypted, "Decrypted value should match original input");
    }

    @Test
    void testBlankInput() throws Exception {
        ProtegrityInticsMockEncryptionImpl impl = getInstance();

        String result = impl.encrypt(
                "",
                ProtegrityInticsMockEncryptionImpl.BIRTHDATE_DATETIME_LP,
                "member_dob"
        );

        assertEquals("", result);
    }

    @Test
    void testLeapYearDate() throws Exception {
        ProtegrityInticsMockEncryptionImpl impl = getInstance();

        String input = "2020-02-29";

        String encrypted = impl.encrypt(
                input,
                ProtegrityInticsMockEncryptionImpl.BIRTHDATE_DATETIME_LP,
                "member_dob"
        );

        String decrypted = impl.decrypt(
                encrypted,
                ProtegrityInticsMockEncryptionImpl.BIRTHDATE_DATETIME_LP,
                "member_dob"
        );

        assertEquals(input, decrypted, "Leap year date should remain consistent after decrypt");
    }
}