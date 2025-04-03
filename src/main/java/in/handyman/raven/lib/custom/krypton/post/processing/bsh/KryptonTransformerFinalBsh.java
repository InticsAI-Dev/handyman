package in.handyman.raven.lib.custom.krypton.post.processing.bsh;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.*;

public class KryptonTransformerFinalBsh {
    private Logger logger;

    public KryptonTransformerFinalBsh(Logger logger) {
        this.logger = logger;
    }

    public static List processKryptonJson1(Map kryptonJson) {
        Map metaItemAndKeyDetails = metaItemAndKeyDetails();
        Map containerMapping = metaContainerItemDetails();
        List outputJson = new ArrayList();

        Iterator it = metaItemAndKeyDetails.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String outputKey = (String) entry.getKey();
            String kryptonKey = (String) entry.getValue();

            Object value = findValueInKryptonJson(kryptonJson, kryptonKey);

            // Check if key already exists in outputJson
            boolean found = false;
            Iterator outputIterator = outputJson.iterator();
            while (outputIterator.hasNext()) {
                Map jsonObject = (Map) outputIterator.next();
                if (jsonObject.get("key").equals(outputKey)) {
                    // If key exists, append value with comma if necessary
                    String existingValue = (String) jsonObject.get("value");
                    if (existingValue.length() > 0) {
                        existingValue += ", " + (value != null ? value : "");
                    } else {
                        existingValue = (value != null ? value.toString() : "");
                    }
                    jsonObject.put("value", existingValue);
                    found = true;
                    break;
                }
            }

            // If not found, add new entry
            if (!found) {
                Map jsonObject = new HashMap();
                jsonObject.put("key", outputKey);
                jsonObject.put("value", value != null ? value : "");
                jsonObject.put("confidence", new Integer(100));
                jsonObject.put("boundingBox", new HashMap());

                outputJson.add(jsonObject);
            }
        }
        return outputJson;
    }

    private static Object findValueInKryptonJson(Map kryptonJson, String kryptonKey) {
        Iterator iterator = kryptonJson.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object value = entry.getValue();

            if (value instanceof List) {
                List list = (List) value;
                if (!list.isEmpty() && list.get(0) instanceof Map) {
                    Map map = (Map) list.get(0);
                    if (map.containsKey(kryptonKey)) {
                        return map.get(kryptonKey);
                    }
                }
            } else if (value instanceof Map) {
                Map map = (Map) value;
                if (map.containsKey(kryptonKey)) {
                    return map.get(kryptonKey);
                }
            }
        }
        return null;
    }


    public static Map processKryptonJson(Map kryptonJson) {
        Map metaContainerEntityDetails = metaContainerEntityDetails();
        Map metaContainerItemDetails = metaContainerItemDetails();
        Map metaItemAndKeyDetails = metaItemAndKeyDetails();
        Map outputJson=new HashMap();


        JSONObject jsonObject = new JSONObject(kryptonJson);
        Iterator keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Iterator metaKeys = metaContainerEntityDetails.keySet().iterator();

            while (metaKeys.hasNext()) {

                String metaContainerKey = (String) metaKeys.next();
                List values = (List) metaContainerEntityDetails.get(metaContainerKey);

                if (values.contains(key)) {
                    Object jsonValue = jsonObject.opt(key);
                    if (jsonValue instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray) jsonValue;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject item = jsonArray.optJSONObject(i);
                            if (item != null) {
//                                System.out.println("\nmetaContainerKey : " + metaContainerKey + "\nitems: " + item);
                                List outputJsonFromArray = matchMetaContainerWithKeys(metaContainerKey, item, metaContainerItemDetails,metaItemAndKeyDetails);
                                if(outputJson.containsKey(metaContainerKey)){
                                    List existMetaList= (List)outputJson.get(metaContainerKey);
                                    existMetaList.add(outputJsonFromArray);
                                    outputJson.put(metaContainerKey,existMetaList);

                                }else {
                                    outputJson.put(metaContainerKey,outputJsonFromArray);
                                }

                            }
                        }
                    } else if (jsonValue instanceof JSONObject) {
//                        System.out.println("\nmetaContainerKey : " + metaContainerKey + "\nitems: " + jsonValue);
                        List outputJsonFromArray= matchMetaContainerWithKeys(metaContainerKey, (JSONObject) jsonValue, metaContainerItemDetails,metaItemAndKeyDetails);
                        outputJson.put(metaContainerKey,outputJsonFromArray);
                    }

                }
            }
        }
        return outputJson;
    }

    private static List matchMetaContainerWithKeys(String metaContainerKey, JSONObject jsonObject, Map itemMapping, Map metaItemAndKeyDetails) {
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
//                System.out.println("  Mapped Key: " + metaKey + " -> " + jsonKey + " : " + jsonObject.optString(jsonKey));
                outputJson.add(outputObject);
            }
        }
        return outputJson;
    }


    private static Map metaContainerEntityDetails() {
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

        List servicingFacilityDetails = new ArrayList();
        servicingFacilityDetails.add("SERVICING_FACILITY_DETAILS");
        servicingFacilityDetails.add("FacilityInformation");

        List servicingProviderDetails = new ArrayList();
        servicingProviderDetails.add("ServicingProviderInformation");
        servicingProviderDetails.add("SERVICING_PROVIDER_DETAILS");

        metaContainerEntityDetails.put("MEMBER_DETAILS", memberDetails);
        metaContainerEntityDetails.put("REFERRING_PROVIDER_DETAILS", referringProviderDetails);
        metaContainerEntityDetails.put("SERVICING_DETAILS", servicingDetails);
        metaContainerEntityDetails.put("SERVICING_FACILITY_DETAILS", servicingFacilityDetails);
        metaContainerEntityDetails.put("SERVICING_PROVIDER_DETAILS", servicingProviderDetails);

        return metaContainerEntityDetails;
    }

    private static Map metaContainerItemDetails() {
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

    static Map metaItemAndKeyDetails() {
        Map metaContainerEntityDetails = new HashMap();

        // Member Information
        metaContainerEntityDetails.put("member_zipcode", "memberZipcode");
        metaContainerEntityDetails.put("member_last_name", "memberName");
        metaContainerEntityDetails.put("member_id", "memberId");
        metaContainerEntityDetails.put("member_date_of_birth", "memberDOB");
        metaContainerEntityDetails.put("member_gender", "memberGender");
        metaContainerEntityDetails.put("member_state", "memberState");
        metaContainerEntityDetails.put("member_first_name", "memberName");
        metaContainerEntityDetails.put("member_full_name", "memberName");
        metaContainerEntityDetails.put("medicaid_id", "medicaidId");
        metaContainerEntityDetails.put("member_city", "memberCity");
        metaContainerEntityDetails.put("member_address_line1", "memberAddress");
        metaContainerEntityDetails.put("member_group_id", "memberGroupId");
        metaContainerEntityDetails.put("member_type", "memberType");
        metaContainerEntityDetails.put("member_phone", "memberPhone");

        // Facility Information
        metaContainerEntityDetails.put("facility_provider_type", "providerType");
        metaContainerEntityDetails.put("facility_provider_name", "providerName");
        metaContainerEntityDetails.put("facility_provider_npi", "providerNPI");
        metaContainerEntityDetails.put("facility_provider_tax_id", "providerTaxId");
        metaContainerEntityDetails.put("facility_provider_address", "providerAddress");
        metaContainerEntityDetails.put("facility_provider_city", "providerCity");
        metaContainerEntityDetails.put("facility_provider_state", "providerState");
        metaContainerEntityDetails.put("facility_provider_zip", "providerZip");
        metaContainerEntityDetails.put("facility_provider_specialty", "providerSpecialty");

        // Servicing Provider Information
        metaContainerEntityDetails.put("servicing_provider_type", "providerType");
        metaContainerEntityDetails.put("servicing_provider_name", "providerName");
        metaContainerEntityDetails.put("servicing_provider_npi", "providerNPI");
        metaContainerEntityDetails.put("servicing_provider_tax_id", "providerTaxId");
        metaContainerEntityDetails.put("servicing_provider_address", "providerAddress");
        metaContainerEntityDetails.put("servicing_provider_city", "providerCity");
        metaContainerEntityDetails.put("servicing_provider_state", "providerState");
        metaContainerEntityDetails.put("servicing_provider_zip", "providerZip");
        metaContainerEntityDetails.put("servicing_provider_specialty", "providerSpecialty");

        // Service Details
        metaContainerEntityDetails.put("service_cpt_codes", "cptCodes");
        metaContainerEntityDetails.put("service_diagnosis_codes", "diagnosisCodes");
        metaContainerEntityDetails.put("service_authorization_id", "authorizationID");
        metaContainerEntityDetails.put("service_level_of_service", "levelOfService");
        metaContainerEntityDetails.put("service_level_of_care", "levelOfCare");
        metaContainerEntityDetails.put("service_start_date", "serviceStartDate");
        metaContainerEntityDetails.put("service_end_date", "serviceEndDate");
        metaContainerEntityDetails.put("service_admit_date", "admitDate");
        metaContainerEntityDetails.put("service_discharge_date", "dischargeDate");
        metaContainerEntityDetails.put("service_voluntary_involuntary_status", "voluntaryInvoluntaryStatus");

        return metaContainerEntityDetails;
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


}

