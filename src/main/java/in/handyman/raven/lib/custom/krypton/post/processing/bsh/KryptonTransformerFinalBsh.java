package in.handyman.raven.lib.custom.krypton.post.processing.bsh;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.*;

public class KryptonTransformerFinalBsh {
    private Logger logger;

    public KryptonTransformerFinalBsh(Logger logger) {
        this.logger = logger;
    }


    public static Map processKryptonJson(Map kryptonJson) {
        Map metaContainerEntityDetails = getMetaContainerEntityDetails();
        Map metaContainerItemDetails = getMetaContainerItemDetails();
        Map metaContainerItemAliasDetails = getMetaContainerItemAliasDetails();
        Map outputJson = new HashMap();

        JSONObject jsonObject = new JSONObject(kryptonJson);
        Iterator keys = jsonObject.keys();

        while (keys.hasNext()) {
            String jsonKey = (String) keys.next();
            processKey(jsonKey, jsonObject, outputJson, metaContainerEntityDetails, metaContainerItemDetails, metaContainerItemAliasDetails);
        }

        return outputJson;
    }

    private static void processKey(String jsonKey, JSONObject jsonObject, Map outputJson,
                                   Map metaContainerEntityDetails, Map metaContainerItemDetails, Map metaContainerItemAliasDetails) {

        Iterator metaKeys = metaContainerEntityDetails.keySet().iterator();
        while (metaKeys.hasNext()) {
            String metaContainerKey = (String) metaKeys.next();
            List values = (List) metaContainerEntityDetails.get(metaContainerKey);
            if(metaContainerItemAliasDetails.containsKey(metaContainerKey)){
                Map metaItemAndKeyDetails=(Map) metaContainerItemAliasDetails.get(metaContainerKey);

                if (values.contains(jsonKey)) {
                    Object jsonValue = jsonObject.opt(jsonKey);
                    handleJsonValue(metaContainerKey, jsonValue, outputJson, metaContainerItemDetails, metaItemAndKeyDetails);
                }
            }

        }
    }

    private static void handleJsonValue(String metaContainerKey, Object jsonValue, Map outputJson,
                                        Map metaContainerItemDetails, Map metaItemAndKeyDetails) {

        if (jsonValue instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jsonValue;
            Integer infoNodeLen = jsonArray.length();
            if(infoNodeLen<2){
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.optJSONObject(i);
                if (item != null) {
                    List outputList = buildOutputList(metaContainerKey, item, metaContainerItemDetails, metaItemAndKeyDetails);
                    appendToOutput(metaContainerKey, outputList, outputJson);
                }
            }
            }else{
//                if(metaContainerKey.equals("MEMBER_DETAILS")){
                    List outputList = mapToMemberDetails(metaContainerKey, jsonArray);
                    appendToOutput(metaContainerKey, outputList, outputJson);
//                }
//                else {
//                    for (int i = 0; i < jsonArray.length(); i++) {
//                        JSONObject item = jsonArray.optJSONObject(i);
//                        if (item != null) {
//                            List outputList = buildOutputList(metaContainerKey, item, metaContainerItemDetails, metaItemAndKeyDetails);
//                            appendToOutput(metaContainerKey, outputList, outputJson);
//                        }
//                    }
//                }

            }
        } else if (jsonValue instanceof JSONObject) {
            List outputList = buildOutputList(metaContainerKey, (JSONObject) jsonValue, metaContainerItemDetails, metaItemAndKeyDetails);
            outputJson.put(metaContainerKey, outputList);
        }
    }

    private static void appendToOutput(String key, List newData, Map outputJson) {
        if (outputJson.containsKey(key)) {
            List existing = (List) outputJson.get(key);
            existing.add(newData);
            outputJson.put(key, existing);
        } else {
            outputJson.put(key, newData);
        }
    }

    private static List buildOutputList(String metaContainerKey, JSONObject jsonObject,
                                        Map itemMapping, Map metaItemAndKeyDetails) {
        List outputJson = new ArrayList();
        Iterator metaKeyIterator = metaItemAndKeyDetails.keySet().iterator();

        while (metaKeyIterator.hasNext()) {
            String metaKey = (String) metaKeyIterator.next();
            String jsonKey = (String) metaItemAndKeyDetails.get(metaKey);
            if (jsonObject.has(jsonKey)) {
                Map outputObject = new HashMap();
                outputObject.put("key", metaKey);
                outputObject.put("value", jsonObject.optString(jsonKey) != null ? jsonObject.optString(jsonKey) : "");
                outputObject.put("confidence", new Integer(100));
                outputObject.put("boundingBox", new HashMap());
                outputJson.add(outputObject);
            }
        }

        return outputJson;
    }



    private static Map getMetaContainerEntityDetails() {
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

        metaContainerEntityDetails.put("MEMBER_DETAILS", memberDetails);
        metaContainerEntityDetails.put("REFERRING_PROVIDER_DETAILS", referringProviderDetails);
        metaContainerEntityDetails.put("SERVICING_DETAILS", servicingDetails);
        metaContainerEntityDetails.put("SERVICING_FACILITY_DETAILS", servicingFacilityDetails);
        metaContainerEntityDetails.put("SERVICING_PROVIDER_DETAILS", servicingProviderDetails);
        metaContainerEntityDetails.put("UNDEFINED_PROVIDER_DETAILS", servicingProviderDetails);

        return metaContainerEntityDetails;
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
        servicingProviderDetails.add("servicing_provider_city");
        servicingProviderDetails.add("servicing_provider_tin");
        servicingProviderDetails.add("servicing_provider_zipcode");
        servicingProviderDetails.add("servicing_provider_address_line2");
        servicingProviderDetails.add("servicing_provider_specialty");
        servicingProviderDetails.add("servicing_provider_state");
        servicingProviderDetails.add("servicing_provider_npi");
        servicingProviderDetails.add("servicing_provider_address_line1");
        metaContainerItemDetails.put("SERVICING_PROVIDER_DETAILS", servicingProviderDetails);

        return metaContainerItemDetails;
    }

    static Map getMetaContainerItemAliasDetails() {
        Map metaContainerItemAliasDetails = new HashMap();
        Map memberContainerEntityDetails = new HashMap();

        // Member Information
        memberContainerEntityDetails.put("member_zipcode", "memberZipcode");
        memberContainerEntityDetails.put("member_last_name", "memberName");
        memberContainerEntityDetails.put("member_id", "memberId");
        memberContainerEntityDetails.put("member_date_of_birth", "memberDOB");
        memberContainerEntityDetails.put("member_gender", "memberGender");
        memberContainerEntityDetails.put("member_state", "memberState");
        memberContainerEntityDetails.put("member_first_name", "memberName");
        memberContainerEntityDetails.put("member_full_name", "memberName");
        memberContainerEntityDetails.put("medicaid_id", "medicaidId");
        memberContainerEntityDetails.put("member_city", "memberCity");
        memberContainerEntityDetails.put("member_address_line1", "memberAddress");
        memberContainerEntityDetails.put("member_group_id", "memberGroupId");
        memberContainerEntityDetails.put("member_type", "memberType");
        memberContainerEntityDetails.put("member_phone", "memberPhone");
        metaContainerItemAliasDetails.put("MEMBER_DETAILS",memberContainerEntityDetails);

        Map facilityContainerEntityDetails = new HashMap();

        // Facility Information
        facilityContainerEntityDetails.put("servicing_facility_type", "providerFacilityType");
        facilityContainerEntityDetails.put("servicing_facility_name", "facilityName");
        facilityContainerEntityDetails.put("servicing_facility_npi", "providerFacilityNPI");
        facilityContainerEntityDetails.put("servicing_facility_tax_id", "providerFacilityTaxId");
        facilityContainerEntityDetails.put("servicing_facility_address", "providerFacilityAddress");
        facilityContainerEntityDetails.put("servicing_facility_city", "providerFacilityCity");
        facilityContainerEntityDetails.put("servicing_facility_state", "providerFacilityState");
        facilityContainerEntityDetails.put("servicing_facility_zip", "providerFacilityZip");
        facilityContainerEntityDetails.put("servicing_facility_specialty", "providerFacilitySpecialty");
        metaContainerItemAliasDetails.put("SERVICING_FACILITY_DETAILS",facilityContainerEntityDetails);

        Map servicingProviderContainerEntityDetails = new HashMap();

        // Servicing Provider Information
        servicingProviderContainerEntityDetails.put("servicing_provider_type", "providerType");
        servicingProviderContainerEntityDetails.put("servicing_provider_name", "providerName");
        servicingProviderContainerEntityDetails.put("servicing_provider_npi", "providerNPI");
        servicingProviderContainerEntityDetails.put("servicing_provider_tax_id", "providerTaxId");
        servicingProviderContainerEntityDetails.put("servicing_provider_address", "providerAddress");
        servicingProviderContainerEntityDetails.put("servicing_provider_city", "providerCity");
        servicingProviderContainerEntityDetails.put("servicing_provider_state", "providerState");
        servicingProviderContainerEntityDetails.put("servicing_provider_zip", "providerZip");
        servicingProviderContainerEntityDetails.put("servicing_provider_specialty", "providerSpecialty");
        metaContainerItemAliasDetails.put("SERVICING_PROVIDER_DETAILS",facilityContainerEntityDetails);

        // Service Details
        Map serviceDetailsContainerEntityDetails = new HashMap();
        serviceDetailsContainerEntityDetails.put("service_cpt_codes", "cptCodes");
        serviceDetailsContainerEntityDetails.put("service_diagnosis_codes", "diagnosisCodes");
        serviceDetailsContainerEntityDetails.put("diagnosis_description", "diagnosisDescription");
        serviceDetailsContainerEntityDetails.put("service_authorization_id", "authorizationID");
        serviceDetailsContainerEntityDetails.put("service_level_of_service", "levelOfService");
        serviceDetailsContainerEntityDetails.put("service_level_of_care", "levelOfCare");
        serviceDetailsContainerEntityDetails.put("service_start_date", "serviceStartDate");
        serviceDetailsContainerEntityDetails.put("service_end_date", "serviceEndDate");
        serviceDetailsContainerEntityDetails.put("service_admit_date", "admitDate");
        serviceDetailsContainerEntityDetails.put("service_discharge_date", "dischargeDate");
        serviceDetailsContainerEntityDetails.put("service_voluntary_involuntary_status", "voluntaryInvoluntaryStatus");
        metaContainerItemAliasDetails.put("SERVICING_DETAILS",facilityContainerEntityDetails);

        return metaContainerItemAliasDetails;
    }



    Map inputKryptonJson() {
        Map inputJson = new HashMap();
        Map memberInformation = new HashMap();
        memberInformation.put("memberType", "");
        memberInformation.put("memberName", "");
        memberInformation.put("memberId", "");
        memberInformation.put("memberGroupId", null);
        memberInformation.put("memberDOB", "");
        memberInformation.put("memberAddress", "");
        memberInformation.put("memberCity", "");
        memberInformation.put("memberState", "");
        memberInformation.put("memberZipcode", "");
        memberInformation.put("memberPhone", null);
        memberInformation.put("medicaidId", "");
        memberInformation.put("memberGender", "null");

        Map serviceDetails = new HashMap();
        serviceDetails.put("cptCodes", Arrays.asList("", ""));
        serviceDetails.put("diagnosisCodes", new ArrayList());
        serviceDetails.put("authorizationID", "");
        serviceDetails.put("levelOfService", "");
        serviceDetails.put("levelOfCare", Arrays.asList(""));
        serviceDetails.put("serviceStartDate", "");
        serviceDetails.put("serviceEndDate", null);
        serviceDetails.put("admitDate", "");
        serviceDetails.put("dischargeDate", null);
        serviceDetails.put("voluntaryInvoluntaryStatus", "");

        inputJson.put("MemberInformation", Arrays.asList(memberInformation));
        inputJson.put("ServiceDetails", serviceDetails);

        return inputJson;
    }

    public static List mapToMemberDetails(String metaContainerKey, JSONArray memberInformations) {
        // Define priority checklist for member types
        Map containerCheckList = getStringListMap();
        List checkListValues = (List) containerCheckList.get(metaContainerKey);        // Handle case with multiple members
        List allMembers = extractAllMemberTypes(memberInformations);

        // Calculate ranks for each member type
        Map memberTypeRanks = calculateMemberTypeRanks(allMembers, checkListValues);

        // Find member type with minimum rank
        String memberTypeWithMinRank = findMemberTypeWithMinRank(memberTypeRanks);

        // Find index of this member type in the memberInformation list
        int indexOfMinRankMember = findIndexOfMinRankMember(allMembers, memberTypeWithMinRank);

        // Create the final result by adding member information of the member with the minimum rank
        List finalResult = new ArrayList();
        if (indexOfMinRankMember >= 0 && indexOfMinRankMember < memberInformations.length()) {
            finalResult = addMemberInfoToFinalResult(memberInformations, indexOfMinRankMember,finalResult);
        }

        return finalResult;
    }


    // Method to extract all member types from memberInformations
    private static List extractAllMemberTypes(JSONArray memberInformations) {
        List allMembers = new ArrayList();

        List containerTypeKeys = getList();

        Iterator iterator = memberInformations.iterator();
        while (iterator.hasNext()) {
            Object item = iterator.next();

            if (item instanceof JSONObject) {

                JSONObject memberMap = (JSONObject) item; // Cast to JSONObject instead of Map
                for ( Object containerTypeKey : containerTypeKeys) {
                    if (memberMap.has(containerTypeKey.toString())) {
                        Object memberTypeObj = memberMap.opt(containerTypeKey.toString());
                        String memberTypeValue = "";
                        if (memberTypeObj != null) {
                            memberTypeValue = memberTypeObj.toString();
                        }

                        if (memberTypeValue != null &&
                                memberTypeValue.trim().length() > 0 &&
                                !memberTypeValue.equals("null")) {
                            allMembers.add(memberTypeValue);
                        } else {
                            allMembers.add("");
                        }
                    }
                }
            }
        }
        return allMembers;
    }


    private static List getList() {
        List containerTypeKeys = new ArrayList();
        containerTypeKeys.add("memberType");
        containerTypeKeys.add("providerType");
        return containerTypeKeys;
    }

    // Method to calculate ranks for each member type based on the checklist
    private static Map calculateMemberTypeRanks(List allMembers, List memberChecklist) {
        Map memberTypeRanks = new HashMap();
        Iterator membersIterator = allMembers.iterator();
        while (membersIterator.hasNext()) {
            String memberType = (String) membersIterator.next();
            int rank = memberChecklist.indexOf(memberType.toLowerCase()) + 1;
            if (rank == 0) {
                rank = Integer.MAX_VALUE;
            }
            memberTypeRanks.put(memberType, new Integer(rank));
        }
        return memberTypeRanks;
    }

    // Method to find the member type with the minimum rank
    private static String findMemberTypeWithMinRank(Map memberTypeRanks) {
        String memberTypeWithMinRank = null;
        int minRank = Integer.MAX_VALUE;

        Iterator rankEntryIterator = memberTypeRanks.entrySet().iterator();
        while (rankEntryIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) rankEntryIterator.next();
            Integer rankValue = (Integer) entry.getValue();
            if (rankValue.intValue() < minRank) {
                minRank = rankValue.intValue();
                memberTypeWithMinRank = (String) entry.getKey();
            }
        }
        return memberTypeWithMinRank;
    }

    // Method to find the index of the member type with minimum rank in allMembers list
    private static int findIndexOfMinRankMember(List allMembers, String memberTypeWithMinRank) {
        int indexOfMinRankMember = -1;
        for (int i = 0; i < allMembers.size(); i++) {
            String member = (String) allMembers.get(i);
            if (member.equals(memberTypeWithMinRank)) {
                indexOfMinRankMember = i;
                break;
            }
        }
        return indexOfMinRankMember;
    }

    // Method to add the selected member's information to the final result
    private static List addMemberInfoToFinalResult(JSONArray memberInformation, int indexOfMinRankMember, List finalResult) {
        JSONObject inputJsonObject = memberInformation.optJSONObject(indexOfMinRankMember); // Get JSONObject instead of Map        Iterator entryIterator = inputJsonObjectMap.entrySet().iterator();
        if (inputJsonObject != null) {
            Iterator entryIterator = inputJsonObject.keys();
        while (entryIterator.hasNext()) {
            String key = (String) entryIterator.next(); // Get key as String            Map memberInfo = new HashMap();
                    Map memberInfo = new HashMap();
                    memberInfo.put("key", key);
                    memberInfo.put("value", inputJsonObject.optString(key) != null ? inputJsonObject.optString(key) : "");
                    memberInfo.put("confidence", new Integer(0));  // Using 100 as in the original class
                    memberInfo.put("boundingBox", new HashMap());
                    finalResult.add(memberInfo);

         }
        }
        return finalResult;
    }


    private static Map getStringListMap() {
        Map containerCheckList = new HashMap();
        List memberChecklist = new ArrayList();
        memberChecklist.add("member");
        memberChecklist.add("patient");
        memberChecklist.add("subscriber");
        memberChecklist.add("Enrollee");

        List referringProviderChecklist = new ArrayList();
        referringProviderChecklist.add("member");
        referringProviderChecklist.add("patient");
        referringProviderChecklist.add("subscriber");
        referringProviderChecklist.add("Enrollee");

        List servicingProviderChecklist = new ArrayList();
        servicingProviderChecklist.add("member");
        servicingProviderChecklist.add("patient");
        servicingProviderChecklist.add("subscriber");
        servicingProviderChecklist.add("Enrollee");

        List servicingFacilityChecklist = new ArrayList();
        servicingFacilityChecklist.add("member");
        servicingFacilityChecklist.add("patient");
        servicingFacilityChecklist.add("subscriber");
        servicingFacilityChecklist.add("Enrollee");

        List serviceDetailsChecklist = new ArrayList();
        serviceDetailsChecklist.add("member");
        serviceDetailsChecklist.add("patient");
        serviceDetailsChecklist.add("subscriber");
        serviceDetailsChecklist.add("Enrollee");

        containerCheckList.put("MEMBER_DETAILS", memberChecklist);
        containerCheckList.put("REFERRING_PROVIDER_DETAILS", referringProviderChecklist);
        containerCheckList.put("SERVICING_DETAILS", serviceDetailsChecklist);
        containerCheckList.put("SERVICING_FACILITY_DETAILS", servicingFacilityChecklist);
        containerCheckList.put("SERVICING_PROVIDER_DETAILS", servicingProviderChecklist);
        containerCheckList.put("UNDEFINED_PROVIDER_DETAILS", memberChecklist);
        return containerCheckList;
    }

}

