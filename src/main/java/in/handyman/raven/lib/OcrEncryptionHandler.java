package in.handyman.raven.lib;

import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.services.sor.transform.OcrTextComparisonInput;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OcrEncryptionHandler {

    private final Logger log;
    private final Marker aMarker;
    private final InticsIntegrity encryption;
    public static final String AES_256 = "AES256";

    public OcrEncryptionHandler(Logger log, Marker aMarker, InticsIntegrity encryption) {
        this.log = log;
        this.aMarker = aMarker;
        this.encryption = encryption;
    }

    public void performDecryption(List<OcrTextComparisonInput> records,
            boolean pipelineEndToEndEncryptionActivator,
            boolean deepSiftOutputActivator) {
        log.info(aMarker, "Total records to process for decryption: {}", records.size());

        if (pipelineEndToEndEncryptionActivator) {
            log.info(aMarker, "Starting Decryption process for item-wise encryption");
            decryptAndUpdate(records, false);
        } else {
            log.info(aMarker, "Skipping Decryption as itemWiseEncryption is false");
        }

        if (deepSiftOutputActivator) {
            log.info(aMarker, "Starting Decryption process for deep sift text encryption");
            decryptAndUpdate(records, true);
        } else {
            log.info(aMarker, "Skipping Decryption as deep sift text is false");
        }
    }

    private void decryptAndUpdate(List<OcrTextComparisonInput> records, boolean isPageContent) {
        log.info(aMarker, "Total records to process for decryption (isPageContent={}): {}", isPageContent,
                records.size());
        List<EncryptionRequestClass> requests = buildDecryptionRequests(records, isPageContent);

        if (requests.isEmpty()) {
            return;
        }

        List<EncryptionRequestClass> responses = encryption.decrypt(requests);
        Map<String, List<EncryptionRequestClass>> groupedResponses = groupResponsesByRecordId(responses);

        applyDecryptedValues(records, groupedResponses, isPageContent);
    }

    private List<EncryptionRequestClass> buildDecryptionRequests(List<OcrTextComparisonInput> records,
            boolean isPageContent) {
        List<EncryptionRequestClass> requests = new ArrayList<>();

        for (OcrTextComparisonInput r : records) {
            if (!r.getIsEncrypted())
                continue;

            String rawVal = isPageContent ? r.getExtractedText() : r.getAnswer();
            if (rawVal == null || rawVal.isEmpty())
                continue;

            if ("multi_value".equalsIgnoreCase(r.getLineItemType()) && rawVal.contains(",")) {
                addMultiValueRequests(requests, r, rawVal);
            } else {
                requests.add(newRequest(String.valueOf(r.getOcrFieldId()), rawVal, AES_256));
            }
        }

        return requests;
    }

    private void addMultiValueRequests(List<EncryptionRequestClass> requests, OcrTextComparisonInput record,
            String rawVal) {
        log.info("Adding multi-value requests for record ID: {}", record.getOcrFieldId());
        String[] parts = rawVal.split(",");
        for (int i = 0; i < parts.length; i++) {
            String trimmed = parts[i].trim();
            if (!trimmed.isEmpty()) {
                requests.add(newRequest(record.getOcrFieldId() + "_" + i, trimmed, AES_256));
            }
        }
    }

    private EncryptionRequestClass newRequest(String key, String value, String policy) {
        log.info("Creating request - Key: {}, Policy: {}", key, policy);
        return EncryptionRequestClass.builder()
                .key(key)
                .value(value)
                .policy(policy)
                .build();
    }

    private void applyDecryptedValues(List<OcrTextComparisonInput> records,
            Map<String, List<EncryptionRequestClass>> groupedResponses, boolean isPageContent) {
        log.info(aMarker, "Applying decrypted values to records (isPageContent={}): {}", isPageContent, records.size());
        for (OcrTextComparisonInput r : records) {
            List<EncryptionRequestClass> respList = groupedResponses.get(String.valueOf(r.getOcrFieldId()));
            if (respList == null)
                continue;

            // preserve order for multi-value
            respList.sort(Comparator.comparingInt(resp -> {
                if (!resp.getKey().contains("_"))
                    return 0;
                return Integer.parseInt(resp.getKey().substring(resp.getKey().indexOf("_") + 1));
            }));

            String finalValue = respList.stream()
                    .map(EncryptionRequestClass::getValue)
                    .collect(Collectors.joining(","));

            if (isPageContent) {
                r.setExtractedText(finalValue);
            } else {
                r.setAnswer(finalValue);
            }
        }
    }

    public void performEncryption(List<OcrTextComparisonInput> records, boolean itemWiseEncryption,
            boolean deepSiftOutputActivator) {
        log.info(aMarker, "Total records to process for encryption: {}", records.size());

        if (itemWiseEncryption) {
            log.info(aMarker, "Starting Encryption process for item-wise encryption");
            encryptAndApply(records, false);
        } else {
            log.info(aMarker, "Skipping Encryption as itemWiseEncryption is false");
        }

        if (deepSiftOutputActivator) {
            log.info(aMarker, "Starting Encryption process for deep sift text encryption");
            encryptAndApply(records, true);
        } else {
            log.info(aMarker, "Skipping Encryption as deep sift text is false");
        }
    }

    private void encryptAndApply(List<OcrTextComparisonInput> records, boolean isPageContent) {
        log.info(aMarker, "Total records to process for encryption (isPageContent={}): {}", isPageContent,
                records.size());
        List<EncryptionRequestClass> requests = buildEncryptionRequests(records, isPageContent);

        if (requests.isEmpty()) {
            log.info(aMarker, "No records found for encryption [{}]",
                    isPageContent ? "PageContent" : "ExtractedAnswer");
            return;
        }

        List<EncryptionRequestClass> responses = encryption.encrypt(requests);
        Map<String, List<EncryptionRequestClass>> groupedResponses = groupResponsesByRecordId(responses);

        applyEncryptedValues(records, groupedResponses, isPageContent);
    }

    private Map<String, List<EncryptionRequestClass>> groupResponsesByRecordId(List<EncryptionRequestClass> responses) {
        log.info("Grouping responses by record ID, total responses: {}", responses.size());
        return responses.stream()
                .collect(Collectors.groupingBy(resp -> {
                    String key = resp.getKey();
                    return key.contains("_") ? key.substring(0, key.indexOf("_")) : key;
                }));
    }

    private List<EncryptionRequestClass> buildEncryptionRequests(List<OcrTextComparisonInput> records,
            boolean isPageContent) {
        log.info("Building encryption requests (isPageContent={}): {}", isPageContent, records.size());
        List<EncryptionRequestClass> requests = new ArrayList<>();

        for (OcrTextComparisonInput r : records) {
            if (!r.getIsEncrypted())
                continue;

            String rawVal = isPageContent ? r.getExtractedText() : r.getBestMatch();
            if (rawVal == null || rawVal.isEmpty())
                continue;

            if ("multi_value".equalsIgnoreCase(r.getLineItemType()) && rawVal.contains(",")) {
                addMultiValueRequests(requests, r, rawVal);
            } else {
                requests.add(newRequest(String.valueOf(r.getOcrFieldId()), rawVal, AES_256));
            }
        }
        return requests;
    }

    private void applyEncryptedValues(List<OcrTextComparisonInput> records,
            Map<String, List<EncryptionRequestClass>> groupedResponses, boolean isPageContent) {
        log.info(aMarker, "Applying encrypted values to records (isPageContent={}): {}", isPageContent, records.size());
        for (OcrTextComparisonInput r : records) {
            List<EncryptionRequestClass> respList = groupedResponses.get(String.valueOf(r.getOcrFieldId()));
            if (respList == null)
                continue;

            // Preserve order for multi-value
            respList.sort(Comparator.comparingInt(resp -> {
                if (!resp.getKey().contains("_"))
                    return 0;
                return Integer.parseInt(resp.getKey().substring(resp.getKey().indexOf("_") + 1));
            }));

            String finalValue = respList.stream()
                    .map(EncryptionRequestClass::getValue)
                    .collect(Collectors.joining(","));

            if (isPageContent) {
                r.setExtractedText(finalValue);
            } else {
                r.setBestMatch(finalValue);
            }
        }
    }
}
