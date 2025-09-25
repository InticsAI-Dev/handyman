package in.handyman.raven.core.encryption.impl;

import in.handyman.raven.core.encryption.InticsDataEncryptionApi;
import in.handyman.raven.exception.HandymanException;
import org.slf4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            aesKey = keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AES key", e);
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
                return encrypt ? aesEncrypt(input) : aesDecrypt(input);
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
        int len = input.length();
        // For every 10 characters, reduce shift by 1 (down to min 1)
        int shift = 10 - (len / 10);
        return Math.max(1, shift);
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
    private String aesEncrypt(String input) throws HandymanException {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encryptedBytes = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new HandymanException("AES Encryption failed", e);
        }
    }

    private String aesDecrypt(String encrypted) throws HandymanException {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encrypted);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new HandymanException("AES Decryption failed", e);
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
