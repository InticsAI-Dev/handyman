package in.handyman.raven.core.encryption.impl;

import in.handyman.raven.core.encryption.InticsDataEncryptionApi;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.util.PropertyHandler;
import org.slf4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class ProtegrityInticsMockEncryptionImpl implements InticsDataEncryptionApi {

    public static final String MEMBERNAME_UPPERALPHANUM_LP = "MEMBERNAME_UPPERALPHANUM_LP";
    public static final String MEMBERIDENTIFIERS_UPPERALPHANUM_LP = "MEMBERIDENTIFIERS_UPPERALPHANUM_LP";
    public static final String AES_256 = "AES256";
    public static final String BIRTHDATE_DATETIME_LP = "BIRTHDATE_DATETIME_LP";
    private final Logger logger;

    // AES key for AES256 policy
    private static SecretKey aesKey;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static {
        try {
            aesKey = getSecretKeyFromConfig();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AES key", e);
        }
    }


    private static SecretKey getSecretKeyFromConfig() throws HandymanException {
        try {

            String secretKeyString = PropertyHandler.get("aes.secretKey");

            if (secretKeyString == null || secretKeyString.isEmpty()) {

                throw new HandymanException("AES secret key is not configured in the properties file.");
            }

            byte[] decodedKey = Base64.getDecoder().decode(secretKeyString);


            if (decodedKey == null || decodedKey.length == 0) {

                return new SecretKeySpec(new byte[32], "AES"); // Return an empty 256-bit key
            }

            if (decodedKey.length != 32) {

                throw new HandymanException("Invalid AES-256 key length. Expected 32 bytes (256 bits).");
            }

            return new SecretKeySpec(decodedKey, "AES");

        } catch (Exception e) {

            throw new HandymanException("Error decoding AES secret key from config.", e);
        }
    }
    public ProtegrityInticsMockEncryptionImpl(Logger logger) {
        this.logger = logger;
    }

    // ================== MAIN METHODS ==================

    @Override
    public String encrypt(String inputToken, String encryptionPolicy, String sorItem) throws HandymanException {
        return auditWrapper(inputToken, encryptionPolicy, sorItem, true);
    }

    @Override
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException {
        return auditWrapper(encryptedToken, encryptionPolicy, sorItem, false);
    }

    @Override
    public List<EncryptionRequestClass> encrypt(List<EncryptionRequestClass> requestList) throws HandymanException {
        return auditWrapperList(requestList, true);
    }

    @Override
    public List<EncryptionRequestClass> decrypt(List<EncryptionRequestClass> requestList) throws HandymanException {
        return auditWrapperList(requestList, false);
    }

    @Override
    public String getEncryptionMethod() {
        return "PROTEGRITY_API_ENC";
    }

    // ================== AUDIT WRAPPERS ==================

    private String auditWrapper(String value, String policy, String key, boolean encrypt) throws HandymanException {
        if (value == null || value.isBlank()) {
            logger.warn("{} skipped: value is null/blank for key={}", encrypt ? "Encryption" : "Decryption", key);
            return value;
        }


        String result = tokenizeByPolicy(value, policy, encrypt);

        return result;

    }

    private List<EncryptionRequestClass> auditWrapperList(List<EncryptionRequestClass> requestList, boolean encrypt) throws HandymanException {
        if (requestList == null || requestList.isEmpty()) {
            return Collections.emptyList();
        }


        List<EncryptionRequestClass> result = new ArrayList<>();
        for (EncryptionRequestClass req : requestList) {
            String transformed = tokenizeByPolicy(req.getValue(), req.getPolicy(), encrypt);
            result.add(new EncryptionRequestClass(req.getPolicy(), transformed, req.getKey()));
        }
        String msg = String.format("SUCCESS | batchSize=%d", requestList.size());
        logger.info(msg);

        return result;

    }

    // ================== POLICY LOGIC ==================

    private String tokenizeByPolicy(String input, String policy, boolean encrypt) throws HandymanException {
        int dynamicShift = calculateShift(input);

        switch (policy.toUpperCase()) {
            case MEMBERNAME_UPPERALPHANUM_LP:
                return shiftAlphaNumeric(input, encrypt ? dynamicShift : -dynamicShift);
            case MEMBERIDENTIFIERS_UPPERALPHANUM_LP:
                return shiftMemberIdentifier(input, encrypt ? dynamicShift : -dynamicShift);
            case AES_256:
                return encrypt ? aesEncrypt(input,AES_256) : aesDecrypt(input,AES_256);
            case BIRTHDATE_DATETIME_LP:
                return shiftDate(input, encrypt, dynamicShift);
            default:
                return null;
        }
    }
    private String shiftDate(String input, boolean encrypt, int dynamicShift) throws HandymanException {
        try {
            LocalDate date = LocalDate.parse(input, DATE_FMT);
            if (encrypt) {
                return date.plusYears(100)
                        .plusMonths(dynamicShift)
                        .plusDays(dynamicShift)
                        .format(DATE_FMT);
            } else {
                return date.minusYears(100)
                        .minusMonths(dynamicShift)
                        .minusDays(dynamicShift)
                        .format(DATE_FMT);
            }
        } catch (Exception e) {
            throw new HandymanException("Invalid date format, expected yyyy-MM-dd: " + input, e);
        }
    }

    private int calculateShift(String input) {
        if (input == null || input.isEmpty()) return 3; // fallback default
        // For every 10 characters, reduce shift by 1 (down to min 1)

        return input.length();
    }

    // Names & alphanumeric tokens
    private String shiftAlphaNumeric(String input, int shift) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append((char) ('A' + (c - 'A' + shift + 26) % 26));
            } else if (Character.isLowerCase(c)) {
                sb.append((char) ('a' + (c - 'a' + shift + 26) % 26));
            } else if (Character.isDigit(c)) {
                sb.append((char) ('0' + (c - '0' + shift + 10) % 10));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    private String shiftMemberIdentifier(String input, int shift) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                // Shift uppercase letters A–Z
                sb.append((char) ('A' + (c - 'A' + shift + 26) % 26));
            } else if (Character.isLowerCase(c)) {
                // Shift lowercase letters a–z
                sb.append((char) ('a' + (c - 'a' + shift + 26) % 26));
            } else if (Character.isDigit(c)) {
                // Shift numbers 0–9
                sb.append((char) ('0' + (c - '0' + shift + 10) % 10));
            } else {
                // Leave other symbols as-is
                sb.append(c);
            }
        }
        return sb.toString();
    }
    // Digits only
    private String shiftDigits(String input, int shift) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append((char) ('0' + (c - '0' + shift + 10) % 10));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // AES256
    private String aesEncrypt(String input,String encryptionPolicy) throws HandymanException {
        try {
            if (input.isBlank()) {
                logger.warn("Encryption skipped: Input token is blank.");
                return input;
            }

            logger.debug("Encrypting data for encryptionPolicy: {}", encryptionPolicy);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            byte[] encrypted = cipher.doFinal(input.getBytes());
            String encryptedString = Base64.getEncoder().encodeToString(encrypted);

            return encryptedString;

        } catch (Exception e) {
            logger.error("Error during AES-256 encryption - {}", e.getMessage(), e);
            throw new HandymanException("Error during AES-256 encryption.", e);
        }
    }

    private String aesDecrypt(String encrypted,String encryptionPolicy) throws HandymanException {
        try {
            if (encrypted.isBlank()) {
                logger.warn("Decryption skipped: Input token is blank.");
                return encrypted;
            }

            logger.debug("Decrypting data for encryptionPolicy: {}", encryptionPolicy);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            String decryptedString = new String(decrypted);
            return decryptedString;

        } catch (Exception e) {
            logger.error("Error during AES-256 decryption - {}", e.getMessage(), e);
            throw new HandymanException("Error during AES-256 decryption.", e);
        }
    }

    // Dates in yyyy-MM-dd
    private String shiftDate(String input, boolean encrypt) throws HandymanException {
        try {
            LocalDate date = LocalDate.parse(input, DATE_FMT);
            if (encrypt) {
                return date.plusYears(100).plusMonths(3).plusDays(7).format(DATE_FMT);
            } else {
                return date.minusYears(100).minusMonths(3).minusDays(7).format(DATE_FMT);
            }
        } catch (Exception e) {
            throw new HandymanException("Invalid date format, expected yyyy-MM-dd: " + input, e);
        }
    }
}
