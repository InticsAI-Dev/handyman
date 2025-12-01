package in.handyman.raven.lib.bsh.member;

import java.util.*;

public class MemberDataTransformer {
    private boolean enableAddressMerging;

    private static Set ADDRESS_FIELDS;

    static {
        ADDRESS_FIELDS = new HashSet();
        ADDRESS_FIELDS.add("member_zipcode");
        ADDRESS_FIELDS.add("member_state");
        ADDRESS_FIELDS.add("member_city");
        ADDRESS_FIELDS.add("member_gender");
        ADDRESS_FIELDS.add("member_address_line1");
    }

    public MemberDataTransformer() {
        this.enableAddressMerging = false;
    }

    public MemberDataTransformer(boolean enableAddressMerging) {
        this.enableAddressMerging = enableAddressMerging;
    }

    public Map processPatientData(Map input) {
        System.out.println("Starting processPatientData execution");

        List patientDataList = (List) input.get("patient_data");
        if (patientDataList == null || patientDataList.isEmpty()) {
            System.out.println("No patient data found in input");
            return createEmptyOutput();
        }

        Map result = new HashMap();
        List memberDetailsList = new ArrayList();
        Set seen = new HashSet();

        List sections = new ArrayList();

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

        if (enableAddressMerging) {
            System.out.println("Address merging is ENABLED - performing merge");
            mergeAddressFields(sections);
        } else {
            System.out.println("Address merging is DISABLED - skipping merge");
        }

        for (int s = 0; s < sections.size(); s++) {
            SectionData sectionData = (SectionData) sections.get(s);
            Map sectionOutput = new HashMap();
            sectionOutput.put("sectionAlias", sectionData.sectionAlias);

            List transformedFields = new ArrayList();

            for (int fd = 0; fd < sectionData.fields.size(); fd++) {
                FieldData fieldData = (FieldData) sectionData.fields.get(fd);
                String uniqueKey = fieldData.key + "|" + fieldData.value + "|" + sectionData.sectionAlias;

                if (!seen.contains(uniqueKey)) {
                    seen.add(uniqueKey);

                    Map transformedField = new HashMap();
                    transformedField.put("key", fieldData.key);
                    transformedField.put("value", fieldData.value);
                    transformedField.put("label", fieldData.label);

                    Map boundingBox = new HashMap();
                    if (fieldData.boundingBoxList != null && fieldData.boundingBoxList.size() == 4) {
                        boundingBox.put("topLeftX", fieldData.boundingBoxList.get(0));
                        boundingBox.put("topLeftY", fieldData.boundingBoxList.get(1));
                        boundingBox.put("bottomRightX", fieldData.boundingBoxList.get(2));
                        boundingBox.put("bottomRightY", fieldData.boundingBoxList.get(3));
                    }
                    transformedField.put("boundingBox", boundingBox);

                    transformedFields.add(transformedField);
                }
            }

            sectionOutput.put("fields", transformedFields);
            memberDetailsList.add(sectionOutput);
        }

        result.put("MEMBER_DETAILS", memberDetailsList);

        System.out.println("Completed processPatientData execution with " + memberDetailsList.size() + " sections");
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
                    System.out.println("Found matching sections: " + targetSection.sectionAlias +
                            " and " + sourceSection.sectionAlias);

                    if (!targetHasAddress && hasAddressFields(sourceSection)) {
                        System.out.println("Moving address fields from " + sourceSection.sectionAlias +
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

        // Remove from source
        for (int i = 0; i < toRemove.size(); i++) {
            source.fields.remove(toRemove.get(i));
        }

        target.fields.addAll(addressFields);

        System.out.println("Moved " + addressFields.size() + " address fields");
    }

    private Map createEmptyOutput() {
        Map result = new HashMap();
        result.put("MEMBER_DETAILS", new ArrayList());
        return result;
    }

    // Inner classes - must be public static for BeanShell
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