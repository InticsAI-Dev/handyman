package in.handyman.raven.lib.custom.krypton.post.processing.bsh;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.*;

public class KryptonTransformerFinalBsh {
    private Logger logger;

    public KryptonTransformerFinalBsh(Logger logger) {
        this.logger = logger;
        logger.info("KryptonTransformerFinalBsh initialized.");
    }

    public Map processKryptonJson(Map kryptonJson) {
        logger.info("Processing Krypton JSON. Input keys: {}", kryptonJson.keySet());
        Map metaContainerEntityDetails = getMetaContainerNodeAliasDetails();
        logger.info("Meta container entity details keys: {}", metaContainerEntityDetails.keySet());
        Map metaContainerItemDetails = getMetaContainerItemDetails();
        logger.info("Meta container item details keys: {}", metaContainerItemDetails.keySet());
        Map metaContainerItemAliasDetails = getMetaContainerItemAliasDetails();
        logger.info("Meta container item alias details keys: {}", metaContainerItemAliasDetails.keySet());
        Map outputJson = new HashMap();
        JSONObject jsonObject = new JSONObject(kryptonJson);
        Iterator keys = jsonObject.keys();
        logger.info("Processing JSON object keys: {}", jsonObject.keySet());
        while (keys.hasNext()) {
            String jsonContainerKey = (String) keys.next();
            processKey(jsonContainerKey, jsonObject, outputJson, metaContainerEntityDetails, metaContainerItemDetails, metaContainerItemAliasDetails);
        }
        logger.info("Final output JSON keys: {}", outputJson.keySet());
        return outputJson;
    }

    private void processKey(String jsonContainerKey, JSONObject jsonObject, Map outputJson,
                            Map metaContainerEntityDetails, Map metaContainerItemDetails, Map metaContainerItemAliasDetails) {
        logger.debug("Processing JSON key: {}", jsonContainerKey);
        Iterator metaKeys = metaContainerEntityDetails.keySet().iterator();
        while (metaKeys.hasNext()) {
            String metaContainerKey = (String) metaKeys.next();
            List metaContainerEntityKeys = (List) metaContainerEntityDetails.get(metaContainerKey);
            logger.debug("Checking meta container key: {}, with metaContainerEntityKeys: {}", metaContainerKey, metaContainerEntityKeys);
            if (metaContainerItemAliasDetails.containsKey(metaContainerKey)) {
                Map metaItemAndKeyDetails = (Map) metaContainerItemAliasDetails.get(metaContainerKey);
                logger.debug("Meta item and key details keys for {}: {}", metaContainerKey, metaItemAndKeyDetails.keySet());
                if (metaContainerEntityKeys.contains(jsonContainerKey)) {
                    Object jsonValue = jsonObject.opt(jsonContainerKey);
                    handleJsonValue(metaContainerKey, jsonValue, outputJson, metaContainerItemDetails, metaItemAndKeyDetails, metaContainerItemAliasDetails);
                }
            }
        }
    }

    private void handleJsonValue(String metaContainerKey, Object jsonValue, Map outputJson,
                                 Map metaContainerItemDetails, Map metaItemAndKeyDetails, Map metaContainerItemAliasDetails) {
        logger.debug("Handling JSON value for meta container key: {}", metaContainerKey);
        if (jsonValue instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jsonValue;
            Integer infoNodeLen = jsonArray.length();
            logger.debug("JSON array length for {}: {}", metaContainerKey, infoNodeLen);
            if (infoNodeLen < 2) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.optJSONObject(i);
                    if (item != null) {
                        List outputList = buildOutputList(metaContainerKey, item, metaContainerItemDetails, metaItemAndKeyDetails);
                        appendToOutput(metaContainerKey, outputList, outputJson);
                    }
                }
            } else {
                List outputList = mapToPriorityCheckDetails(metaContainerKey, jsonArray, metaItemAndKeyDetails, metaContainerItemAliasDetails);
                appendToOutput(metaContainerKey, outputList, outputJson);
            }
        } else if (jsonValue instanceof JSONObject) {
            List outputList = buildOutputList(metaContainerKey, (JSONObject) jsonValue, metaContainerItemDetails, metaItemAndKeyDetails);
            outputJson.put(metaContainerKey, outputList);
        }
    }

    private void appendToOutput(String key, List newData, Map outputJson) {
        logger.debug("Appending to output for key: {}", key);
        if (outputJson.containsKey(key)) {
            List existing = (List) outputJson.get(key);
            existing.addAll(newData);
            outputJson.put(key, existing);
        } else {
            outputJson.put(key, newData);
        }
    }

    private List buildOutputList(String metaContainerKey, JSONObject jsonObject,
                                 Map itemMapping, Map metaItemAndKeyDetails) {
        logger.debug("Building output list for meta container key: {}", metaContainerKey);
        List outputJson = new ArrayList();
        Iterator metaKeyIterator = metaItemAndKeyDetails.keySet().iterator();
        List validName = new ArrayList();
        validName.add("providerName");
        validName.add("memberName");
        logger.debug("Meta item and key details keys: {}", metaItemAndKeyDetails.keySet());
        while (metaKeyIterator.hasNext()) {
            String metaKey = (String) metaKeyIterator.next();
            String jsonKey = (String) metaItemAndKeyDetails.get(metaKey);
            logger.debug("Processing meta key: {}, corresponding to JSON key: {}", metaKey, jsonKey);
            if (jsonObject.has(jsonKey)) {
                if (!validName.contains(jsonKey)) {
                    String resultString = "";
                    if (jsonObject.get(jsonKey) instanceof JSONArray) {
                        JSONArray values = (JSONArray) jsonObject.get(jsonKey);
                        List items = new ArrayList();
                        for (int i = 0; i < values.length(); i++) {
                            items.add(values.get(i));
                        }
                        resultString = String.join(", ", items);
                        outputJsonBind(jsonObject, metaKey, resultString, outputJson);
                    } else if (jsonObject.get(jsonKey) instanceof String) {
                        outputJsonBind(jsonObject, metaKey, jsonObject.get(jsonKey).toString(), outputJson);
                    } else if (jsonObject.get(jsonKey) != null) {
                        // fallback for unexpected types
                        resultString = jsonObject.get(jsonKey).toString();
                        outputJsonBind(jsonObject, metaKey, resultString, outputJson);
                    }
                } else {
                    String value;
                    if (jsonObject.get(jsonKey) instanceof JSONArray) {
                        JSONArray values = (JSONArray) jsonObject.get(jsonKey);
                        List items = new ArrayList();
                        for (int i = 0; i < values.length(); i++) {
                            items.add(values.get(i));
                        }
                        value = String.join(", ", items);
                        outputJsonBind(jsonObject, metaKey, value, outputJson);
                    } else {
                        value = jsonObject.get(jsonKey) != null ? jsonObject.get(jsonKey).toString() : "";
                        outputJsonBind(jsonObject, metaKey, value, outputJson);
                    }
                    if (metaKey.contains("member_full_name")) {
                        value = jsonObject.get(jsonKey) != null ? jsonObject.get(jsonKey).toString() : "";
                        Map nameList = transformMemberName(jsonKey, metaKey, value);
                        outputJsonBind(jsonObject, "member_full_name", (String) nameList.get("member_full_name"), outputJson);
                        outputJsonBind(jsonObject, "member_first_name", (String) nameList.get("member_first_name"), outputJson);
                        outputJsonBind(jsonObject, "member_last_name", (String) nameList.get("member_last_name"), outputJson);
                    } else if (metaKey.contains("servicing_provider_full_name")) {
                        value = jsonObject.get(jsonKey) != null ? jsonObject.get(jsonKey).toString() : "";
                        Map nameList = transformServicingProviderName(jsonKey, metaKey, value);
                        outputJsonBind(jsonObject, "servicing_provider_full_name", (String) nameList.get("servicing_provider_full_name"), outputJson);
                        outputJsonBind(jsonObject, "servicing_provider_first_name", (String) nameList.get("servicing_provider_first_name"), outputJson);
                        outputJsonBind(jsonObject, "servicing_provider_last_name", (String) nameList.get("servicing_provider_last_name"), outputJson);
                    } else if (metaKey.contains("referring_provider_full_name")) {
                        value = jsonObject.get(jsonKey) != null ? jsonObject.get(jsonKey).toString() : "";
                        Map nameList = transformReferringProviderName(jsonKey, metaKey, value);
                        outputJsonBind(jsonObject, "referring_provider_full_name", (String) nameList.get("referring_provider_full_name"), outputJson);
                        outputJsonBind(jsonObject, "referring_provider_first_name", (String) nameList.get("referring_provider_first_name"), outputJson);
                        outputJsonBind(jsonObject, "referring_provider_last_name", (String) nameList.get("referring_provider_last_name"), outputJson);
                    } else if (metaKey.contains("servicing_facility_full_name")) {
                        value = jsonObject.get(jsonKey) != null ? jsonObject.get(jsonKey).toString() : "";
                        Map nameList = transformFacilityProviderName(jsonKey, metaKey, value);
                        outputJsonBind(jsonObject, "servicing_facility_full_name", (String) nameList.get("servicing_facility_full_name"), outputJson);
                        outputJsonBind(jsonObject, "servicing_facility_first_name", (String) nameList.get("servicing_facility_first_name"), outputJson);
                        outputJsonBind(jsonObject, "servicing_facility_last_name", (String) nameList.get("servicing_facility_last_name"), outputJson);
                    } else if (metaKey.contains("undefined_provider_full_name")) {
                        value = jsonObject.get(jsonKey) != null ? jsonObject.get(jsonKey).toString() : "";
                        Map nameList = transformUndefinedProviderName(jsonKey, metaKey, value);
                        outputJsonBind(jsonObject, "undefined_provider_full_name", (String) nameList.get("undefined_provider_full_name"), outputJson);
                        outputJsonBind(jsonObject, "undefined_provider_first_name", (String) nameList.get("undefined_provider_first_name"), outputJson);
                        outputJsonBind(jsonObject, "undefined_provider_last_name", (String) nameList.get("undefined_provider_last_name"), outputJson);
                    }
                }
            }
        }
        logger.debug("Output list built with {} entries.", outputJson.size());
        return outputJson;
    }

    private void outputJsonBind(JSONObject jsonObjectvalue, String metaKey, String value, List outputJson) {
        Map outputObject = new HashMap();
        outputObject.put("key", metaKey);
        outputObject.put("value", (!value.isEmpty()) && (!value.equals("null")) ? value : "");
        outputObject.put("confidence", getConfidenceScore());
        outputObject.put("boundingBox", new HashMap());
        outputJson.add(outputObject);
        logger.debug("Output JSON bound for key: {}", metaKey);
    }

    public List mapToPriorityCheckDetails(String metaContainerKey, JSONArray containerInformations, Map metaItemAndKeyDetails, Map metaContainerItemAliasDetails) {
        logger.info("Mapping to priority check details for meta container key: {}", metaContainerKey);
        // Define priority checklist for container types
        Map containerCheckList = getContainerPriorityMap();
        logger.debug("Container check list keys: {}", containerCheckList.keySet());
        List checkListValues = (List) containerCheckList.get(metaContainerKey);        // Handle case with multiple Containers
        logger.debug("Check list values for {}: {}", metaContainerKey, checkListValues);
        List allContainers = extractAllContainerTypes(containerInformations);
        logger.debug("All container types extracted: {}", allContainers);
        // Calculate ranks for each Containers type
        Map ContainersTypeRanks = calculateContainerTypeRanks(allContainers, checkListValues);
        logger.debug("Container type ranks: {}", ContainersTypeRanks);
        // Find Containers type with minimum rank
        String ContainersTypeWithMinRank = findContainerTypeWithMinRank(ContainersTypeRanks);
        logger.debug("Container type with minimum rank: {}", ContainersTypeWithMinRank);
        // Find index of this Containers type in the ContainersInformation list
        int indexOfMinRankContainers = findIndexOfMinRankContainer(allContainers, ContainersTypeWithMinRank);
        logger.debug("Index of min rank container: {}", indexOfMinRankContainers);
        // Create the final result by adding Containers information of the Containers with the minimum rank
        List finalResult = new ArrayList();
        if (indexOfMinRankContainers >= 0 && indexOfMinRankContainers < containerInformations.length()) {
            finalResult = addContainerInfoToFinalResult(containerInformations, indexOfMinRankContainers, finalResult, metaItemAndKeyDetails);
        } else if (indexOfMinRankContainers == -1 && containerInformations.length() > 0) {
            finalResult = addContainerInfoToFinalResult(containerInformations, 0, finalResult, metaItemAndKeyDetails);
        }
        logger.debug("Final result after priority check has {} entries.", finalResult.size());
        return finalResult;
    }

    // Method to extract all container types from containerInformation
    private List extractAllContainerTypes(JSONArray containerInformation) {
        List allContainers = new ArrayList();
        List containerTypeKeys = getAllTypePlaceholders();
        logger.debug("Container type keys to extract: {}", containerTypeKeys);
        Iterator iterator = containerInformation.iterator();
        while (iterator.hasNext()) {
            Object item = iterator.next();
            if (item instanceof JSONObject) {
                JSONObject containerMap = (JSONObject) item; // Cast to JSONObject instead of Map
                for (Object containerTypeKey : containerTypeKeys) {
                    if (containerMap.has(containerTypeKey.toString())) {
                        Object containerTypeObj = containerMap.opt(containerTypeKey.toString());
                        String containerTypeValue = "";
                        if (containerTypeObj != null) {
                            containerTypeValue = containerTypeObj.toString();
                        }
                        if (containerTypeValue != null &&
                                containerTypeValue.trim().length() > 0 &&
                                !containerTypeValue.equals("null")) {
                            allContainers.add(containerTypeValue);
                        } else {
                            allContainers.add("");
                        }
                    }
                }
            }
        }
        logger.debug("Extracted all container types: {}", allContainers);
        return allContainers;
    }

    // Method to calculate ranks for each container type based on the checklist
    private Map calculateContainerTypeRanks(List allContainers, List containerChecklist) {
        Map containerTypeRanks = new HashMap();
        Iterator containerIterator = allContainers.iterator();
        while (containerIterator.hasNext()) {
            String containerType = (String) containerIterator.next();
            int rank = containerChecklist != null ? containerChecklist.indexOf(containerType.trim()) + 1 : Integer.MAX_VALUE;
            if (rank == 0) {
                rank = Integer.MAX_VALUE;
            }
            containerTypeRanks.put(containerType, new Integer(rank));
        }
        logger.debug("Calculated container type ranks: {}", containerTypeRanks);
        return containerTypeRanks;
    }

    // Method to find the container type with the minimum rank
    private String findContainerTypeWithMinRank(Map containerTypeRanks) {
        String containerTypeWithMinRank = null;
        int minRank = Integer.MAX_VALUE;
        Iterator rankEntryIterator = containerTypeRanks.entrySet().iterator();
        while (rankEntryIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) rankEntryIterator.next();
            Integer rankValue = (Integer) entry.getValue();
            if (rankValue.intValue() < minRank) {
                minRank = rankValue.intValue();
                containerTypeWithMinRank = (String) entry.getKey();
            }
        }
        logger.debug("Container type with minimum rank found: {}", containerTypeWithMinRank);
        return containerTypeWithMinRank;
    }

    // Method to find the index of the container type with minimum rank in allContainers list
    private int findIndexOfMinRankContainer(List allContainers, String containerTypeWithMinRank) {
        int indexOfMinRankContainer = -1;
        for (int i = 0; i < allContainers.size(); i++) {
            String container = (String) allContainers.get(i);
            if (container != null && container.equals(containerTypeWithMinRank)) {
                indexOfMinRankContainer = i;
                break;
            }
        }
        logger.debug("Index of container with minimum rank: {}", indexOfMinRankContainer);
        return indexOfMinRankContainer;
    }

    static Map transformServicingProviderName(String containerName, String sorItemName, String name) {
        Map processedName = new HashMap();
        if (name == null || name.trim().isEmpty()) {
            return processedName;
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
            lastName = "";
        }
        processedName.put("servicing_provider_first_name", firstName);
        processedName.put("servicing_provider_last_name", lastName);
        processedName.put("servicing_provider_full_name", name);
        return processedName;
    }

    static Map transformMemberName(String containerName, String sorItemName, String name) {
        Map processedName = new HashMap();
        if (name == null || name.trim().isEmpty()) {
            return processedName;
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
            lastName = "";
        }
        processedName.put("member_first_name", firstName);
        processedName.put("member_last_name", lastName);
        processedName.put("member_full_name", name);
        return processedName;
    }

    static Map transformReferringProviderName(String containerName, String sorItemName, String name) {
        Map processedName = new HashMap();
        if (name == null || name.trim().isEmpty()) {
            return processedName;
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
            lastName = "";
        }
        processedName.put("referring_provider_first_name", firstName);
        processedName.put("referring_provider_last_name", lastName);
        processedName.put("referring_provider_full_name", name);
        return processedName;
    }

    static Map transformUndefinedProviderName(String containerName, String sorItemName, String name) {
        Map processedName = new HashMap();
        if (name == null || name.trim().isEmpty()) {
            return processedName;
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
            lastName = "";
        }
        processedName.put("undefined_provider_first_name", firstName);
        processedName.put("undefined_provider_last_name", lastName);
        processedName.put("undefined_provider_full_name", name);
        return processedName;
    }

    static Map transformFacilityProviderName(String containerName, String sorItemName, String name) {
        Map processedName = new HashMap();
        if (name == null || name.trim().isEmpty()) {
            return processedName;
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
            lastName = "";
        }
        processedName.put("servicing_facility_first_name", firstName);
        processedName.put("servicing_facility_last_name", lastName);
        processedName.put("servicing_facility_full_name", name);
        return processedName;
    }

    private List addContainerInfoToFinalResult(JSONArray containerInformation,
                                               int indexOfMinRankContainer,
                                               List finalResult,
                                               Map metaItemAndKeyDetails) {
        logger.debug("Adding container info to final result at index: {}", indexOfMinRankContainer);
        JSONObject jsonObject = containerInformation.optJSONObject(indexOfMinRankContainer);
        if (jsonObject != null) {
            Iterator metaKeyIterator = metaItemAndKeyDetails.keySet().iterator();
            List validName = new ArrayList();
            validName.add("providerName");
            validName.add("memberName");
            logger.debug("Meta item and key details keys: {}", metaItemAndKeyDetails.keySet());
            while (metaKeyIterator.hasNext()) {
                String metaKey = (String) metaKeyIterator.next();
                String jsonKey = (String) metaItemAndKeyDetails.get(metaKey);
                logger.debug("Processing meta key: {}, corresponding to JSON key: {}", metaKey, jsonKey);
                List items = new ArrayList();
                if (jsonObject.has(jsonKey)) {
                    if (!validName.contains(jsonKey)) {
                        String value;
                        if (jsonObject.get(jsonKey) instanceof JSONArray) {
                            JSONArray values = (JSONArray) jsonObject.get(jsonKey);
                            for (int i = 0; i < values.length(); i++) {
                                items.add(values.get(i));
                            }
                            value = String.join(", ", items);
                            addNameToResult(finalResult, metaKey, value);
                        } else if (jsonObject.get(jsonKey) instanceof String) {
                            value = (String) jsonObject.get(jsonKey);  // Directly assign the value if it's a String
                            addNameToResult(finalResult, metaKey, value);
                        } else if (jsonObject.get(jsonKey) != null) {
                            // fallback for unexpected types
                            value = jsonObject.get(jsonKey).toString();
                            addNameToResult(finalResult, metaKey, value);
                        }
                    } else {
                        // Handle name fields
                        String value = jsonObject.get(jsonKey) != null ? jsonObject.get(jsonKey).toString() : "";
                        if (jsonObject.get(jsonKey) instanceof JSONArray) {
                            JSONArray values = (JSONArray) jsonObject.get(jsonKey);
                            for (int i = 0; i < values.length(); i++) {
                                items.add(values.get(i));
                            }
                            value = String.join(", ", items);
                        }
                        if (metaKey.contains("member_full_name")) {
                            Map nameList = transformMemberName(jsonKey, metaKey, value);
                            addNameToResult(finalResult, "member_full_name", nameList.get("member_full_name").toString());
                            addNameToResult(finalResult, "member_first_name", nameList.get("member_first_name").toString());
                            addNameToResult(finalResult, "member_last_name", nameList.get("member_last_name").toString());
                        } else if (metaKey.contains("servicing_provider_full_name")) {
                            Map nameList = transformServicingProviderName(jsonKey, metaKey, value);
                            addNameToResult(finalResult, "servicing_provider_full_name", nameList.get("servicing_provider_full_name").toString());
                            addNameToResult(finalResult, "servicing_provider_first_name", nameList.get("servicing_provider_first_name").toString());
                            addNameToResult(finalResult, "servicing_provider_last_name", nameList.get("servicing_provider_last_name").toString());
                        } else if (metaKey.contains("referring_provider_full_name")) {
                            Map nameList = transformReferringProviderName(jsonKey, metaKey, value);
                            addNameToResult(finalResult, "referring_provider_full_name", nameList.get("referring_provider_full_name").toString());
                            addNameToResult(finalResult, "referring_provider_first_name", nameList.get("referring_provider_first_name").toString());
                            addNameToResult(finalResult, "referring_provider_last_name", nameList.get("referring_provider_last_name").toString());
                        } else if (metaKey.contains("servicing_facility_full_name")) {
                            Map nameList = transformFacilityProviderName(jsonKey, metaKey, value);
                            addNameToResult(finalResult, "servicing_facility_full_name", nameList.get("servicing_facility_full_name").toString());
                            addNameToResult(finalResult, "servicing_facility_first_name", nameList.get("servicing_facility_first_name").toString());
                            addNameToResult(finalResult, "servicing_facility_last_name", nameList.get("servicing_facility_last_name").toString());
                        } else if (metaKey.contains("undefined_provider_full_name")) {
                            Map nameList = transformUndefinedProviderName(jsonKey, metaKey, value);
                            addNameToResult(finalResult, "undefined_provider_full_name", nameList.get("undefined_provider_full_name").toString());
                            addNameToResult(finalResult, "undefined_provider_first_name", nameList.get("undefined_provider_first_name").toString());
                            addNameToResult(finalResult, "undefined_provider_last_name", nameList.get("undefined_provider_last_name").toString());
                        }
                    }
                }
            }
        }
        logger.debug("Container info added to final result. Current size: {}", finalResult.size());
        return finalResult;
    }

    static float getConfidenceScore() {
        return 100;
    }

    // Helper method to add name entries to finalResult
    private void addNameToResult(List finalResult, String key, String value) {
        Map containerInfo = new HashMap();
        containerInfo.put("key", key);
        containerInfo.put("value", (!value.isEmpty()) && (!value.equals("null")) ? value : "");
        containerInfo.put("confidence", getConfidenceScore());
        containerInfo.put("boundingBox", new HashMap());
        finalResult.add(containerInfo);
        logger.debug("Added to final result - Key: {}", key);
    }

    private static Map getMetaContainerNodeAliasDetails() {
        Map metaContainerEntityDetails = new HashMap();

        List memberDetails = new ArrayList();
        memberDetails.add("MemberInformation");
        memberDetails.add("MemberDetails");
        memberDetails.add("MEMBER_DETAILS");

        List referringProviderDetails = new ArrayList();
        referringProviderDetails.add("REFERRING_PROVIDER_DETAILS");
        referringProviderDetails.add("ReferringProviderInformation");


        List servicingDetails = new ArrayList();
        servicingDetails.add("ServicingDetails");
        servicingDetails.add("ServiceDetails");
        servicingDetails.add("SERVICING_DETAILS");
        servicingDetails.add("serviceDetails");


        List servicingFacilityDetails = new ArrayList();
        servicingFacilityDetails.add("SERVICING_FACILITY_DETAILS");
        servicingFacilityDetails.add("FacilityInformation");
        servicingFacilityDetails.add("facilityInfo");

        List servicingProviderDetails = new ArrayList();
        servicingProviderDetails.add("ServicingProviderInformation");
        servicingProviderDetails.add("SERVICING_PROVIDER_DETAILS");
        servicingProviderDetails.add("servicingProvider");


        List undefinedProviderDetails = new ArrayList();
        undefinedProviderDetails.add("UndefinedProviderInformation");

        metaContainerEntityDetails.put("MEMBER_DETAILS", memberDetails);
        metaContainerEntityDetails.put("REFERRING_PROVIDER_DETAILS", referringProviderDetails);
        metaContainerEntityDetails.put("SERVICING_DETAILS", servicingDetails);
        metaContainerEntityDetails.put("SERVICING_FACILITY_DETAILS", servicingFacilityDetails);
        metaContainerEntityDetails.put("SERVICING_PROVIDER_DETAILS", servicingProviderDetails);
        metaContainerEntityDetails.put("UNDEFINED_PROVIDER_DETAILS", undefinedProviderDetails);

        return metaContainerEntityDetails;
    }


    private static Map getContainerPriorityMap() {
        Map containerCheckList = new HashMap();
        List memberChecklist = new ArrayList();
        memberChecklist.add("Member");
        memberChecklist.add("Patient");
        memberChecklist.add("Subscriber");
        memberChecklist.add("Enrollee");

        List referringProviderChecklist = new ArrayList();
        referringProviderChecklist.add("Requesting Provider");


        List servicingProviderChecklist = new ArrayList();
        servicingProviderChecklist.add("Attending Physician");


        List servicingFacilityChecklist = new ArrayList();
        servicingFacilityChecklist.add("Admitting Facility");

        List serviceDetailsChecklist = new ArrayList();
        serviceDetailsChecklist.add("Service Details");

        List undefinedDetailsChecklist = new ArrayList();
        undefinedDetailsChecklist.add("undefined");


        containerCheckList.put("MEMBER_DETAILS", memberChecklist);
        containerCheckList.put("REFERRING_PROVIDER_DETAILS", referringProviderChecklist);
        containerCheckList.put("SERVICING_DETAILS", serviceDetailsChecklist);
        containerCheckList.put("SERVICING_FACILITY_DETAILS", servicingFacilityChecklist);
        containerCheckList.put("SERVICING_PROVIDER_DETAILS", servicingProviderChecklist);
        containerCheckList.put("UNDEFINED_PROVIDER_DETAILS", undefinedDetailsChecklist);
        return containerCheckList;
    }


    private static List getAllTypePlaceholders() {
        List containerTypeKeys = new ArrayList();
        containerTypeKeys.add("memberType");
        containerTypeKeys.add("providerType");

        return containerTypeKeys;
    }


    static Map getMetaContainerItemAliasDetails() {
        Map metaContainerItemAliasDetails = new HashMap();


        // Member Information
        Map memberContainerItemAliasDetails = new HashMap();
        memberContainerItemAliasDetails.put("member_zipcode", "memberZipcode");
        memberContainerItemAliasDetails.put("member_last_name", "memberName");
        memberContainerItemAliasDetails.put("member_id", "memberId");
        memberContainerItemAliasDetails.put("member_date_of_birth", "memberDOB");
        memberContainerItemAliasDetails.put("member_gender", "memberGender");
        memberContainerItemAliasDetails.put("member_state", "memberState");
        memberContainerItemAliasDetails.put("member_first_name", "memberName");
        memberContainerItemAliasDetails.put("member_full_name", "memberName");
        memberContainerItemAliasDetails.put("medicaid_id", "medicaidId");
        memberContainerItemAliasDetails.put("member_city", "memberCity");
        memberContainerItemAliasDetails.put("member_address_line1", "memberAddress");
        memberContainerItemAliasDetails.put("member_group_id", "memberGroupId");
        memberContainerItemAliasDetails.put("member_type", "memberType");
        memberContainerItemAliasDetails.put("member_phone", "memberPhone");
        metaContainerItemAliasDetails.put("MEMBER_DETAILS", memberContainerItemAliasDetails);


        // Facility Information
        Map facilityContainerItemAliasDetails = new HashMap();
        facilityContainerItemAliasDetails.put("servicing_facility_type", "providerType");
        facilityContainerItemAliasDetails.put("servicing_facility_full_name", "providerName");
        facilityContainerItemAliasDetails.put("servicing_facility_npi", "providerNPI");
        facilityContainerItemAliasDetails.put("servicing_facility_tin", "providerTaxId");
        facilityContainerItemAliasDetails.put("servicing_facility_address_line1", "providerAddress");
        facilityContainerItemAliasDetails.put("servicing_facility_city", "providerCity");
        facilityContainerItemAliasDetails.put("servicing_facility_state", "providerState");
        facilityContainerItemAliasDetails.put("servicing_facility_zipcode", "providerZip");
        facilityContainerItemAliasDetails.put("servicing_facility_specialty", "providerSpecialty");
        metaContainerItemAliasDetails.put("SERVICING_FACILITY_DETAILS", facilityContainerItemAliasDetails);

        // REFERRING PROVIDER
        Map referringProviderItemAliasDetails = new HashMap();
        referringProviderItemAliasDetails.put("referring_provider_type", "providerType");
        referringProviderItemAliasDetails.put("referring_provider_full_name", "providerName");
        referringProviderItemAliasDetails.put("referring_provider_npi", "providerNPI");
        referringProviderItemAliasDetails.put("referring_provider_tin", "providerTaxId");
        referringProviderItemAliasDetails.put("referring_provider_address_line1", "providerAddress");
        referringProviderItemAliasDetails.put("referring_provider_city", "providerCity");
        referringProviderItemAliasDetails.put("referring_provider_state", "providerState");
        referringProviderItemAliasDetails.put("referring_provider_zipcode", "providerZip");
        referringProviderItemAliasDetails.put("referring_provider_specialty", "providerSpecialty");
        metaContainerItemAliasDetails.put("REFERRING_PROVIDER_DETAILS", referringProviderItemAliasDetails);


        // Servicing Provider Information
        Map servicingProviderItemAliasDetails = new HashMap();
        servicingProviderItemAliasDetails.put("servicing_provider_type", "providerType");
        servicingProviderItemAliasDetails.put("servicing_provider_full_name", "providerName");
        servicingProviderItemAliasDetails.put("servicing_provider_first_name", "providerName");
        servicingProviderItemAliasDetails.put("servicing_provider_last_name", "providerName");
        servicingProviderItemAliasDetails.put("servicing_provider_npi", "providerNPI");
        servicingProviderItemAliasDetails.put("servicing_provider_tin", "providerTaxId");
        servicingProviderItemAliasDetails.put("servicing_provider_address_line1", "providerAddress");
        servicingProviderItemAliasDetails.put("servicing_provider_city", "providerCity");
        servicingProviderItemAliasDetails.put("servicing_provider_state", "providerState");
        servicingProviderItemAliasDetails.put("servicing_provider_zipcode", "providerZip");
        servicingProviderItemAliasDetails.put("servicing_provider_specialty", "providerSpecialty");
        metaContainerItemAliasDetails.put("SERVICING_PROVIDER_DETAILS", servicingProviderItemAliasDetails);


        // Service Details
        Map serviceDetailsItemAliasDetails = new HashMap();
        serviceDetailsItemAliasDetails.put("service_code", "cptCodes");
        serviceDetailsItemAliasDetails.put("diagnosis_code", "diagnosisCodes");
        serviceDetailsItemAliasDetails.put("diagnosis_description", "diagnosisDescription");
        serviceDetailsItemAliasDetails.put("auth_id", "authorizationID");
        serviceDetailsItemAliasDetails.put("level_of_service", "levelOfService");
        serviceDetailsItemAliasDetails.put("level_of_care", "levelOfCare");
        serviceDetailsItemAliasDetails.put("service_from_date", "serviceStartDate");
        serviceDetailsItemAliasDetails.put("service_to_date", "serviceEndDate");
        serviceDetailsItemAliasDetails.put("auth_admit_date", "admitDate");
        serviceDetailsItemAliasDetails.put("auth_discharge_date", "dischargeDate");
        serviceDetailsItemAliasDetails.put("service_voluntary_involuntary_status", "voluntaryInvoluntaryStatus");
        metaContainerItemAliasDetails.put("SERVICING_DETAILS", serviceDetailsItemAliasDetails);


        Map undefinedProviderDetails = new HashMap();
        undefinedProviderDetails.put("undefined_provider_type", "providerType");
        undefinedProviderDetails.put("undefined_provider_full_name", "providerName");
        undefinedProviderDetails.put("undefined_provider_npi", "providerNPI");
        undefinedProviderDetails.put("undefined_provider_tax_id", "providerTaxId");
        undefinedProviderDetails.put("undefined_provider_address", "providerAddress");
        undefinedProviderDetails.put("undefined_provider_city", "providerCity");
        undefinedProviderDetails.put("undefined_provider_state", "providerState");
        undefinedProviderDetails.put("undefined_provider_zip", "providerZip");
        undefinedProviderDetails.put("undefined_provider_specialty", "providerSpecialty");
        metaContainerItemAliasDetails.put("UNDEFINED_PROVIDER_DETAILS", undefinedProviderDetails);

        return metaContainerItemAliasDetails;
    }

    private static Map getMetaContainerItemDetails() {
        Map metaContainerItemDetails = new HashMap();

        List memberDetails = new ArrayList();
        memberDetails.add("member_zipcode");
        memberDetails.add("member_last_name");
        memberDetails.add("member_id");
        memberDetails.add("member_date_of_birth");
        memberDetails.add("member_gender");
        memberDetails.add("member_state");
        memberDetails.add("member_first_name");
        memberDetails.add("member_full_name");
        memberDetails.add("medicaid_id");
        memberDetails.add("member_city");
        memberDetails.add("member_address_line1");
        memberDetails.add("member_group_id");
        metaContainerItemDetails.put("MEMBER_DETAILS", memberDetails);

        List referringProviderDetails = new ArrayList();
        referringProviderDetails.add("referring_provider_full_name");
        referringProviderDetails.add("referring_provider_npi");
        referringProviderDetails.add("referring_provider_address_line1");
        referringProviderDetails.add("referring_provider_specialty");
        referringProviderDetails.add("referring_provider_tin");
        referringProviderDetails.add("referring_provider_city");
        referringProviderDetails.add("referring_provider_state");
        referringProviderDetails.add("referring_provider_zipcode");
        referringProviderDetails.add("referring_provider_first_name");
        referringProviderDetails.add("referring_provider_last_name");
        referringProviderDetails.add("referring_provider_address_line2");
        metaContainerItemDetails.put("REFERRING_PROVIDER_DETAILS", referringProviderDetails);

        List servicingDetails = new ArrayList();
        servicingDetails.add("diagnosis_description");
        servicingDetails.add("auth_id");
        servicingDetails.add("auth_discharge_disposition");
        servicingDetails.add("service_code");
        servicingDetails.add("service_description");
        servicingDetails.add("service_from_date");
        servicingDetails.add("diagnosis_code");
        servicingDetails.add("auth_discharge_date");
        servicingDetails.add("service_to_date");
        servicingDetails.add("level_of_care");
        servicingDetails.add("level_of_service");
        servicingDetails.add("length_of_stay");
        servicingDetails.add("auth_admit_date");
        metaContainerItemDetails.put("SERVICING_DETAILS", servicingDetails);

        List servicingFacilityDetails = new ArrayList();
        servicingFacilityDetails.add("servicing_facility_state");
        servicingFacilityDetails.add("servicing_facility_zipcode");
        servicingFacilityDetails.add("servicing_facility_full_name");
        servicingFacilityDetails.add("servicing_facility_last_name");
        servicingFacilityDetails.add("servicing_facility_specialty");
        servicingFacilityDetails.add("servicing_facility_address_line2");
        servicingFacilityDetails.add("servicing_facility_tin");
        servicingFacilityDetails.add("servicing_facility_npi");
        servicingFacilityDetails.add("servicing_facility_first_name");
        servicingFacilityDetails.add("servicing_facility_address_line1");
        servicingFacilityDetails.add("servicing_facility_city");
        metaContainerItemDetails.put("SERVICING_FACILITY_DETAILS", servicingFacilityDetails);

        List servicingProviderDetails = new ArrayList();
        servicingProviderDetails.add("servicing_provider_first_name");
        servicingProviderDetails.add("servicing_provider_last_name");
        servicingProviderDetails.add("servicing_provider_full_name");
        servicingProviderDetails.add("servicing_provider_city");
        servicingProviderDetails.add("servicing_provider_tin");
        servicingProviderDetails.add("servicing_provider_zipcode");
        servicingProviderDetails.add("servicing_provider_address_line2");
        servicingProviderDetails.add("servicing_provider_specialty");
        servicingProviderDetails.add("servicing_provider_state");
        servicingProviderDetails.add("servicing_provider_npi");
        servicingProviderDetails.add("servicing_provider_address_line1");
        metaContainerItemDetails.put("SERVICING_PROVIDER_DETAILS", servicingProviderDetails);


        List undefinedProviderDetails = new ArrayList();
        undefinedProviderDetails.add("undefined_provider_type");
        undefinedProviderDetails.add("undefined_provider_full_name");
        undefinedProviderDetails.add("undefined_provider_npi");
        undefinedProviderDetails.add("undefined_provider_tax_id");
        undefinedProviderDetails.add("undefined_provider_address");
        undefinedProviderDetails.add("undefined_provider_city");
        undefinedProviderDetails.add("undefined_provider_state");
        undefinedProviderDetails.add("undefined_provider_zip");
        undefinedProviderDetails.add("undefined_provider_specialty");
        metaContainerItemDetails.put("UNDEFINED_PROVIDER_DETAILS", undefinedProviderDetails);


        return metaContainerItemDetails;
    }

}