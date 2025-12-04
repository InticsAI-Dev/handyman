package in.handyman.raven.lib.bsh.member;

import java.util.*;
import org.slf4j.Logger;

public class MemberTransformerFinalBsh {

    private boolean enableAddressMerging;
    private Logger logger;

    private static Set ADDRESS_FIELDS;
    private static Set PASSTHROUGH_KEYS;

    static {
        ADDRESS_FIELDS = new HashSet();
        ADDRESS_FIELDS.add("member_zipcode");
        ADDRESS_FIELDS.add("member_state");
        ADDRESS_FIELDS.add("member_city");
        ADDRESS_FIELDS.add("member_gender");
        ADDRESS_FIELDS.add("member_address_line1");

        // Keys that trigger passthrough mode
        PASSTHROUGH_KEYS = new HashSet();
        PASSTHROUGH_KEYS.add("member_id");
        PASSTHROUGH_KEYS.add("medicaid_id");
        PASSTHROUGH_KEYS.add("multiple_member_indicator");
    }

    public MemberTransformerFinalBsh() {
        this.enableAddressMerging = false;
    }

    public MemberTransformerFinalBsh(Logger logger) {
        this.logger = logger;
        this.enableAddressMerging = false;
        if (logger != null) logger.info("MemberTransformerFinalBsh initialized without merging");
    }

    public MemberTransformerFinalBsh(boolean enableAddressMerging) {
        this.enableAddressMerging = enableAddressMerging;
    }

    public MemberTransformerFinalBsh(boolean enableAddressMerging, Logger logger) {
        this.enableAddressMerging = enableAddressMerging;
        this.logger = logger;
        if (logger != null) logger.info("MemberTransformerFinalBsh initialized with merging=" + enableAddressMerging);
    }

    /**
     * Main processing method compatible with ProviderDataTransformer
     * Returns List<Hashtable> format with sorContainerName
     */
    public List processProviders(Object input) {
        log("Starting processProviders for member data");

        // Handle different input types
        List inputList = null;
        if (input instanceof List) {
            inputList = (List) input;
        } else if (input instanceof Map) {
            Map inputMap = (Map) input;
            inputList = (List) inputMap.get("patient_data");
        }

        if (inputList == null || inputList.isEmpty()) {
            log("No data found in input");
            return new ArrayList();
        }

        // Check if this is passthrough data
        if (shouldPassthrough(inputList)) {
            log("Detected passthrough pattern - processing directly");
            return processPassthroughData(inputList);
        }

        // Otherwise process normally
        return processNormalData(inputList);
    }

    /**
     * Check if data should be passed through without transformation
     */
    private boolean shouldPassthrough(List dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return false;
        }

        Set foundKeys = new HashSet();

        for (int i = 0; i < dataList.size(); i++) {
            Object item = dataList.get(i);
            if (item instanceof Map) {
                Map itemMap = (Map) item;
                Object keyObj = itemMap.get("key");
                if (keyObj != null) {
                    foundKeys.add(keyObj);
                }
            }
        }

        // Check if any passthrough keys are present
        for (Iterator it = PASSTHROUGH_KEYS.iterator(); it.hasNext(); ) {
            Object passthroughKey = it.next();
            if (foundKeys.contains(passthroughKey)) {
                log("Found passthrough key: " + passthroughKey);
                return true;
            }
        }

        return false;
    }

    /**
     * Process data in passthrough mode - minimal transformation
     * Returns List<Hashtable> with sorContainerName for Java processor
     */
    private List processPassthroughData(List dataList) {
        log("Processing " + dataList.size() + " items in passthrough mode");

        List result = new ArrayList();
        Set seen = new HashSet();

        for (int i = 0; i < dataList.size(); i++) {
            Object item = dataList.get(i);
            if (item instanceof Map) {
                Map itemMap = (Map) item;

                String key = (String) itemMap.get("key");
                Object valueObj = itemMap.get("value");
                String value = valueObj != null ? String.valueOf(valueObj) : "";
                String label = (String) itemMap.get("label");
                Object sectionAliasObj = itemMap.get("section_alias");
                String sectionAlias = sectionAliasObj != null ? (String) sectionAliasObj : "MEMBER_INFO";

                String uniqueKey = key + "|" + value;

                if (!seen.contains(uniqueKey)) {
                    seen.add(uniqueKey);

                    Hashtable outputItem = new Hashtable();
                    outputItem.put("key", key);
                    outputItem.put("value", value);
                    outputItem.put("label", label != null ? label : "");
                    outputItem.put("sectionAlias", sectionAlias);
                    outputItem.put("confidence", 0.0);
                    outputItem.put("sorContainerName", "MEMBER_DETAILS");

                    // Handle bounding box
                    Object bbObj = itemMap.get("boundingBox");
                    if (bbObj instanceof Map) {
                        outputItem.put("boundingBox", bbObj);
                    } else {
                        outputItem.put("boundingBox", new HashMap());
                    }

                    result.add(outputItem);

                    log("Passthrough field: key=" + key + ", value=" + value);
                }
            }
        }

        log("Passthrough processing complete with " + result.size() + " fields");
        return result;
    }

    /**
     * Process data normally with full transformation logic
     * Returns List<Hashtable> with sorContainerName for Java processor
     */
    private List processNormalData(List patientDataList) {
        log("Processing data with normal transformation");

        List result = new ArrayList();
        Set seen = new HashSet();
        List sections = new ArrayList();

        // Parse input data into sections
        for (int p = 0; p < patientDataList.size(); p++) {
            Map patientSection = (Map) patientDataList.get(p);
            Map meta = (Map) patientSection.get("_meta");
            List fields = (List) patientSection.get("fields");

            if (meta == null || fields == null) {
                continue;
            }

            String sectionAlias = (String) meta.get("sa");
            SectionData sectionData = new SectionData();
            sectionData.sectionAlias = sectionAlias;
            sectionData.fields = new ArrayList();

            for (int f = 0; f < fields.size(); f++) {
                Map field = (Map) fields.get(f);
                String key = (String) field.get("k");
                String value = (String) field.get("v");
                String label = (String) field.get("l");
                List boundingBoxList = (List) field.get("b");

                FieldData fieldData = new FieldData();
                fieldData.key = key;
                fieldData.value = value != null ? value : "";
                fieldData.label = label != null ? label : "";
                fieldData.boundingBoxList = boundingBoxList;

                sectionData.fields.add(fieldData);
            }

            sections.add(sectionData);
        }

        // Apply address merging if enabled
        if (enableAddressMerging) {
            log("Address merging is ENABLED - performing merge");
            mergeAddressFields(sections);
        } else {
            log("Address merging is DISABLED - skipping merge");
        }

        // Convert sections to output format (List<Hashtable> with sorContainerName)
        for (int s = 0; s < sections.size(); s++) {
            SectionData sectionData = (SectionData) sections.get(s);

            for (int fd = 0; fd < sectionData.fields.size(); fd++) {
                FieldData fieldData = (FieldData) sectionData.fields.get(fd);
                String uniqueKey = fieldData.key + "|" + fieldData.value + "|" + sectionData.sectionAlias;

                if (!seen.contains(uniqueKey)) {
                    seen.add(uniqueKey);

                    Hashtable outputItem = new Hashtable();
                    outputItem.put("key", fieldData.key);
                    outputItem.put("value", fieldData.value);
                    outputItem.put("label", fieldData.label);
                    outputItem.put("sectionAlias", sectionData.sectionAlias);
                    outputItem.put("confidence", 0.0);
                    outputItem.put("sorContainerName", "MEMBER_DETAILS");

                    Map boundingBox = new HashMap();
                    if (fieldData.boundingBoxList != null && fieldData.boundingBoxList.size() == 4) {
                        boundingBox.put("topLeftX", fieldData.boundingBoxList.get(0));
                        boundingBox.put("topLeftY", fieldData.boundingBoxList.get(1));
                        boundingBox.put("bottomRightX", fieldData.boundingBoxList.get(2));
                        boundingBox.put("bottomRightY", fieldData.boundingBoxList.get(3));
                    }
                    outputItem.put("boundingBox", boundingBox);

                    result.add(outputItem);
                }
            }
        }

        log("Normal processing complete with " + result.size() + " fields");
        return result;
    }

    private void mergeAddressFields(List sections) {
        for (int i = 0; i < sections.size(); i++) {
            SectionData targetSection = (SectionData) sections.get(i);
            boolean targetHasAddress = hasAddressFields(targetSection);
            MemberIdentifier targetId = extractMemberIdentifier(targetSection);

            if (targetId.isEmpty()) {
                continue;
            }

            for (int j = 0; j < sections.size(); j++) {
                if (i == j) continue;

                SectionData sourceSection = (SectionData) sections.get(j);
                MemberIdentifier sourceId = extractMemberIdentifier(sourceSection);

                if (targetId.matches(sourceId)) {
                    log("Found matching sections: " + targetSection.sectionAlias +
                            " and " + sourceSection.sectionAlias);

                    if (!targetHasAddress && hasAddressFields(sourceSection)) {
                        log("Moving address fields from " + sourceSection.sectionAlias +
                                " to " + targetSection.sectionAlias);
                        moveAddressFields(sourceSection, targetSection);
                    }
                }
            }
        }
    }

    private boolean hasAddressFields(SectionData section) {
        for (int i = 0; i < section.fields.size(); i++) {
            FieldData field = (FieldData) section.fields.get(i);
            if (ADDRESS_FIELDS.contains(field.key)) {
                return true;
            }
        }
        return false;
    }

    private MemberIdentifier extractMemberIdentifier(SectionData section) {
        MemberIdentifier id = new MemberIdentifier();

        for (int i = 0; i < section.fields.size(); i++) {
            FieldData field = (FieldData) section.fields.get(i);
            String key = field.key;

            if (key.equals("member_id")) {
                id.memberId = field.value;
            } else if (key.equals("member_full_name")) {
                id.name = field.value;
            } else if (key.equals("member_first_name")) {
                id.firstName = field.value;
            } else if (key.equals("member_last_name")) {
                id.lastName = field.value;
            } else if (key.equals("member_date_of_birth")) {
                id.dob = field.value;
            }
        }

        return id;
    }

    private void moveAddressFields(SectionData source, SectionData target) {
        List addressFields = new ArrayList();
        List toRemove = new ArrayList();

        for (int i = 0; i < source.fields.size(); i++) {
            FieldData field = (FieldData) source.fields.get(i);
            if (ADDRESS_FIELDS.contains(field.key)) {
                addressFields.add(field);
                toRemove.add(field);
            }
        }

        for (int i = 0; i < toRemove.size(); i++) {
            source.fields.remove(toRemove.get(i));
        }

        target.fields.addAll(addressFields);
        log("Moved " + addressFields.size() + " address fields");
    }

    private void log(String msg) {
        if (logger != null) logger.info(msg);
        else System.out.println(msg);
    }

    // Inner classes
    public static class SectionData {
        public String sectionAlias;
        public List fields;
    }

    public static class FieldData {
        public String key;
        public String value;
        public String label;
        public List boundingBoxList;
    }

    public static class MemberIdentifier {
        public String memberId;
        public String name;
        public String firstName;
        public String lastName;
        public String dob;

        public boolean isEmpty() {
            return (memberId == null || memberId.isEmpty()) &&
                    (name == null || name.isEmpty()) &&
                    (firstName == null || firstName.isEmpty()) &&
                    (dob == null || dob.isEmpty());
        }

        public boolean matches(MemberIdentifier other) {
            if (this.isEmpty() || other.isEmpty()) {
                return false;
            }

            if (this.memberId != null && !this.memberId.isEmpty() &&
                    other.memberId != null && !other.memberId.isEmpty() &&
                    this.memberId.equalsIgnoreCase(other.memberId)) {
                return true;
            }

            boolean nameMatches = false;

            if (this.name != null && !this.name.isEmpty() &&
                    other.name != null && !other.name.isEmpty() &&
                    this.name.equalsIgnoreCase(other.name)) {
                nameMatches = true;
            }

            if (!nameMatches &&
                    this.firstName != null && !this.firstName.isEmpty() &&
                    this.lastName != null && !this.lastName.isEmpty() &&
                    other.firstName != null && !other.firstName.isEmpty() &&
                    other.lastName != null && !other.lastName.isEmpty() &&
                    this.firstName.equalsIgnoreCase(other.firstName) &&
                    this.lastName.equalsIgnoreCase(other.lastName)) {
                nameMatches = true;
            }

            boolean dobMatches = this.dob != null && !this.dob.isEmpty() &&
                    other.dob != null && !other.dob.isEmpty() &&
                    this.dob.equals(other.dob);

            return nameMatches && dobMatches;
        }
    }
}