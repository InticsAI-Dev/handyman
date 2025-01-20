package in.handyman.raven.util;

import org.jasypt.util.text.AES256TextEncryptor;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

class UniqueIDTest {

    @Test
    void getId() {
        IntStream.range(1, 10).forEach(value -> System.out.println(System.nanoTime() + "   " + UniqueID.getId()));
    }

    @Test
    void decryptPropertyByJasypt() {
        String encryptedValue = "ENC(caIO4QK0gccohEZKU/VkKanKbFG8ZK1Zk36g1PfCQVrvGvOXm0rvU0NiATQKwd)";
        String encryptionPassword = "508251fcdde9d040b9b16";

        final AES256TextEncryptor aesEncryptor = new AES256TextEncryptor();
        aesEncryptor.setPassword(encryptionPassword);
        try {
            if (encryptedValue.startsWith("ENC(") && encryptedValue.endsWith(")")) {
                String encryptedText = encryptedValue.substring(4, encryptedValue.length() - 1);
                System.out.println(aesEncryptor.decrypt(encryptedText));
            }
        } catch (Exception e) {
            System.out.println(encryptedValue);
        }
    }
}
