package in.handyman.raven.lib.custom.kvp.post.processing.bsh;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import java.util.*;

public class ProviderTransformerFinalBsh {
    private Logger logger;
    private final ObjectMapper objectMapper=new ObjectMapper();

    public ProviderTransformerFinalBsh(Logger logger) {
        this.logger = logger;
    }

    public List processProviders(List providers) {
        logger.info("Starting processProviders execution");

        List result = new ArrayList();
        List undefinedResult = new ArrayList();
        Map metaProviderEntityDetails = metaProviderEntityDetails();
        Map itemMappingDetails = itemMappingDetails();
        Map nameMappingDetails = nameMappingDetails();
        List outputProviderContainerList = outputProviderContainerList();

        Map providerTypeCount = new HashMap();
        for (Object obj : providers) {
            Map provider = (Map) obj;
            String providerType = (String) provider.get("Provider Type");
            providerTypeCount.put(providerType, (Integer)providerTypeCount.getOrDefault(providerType, 0) + 1);
        }

        for (int i = 0; i < providers.size(); i++) {
            Map provider = (Map) providers.get(i);
            logger.debug("Processing provider keys: {} ", provider.keySet());

            String providerType = (String) provider.get("Provider Type");
            String matchedContainer = findMatchingContainer(providerType, metaProviderEntityDetails);
            logger.info("{} found in the provider type json and mapping the fields.", matchedContainer);

            // Check if the provider type is duplicated
            if (((Integer) providerTypeCount.get(providerType) > 1) || matchedContainer.equals("UNDEFINED_PROVIDER_DETAILS")) {
                logger.info("Provider type {} is duplicated or undefined. Processing as undefined.", providerType);
                processUndefinedProviderData(provider, "UNDEFINED_PROVIDER_DETAILS", itemMappingDetails, nameMappingDetails, undefinedResult);
            } else {
                if (outputProviderContainerList.contains(matchedContainer)) {
                    outputProviderContainerList.remove(matchedContainer);
                    processProviderData(provider, matchedContainer, itemMappingDetails, nameMappingDetails, result);
                } else {
                    logger.info("{} container already found. Mapping to undefined provider.", matchedContainer);
                    processUndefinedProviderData(provider, "UNDEFINED_PROVIDER_DETAILS", itemMappingDetails, nameMappingDetails, undefinedResult);
                }
            }
        }

        result.addAll(undefinedResult);
        logger.info("processProviders completed with result size: {}" , result.size());

        return transformListToVector(result);
    }

    Vector transformListToVector(List inputList) {
        logger.info("Mapping the final result from object to vector list: {}", inputList.size());
        Vector result = new Vector();

        for (int i = 0; i < inputList.size(); i++) {
            ProviderTransformerOutputItem item = (ProviderTransformerOutputItem) inputList.get(i);
            Hashtable outputItem = new Hashtable();
            outputItem.put("key", item.key);
            outputItem.put("value", item.value);
            outputItem.put("boundingBox", new Hashtable());
            outputItem.put("confidence", new Double(100.0));
            outputItem.put("sorContainerName", item.sorContainerName);

            result.add(outputItem);
        }

        return result;
    }

    String findMatchingContainer(String providerType, Map metaProviderEntityDetails) {
        String cleanedProviderType = cleanString(providerType != null ? providerType : "").toLowerCase();

        for (Iterator it = metaProviderEntityDetails.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            List values = (List) entry.getValue();

            for (int i = 0; i < values.size(); i++) {
                String value = (String) values.get(i);
                if (cleanedProviderType.contains(cleanString(value))) {
                    logger.info("Executing exact match for container: {}" , entry.getKey());
                    return (String) entry.getKey();
                }
            }
        }

        return findClosestMatchingContainer(cleanedProviderType, metaProviderEntityDetails);
    }

    String findClosestMatchingContainer(String cleanedProviderType, Map metaProviderEntityDetails) {
        Map containerDistanceMap = new HashMap();

        for (Iterator it = metaProviderEntityDetails.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            List values = (List) entry.getValue();
            logger.debug("Calculating similarity for container: {}" , entry.getKey());

            for (int i = 0; i < values.size(); i++) {
                String value = (String) values.get(i);
                double distance = (calculateJaroWinklerDistance(cleanedProviderType, cleanString(value)) +
                        calculateLCSLength(cleanedProviderType, cleanString(value))) / 2;

                String key = (String) entry.getKey();
                double existingDistance = containerDistanceMap.containsKey(key) ? ((Double) containerDistanceMap.get(key)) : 0.0;
                containerDistanceMap.put(key, Math.max(existingDistance, distance));
            }
        }

        double maxDistance = 0.0;
        String bestMatch = "UNDEFINED_PROVIDER_DETAILS";

        for (Iterator it = containerDistanceMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            double value = (Double) entry.getValue();
            if (value > maxDistance) {
                maxDistance = value;
                bestMatch = (String) entry.getKey();
            }
        }

        if (maxDistance > 0.7) {
            return bestMatch;
        }

        logger.info("No exact match found. Using closest match: {} with similarity: {}" ,bestMatch, maxDistance);
        return "UNDEFINED_PROVIDER_DETAILS";
    }

    void processProviderData(Map provider, String matchedContainer, Map itemMappingDetails,
                             Map nameMappingDetails, List result) {
        List itemsList = (List) itemMappingDetails.get(matchedContainer);
        if (itemsList == null) {
            logger.warn("No items mapping found for container: {}" , matchedContainer);
            return;
        }

        logger.debug("Processing provider key sets: {}" , provider.keySet());

        for (int i = 0; i < itemsList.size(); i++) {
            String item = (String) itemsList.get(i);
            String mappedKey = (String) nameMappingDetails.get(item);
            logger.debug("Checking item: {} -> mapped to provider key: {}",item, mappedKey);

            if (mappedKey != null) {
                logger.debug("Accessing key in provider map: {}" , mappedKey);
                String keyValue = (String) provider.get(mappedKey);
                if (mappedKey.equals("Provider Name")) {
                    if (matchedContainer.equals("SERVICING_PROVIDER_DETAILS")) {
                        List providerTransformerOutputItems = transformServicingProviderName(keyValue, matchedContainer);
                        result.addAll(providerTransformerOutputItems);
                    } else if (matchedContainer.equals("REFERRING_PROVIDER_DETAILS")) {
                        List providerTransformerOutputItems = transformReferringProviderName(keyValue, matchedContainer);
                        result.addAll(providerTransformerOutputItems);
                    } else {
                        if (keyValue != null) {
                            result.add(new ProviderTransformerOutputItem(item, keyValue, new HashMap(), matchedContainer));
                        }
                    }
                } else {
                    if (keyValue != null) {
                        result.add(new ProviderTransformerOutputItem(item, keyValue, new HashMap(), matchedContainer));
                    }
                }
            }
        }
    }

    void processUndefinedProviderData(Map provider, String matchedContainer, Map itemMappingDetails,
                                      Map nameMappingDetails, List undefinedResult) {
        List itemsList = (List) itemMappingDetails.get(matchedContainer);
        if (itemsList == null) {
            logger.warn("No items mapping found for undefined container: {}", matchedContainer);
            return;
        }

        logger.debug("Processing undefined provider keys: {}" , provider.keySet());

        for (int i = 0; i < itemsList.size(); i++) {
            String item = (String) itemsList.get(i);
            String mappedKey = (String) nameMappingDetails.get(item);
            logger.debug("Checking item: {} -> mapped to undefined provider key: {}" ,item, mappedKey);

            if (mappedKey != null) {
                logger.debug("Accessing key in undefined provider map: {}",  mappedKey);
                String keyValue = (String) provider.get(mappedKey);
                if (keyValue != null) {
                    boolean found = false;

                    for (Object innerObj : undefinedResult) {
                        ProviderTransformerOutputItem providerTransformedObject = (ProviderTransformerOutputItem) innerObj;
                        if (providerTransformedObject.getKey().equals(item)) {
                            String oldJson = providerTransformedObject.getValue();
                            List valueList;
                            try {
                                valueList = objectMapper.readValue(oldJson, List.class);
                            } catch (Exception e) {
                                valueList = new ArrayList();
                                logger.error("Error in mapping the undefined provider value into the respective provider field {}",item); // Handle serialization failure
                            }
                            valueList.add(keyValue);
                            try {
                                String newJson = objectMapper.writeValueAsString(valueList);
                                providerTransformedObject.setValue(newJson);
                            } catch (Exception e) {
                                logger.error("Error in mapping the undefined provider value into the respective provider field {}",item); // Handle serialization failure
                            }
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        List newList = new ArrayList();
                        try {
                            String json = objectMapper.writeValueAsString(newList);
                            undefinedResult.add(new ProviderTransformerOutputItem(item, json, new HashMap(), matchedContainer));
                        } catch (Exception e) {
                            logger.error("Error in mapping the undefined provider value into the respective provider field {}",item); // Handle serialization failure
                        }
                    }
                }
            }
        }
    }

    String cleanString(String str) {
        if (str == null) return "";
        return str.trim().replaceAll(" +", " ").toLowerCase();
    }

    List transformReferringProviderName(String name, String containerName) {
        List ProviderTransformerOutputItems = new ArrayList();

        if (name == null || name.trim().isEmpty()) {
            return ProviderTransformerOutputItems;
        }

        String[] parts = name.trim().split("\\s+");
        String firstName, lastName = "";

        if (parts.length > 1) {
            firstName = "";
            for (int i = 0; i < parts.length - 1; i++) {
                firstName += parts[i] + " ";
            }
            firstName = firstName.trim();
            lastName = parts[parts.length - 1];
        } else {
            firstName = parts[0];
        }

        ProviderTransformerOutputItems.add(new ProviderTransformerOutputItem("referring_provider_first_name", firstName, new HashMap(), containerName));
        ProviderTransformerOutputItems.add(new ProviderTransformerOutputItem("referring_provider_last_name", lastName, new HashMap(), containerName));
        ProviderTransformerOutputItems.add(new ProviderTransformerOutputItem("referring_provider_full_name", name, new HashMap(), containerName));

        return ProviderTransformerOutputItems;
    }

    List transformServicingProviderName(String name, String containerName) {
        List ProviderTransformerOutputItems = new ArrayList();

        if (name == null || name.trim().isEmpty()) {
            return ProviderTransformerOutputItems;
        }

        String[] parts = name.trim().split("\\s+");
        String firstName, lastName = "";

        if (parts.length > 1) {
            firstName = "";
            for (int i = 0; i < parts.length - 1; i++) {
                firstName += parts[i] + " ";
            }
            firstName = firstName.trim();
            lastName = parts[parts.length - 1];
        } else {
            firstName = parts[0];
        }

        ProviderTransformerOutputItems.add(new ProviderTransformerOutputItem("servicing_provider_first_name", firstName, new HashMap(), containerName));
        ProviderTransformerOutputItems.add(new ProviderTransformerOutputItem("servicing_provider_last_name", lastName, new HashMap(), containerName));
        ProviderTransformerOutputItems.add(new ProviderTransformerOutputItem("servicing_provider_full_name", name, new HashMap(), containerName));

        return ProviderTransformerOutputItems;
    }

    double calculateJaroWinklerDistance(String str1, String str2) {
        double jaroScore = calculateJaroSimilarity(str1, str2);
        int prefixLength = 0;
        int maxPrefix = Math.min(4, Math.min(str1.length(), str2.length()));

        for (int i = 0; i < maxPrefix && str1.charAt(i) == str2.charAt(i); i++) {
            prefixLength++;
        }

        return jaroScore + (prefixLength * 0.1 * (1.0 - jaroScore));
    }

    double calculateJaroSimilarity(String str1, String str2) {
        int len1 = str1.length(), len2 = str2.length();
        int matchDistance = Math.max(len1, len2) / 2 - 1;
        if (matchDistance < 0) matchDistance = 0;

        boolean[] matches1 = new boolean[len1], matches2 = new boolean[len2];
        int matches = 0, transpositions = 0, k = 0;

        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, len2);
            for (int j = start; j < end; j++) {
                if (!matches2[j] && str1.charAt(i) == str2.charAt(j)) {
                    matches1[i] = matches2[j] = true;
                    matches++;
                    break;
                }
            }
        }

        for (int i = 0; i < len1; i++) {
            if (matches1[i]) {
                while (!matches2[k]) k++;
                if (str1.charAt(i) != str2.charAt(k)) transpositions++;
                k++;
            }
        }

        double m = matches;
        return matches == 0 ? 0.0 : (m / len1 + m / len2 + (m - transpositions / 2.0) / m) / 3.0;
    }

    double calculateLCSLength(String str1, String str2) {
        int len1 = str1.length(), len2 = str2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                dp[i][j] = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? dp[i - 1][j - 1] + 1 :
                        Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }

        return (double) dp[len1][len2] / Math.max(len1, len2);
    }


    List outputProviderContainerList() {
        List outputProviderContainerList = new ArrayList();

        outputProviderContainerList.add("SERVICING_PROVIDER_DETAILS");
        outputProviderContainerList.add("REFERRING_PROVIDER_DETAILS");
        outputProviderContainerList.add("SERVICING_FACILITY_DETAILS");
        outputProviderContainerList.add("UNDEFINED_PROVIDER_DETAILS");

        return outputProviderContainerList;
    }

    Map metaProviderEntityDetails() {
        Map metaProviderEntityDetails = new HashMap();
        metaProviderEntityDetails.put("SERVICING_PROVIDER_DETAILS", Arrays.asList(new String[]{"Servicing Provider", "physician", "Therapist", "Attending physician", "Accepting physician", "Rendering Provider"}));
        metaProviderEntityDetails.put("REFERRING_PROVIDER_DETAILS", Arrays.asList(new String[]{"Referring Provider", "Requesting Provider", "Ordering Provider"}));
        metaProviderEntityDetails.put("SERVICING_FACILITY_DETAILS", Arrays.asList(new String[]{"SERVICING_FACILITY_DETAILS", "Servicing Facility", "Service Facility", "Facility", "facility name"}));
        return metaProviderEntityDetails;
    }

    Map itemMappingDetails() {
        Map itemMappingDetails = new HashMap();
        itemMappingDetails.put("SERVICING_PROVIDER_DETAILS",
                Arrays.asList(new String[]{"servicing_provider_full_name", "servicing_provider_specialty", "servicing_provider_npi",
                        "servicing_provider_address_line1", "servicing_provider_address_line2", "servicing_provider_city",
                        "servicing_provider_state", "servicing_provider_tin", "servicing_provider_zipcode"}));

        itemMappingDetails.put("SERVICING_FACILITY_DETAILS",
                Arrays.asList(new String[]{"servicing_facility_last_name", "servicing_facility_npi", "servicing_facility_zipcode",
                        "servicing_facility_address_line1", "servicing_facility_address_line2", "servicing_facility_city",
                        "servicing_facility_state", "servicing_facility_tin", "servicing_facility_specialty"}));

        itemMappingDetails.put("REFERRING_PROVIDER_DETAILS",
                Arrays.asList(new String[]{"referring_provider_address_line1", "referring_provider_full_name", "referring_provider_npi",
                        "referring_provider_city", "referring_provider_specialty", "referring_provider_state",
                        "referring_provider_tin", "referring_provider_zipcode", "referring_provider_address_line2"}));

        itemMappingDetails.put("UNDEFINED_PROVIDER_DETAILS",
                Arrays.asList(new String[]{"undefined_provider_address_line_1", "undefined_provider_full_name", "undefined_provider_npi",
                        "undefined_provider_city", "undefined_provider_specialty", "undefined_provider_state",
                        "undefined_provider_tin", "undefined_provider_zipcode", "undefined_provider_address_line2"}));
        return itemMappingDetails;
    }

    Map nameMappingDetails() {
        Map nameMappingDetails = new HashMap();

        nameMappingDetails.put("referring_provider_type", "Provider Type");
        nameMappingDetails.put("referring_provider_npi", "Provider NPI");
        nameMappingDetails.put("referring_provider_specialty", "Provider Specialty");
        nameMappingDetails.put("referring_provider_tin", "Provider Tax ID");
        nameMappingDetails.put("referring_provider_address_line1", "Provider Address");
        nameMappingDetails.put("referring_provider_city", "Provider City");
        nameMappingDetails.put("referring_provider_state", "Provider State");
        nameMappingDetails.put("referring_provider_full_name", "Provider Name");
        nameMappingDetails.put("referring_provider_zipcode", "Provider ZIP Code");

        nameMappingDetails.put("servicing_facility_last_name", "Provider Name");
        nameMappingDetails.put("servicing_facility_type", "Provider Type");
        nameMappingDetails.put("servicing_facility_npi", "Provider NPI");
        nameMappingDetails.put("servicing_facility_specialty", "Provider Specialty");
        nameMappingDetails.put("servicing_facility_tin", "Provider Tax ID");
        nameMappingDetails.put("servicing_facility_address_line1", "Provider Address");
        nameMappingDetails.put("servicing_facility_city", "Provider City");
        nameMappingDetails.put("servicing_facility_state", "Provider State");
        nameMappingDetails.put("servicing_facility_zipcode", "Provider ZIP Code");

        nameMappingDetails.put("servicing_provider_full_name", "Provider Name");
        nameMappingDetails.put("servicing_provider_type", "Provider Type");
        nameMappingDetails.put("servicing_provider_npi", "Provider NPI");
        nameMappingDetails.put("servicing_provider_specialty", "Provider Specialty");
        nameMappingDetails.put("servicing_provider_tin", "Provider Tax ID");
        nameMappingDetails.put("servicing_provider_address_line1", "Provider Address");
        nameMappingDetails.put("servicing_provider_city", "Provider City");
        nameMappingDetails.put("servicing_provider_state", "Provider State");
        nameMappingDetails.put("servicing_provider_zipcode", "Provider ZIP Code");

        nameMappingDetails.put("undefined_provider_name", "Provider Name");
        nameMappingDetails.put("undefined_provider_full_name", "Provider Name");
        nameMappingDetails.put("undefined_provider_first_name", "Provider Name");
        nameMappingDetails.put("undefined_provider_last_name", "Provider Name");
        nameMappingDetails.put("undefined_provider_type", "Provider Type");
        nameMappingDetails.put("undefined_provider_npi", "Provider NPI");
        nameMappingDetails.put("undefined_provider_specialty", "Provider Specialty");
        nameMappingDetails.put("undefined_provider_tin", "Provider Tax ID");
        nameMappingDetails.put("undefined_provider_address_line_1", "Provider Address");
        nameMappingDetails.put("undefined_provider_city", "Provider City");
        nameMappingDetails.put("undefined_provider_state", "Provider State");
        nameMappingDetails.put("undefined_provider_zipcode", "Provider ZIP Code");

        return nameMappingDetails;
    }

    class ProviderTransformerOutputItem {
        String key;
        String value;
        Map boundingBox;
        double confidence;
        String sorContainerName;

        // Constructor
        public ProviderTransformerOutputItem(String key, String value, Map boundingBox, String sorContainerName) {
            this.key = key;
            this.value = value;
            this.boundingBox = boundingBox;
            this.confidence = 100.0;
            this.sorContainerName = sorContainerName;
        }

        // Getters
        public Map getBoundingBox() {
            return boundingBox;
        }

        public double getConfidence() {
            return confidence;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getSorContainerName() {
            return sorContainerName;
        }

        // Setters
        public void setBoundingBox(Map boundingBox) {
            this.boundingBox = boundingBox;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setSorContainerName(String sorContainerName) {
            this.sorContainerName = sorContainerName;
        }
    }


}