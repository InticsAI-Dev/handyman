import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;

import in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput;

public class MemberAddressMapper {

    private Logger logger;

    private static final String ENDING_ADDRESS_PATTERN_STRING =
            "(?:,\\s*|\\s*|/\\s*|\\.\\s*)?([\\w\\s\\(\\)''.\\-]+?)(?:,\\s*|/\\s*|\\.\\s*|\\s+)([A-Za-z]{2})(?:,\\s*|/\\s*|\\.\\s*|\\s+)(\\d{5}(?:-\\d{4})?)\\s*$";
    private static final Pattern ENDING_ADDRESS_PATTERN = Pattern.compile(ENDING_ADDRESS_PATTERN_STRING);

    private static final String SIMPLE_CITY_STATE_ZIP_PATTERN_STRING =
            "^([\\w\\s\\(\\)''.\\-]+?)(?:,\\s*|/\\s*|\\.\\s*|\\s+)([A-Za-z]{2})(?:,\\s*|/\\s*|\\.\\s*|\\s+)(\\d{5}(?:-\\d{4})?)\\s*$";
    private static final Pattern SIMPLE_CITY_STATE_ZIP_PATTERN = Pattern.compile(SIMPLE_CITY_STATE_ZIP_PATTERN_STRING);

    private static final String CITY_STATE_PATTERN_STRING =
            "(.+?)(?:,\\s*|/\\s*|\\.\\s*|\\s+)([\\w\\s\\(\\)''.\\-]+?)(?:,\\s*|/\\s*|\\.\\s*|\\s+)([A-Za-z]{2})(?:\\s*,\\s*|\\s*/\\s*|\\s*\\.|\\s+)?$";
    private static final Pattern CITY_STATE_PATTERN = Pattern.compile(CITY_STATE_PATTERN_STRING);

    // New ZIP code pattern for explicit validation
    private static final Pattern ZIP_CODE_PATTERN = Pattern.compile("\\b\\d{5}(?:-\\d{4})?\\b$");

    private static final Set VALID_US_STATES = new HashSet(Arrays.asList(new String[] {
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY",
            "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND",
            "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY",
            "DC", "GU", "PR", "VI", "AS", "MP"
    }));

    public MemberAddressMapper(Logger logger) {
        this.logger = logger;
    }

    public MappingResult doCustomPredictionMapping(Map predictionKeyMap, Long rootPipelineId) {
        String logPrefix = "[RootPipelineID: " + rootPipelineId + "] ";
        logger.info(logPrefix + "Entered MemberAddressMapper.doCustomPredictionMapping method.");

        if (predictionKeyMap == null) {
            return new MappingResult(new HashMap());
        }

        PostProcessingFieldsInput addressLine1Obj = getFirst(predictionKeyMap, "member_address_line1");
        PostProcessingFieldsInput cityObj = getFirst(predictionKeyMap, "member_city");
        PostProcessingFieldsInput stateObj = getFirst(predictionKeyMap, "member_state");
        PostProcessingFieldsInput zipcodeObj = getFirst(predictionKeyMap, "member_zipcode");

        String inputAddress = hasValue(addressLine1Obj) ? addressLine1Obj.getAnswer().trim() :
                (addressLine1Obj != null && addressLine1Obj.getAnswer() == null ? null : "");
        String city = hasValue(cityObj) ? cityObj.getAnswer().trim() : "";
        String state = hasValue(stateObj) ? stateObj.getAnswer().trim() : "";
        String zipcode = hasValue(zipcodeObj) ? zipcodeObj.getAnswer().trim() : "";

        // Track which fields were empty before parsing
        boolean cityWasEmpty = city.isEmpty();
        boolean stateWasEmpty = state.isEmpty();
        boolean zipcodeWasEmpty = zipcode.isEmpty();

        if (inputAddress != null && !inputAddress.isEmpty()) {
            logger.info(logPrefix + "Processing ''member_address_line1'' field (Input type: List<PostProcessingFieldsInput>).");

            AddressComponents parsedAddress = parseAddress(inputAddress);
            if (parsedAddress != null) {
                updateList(predictionKeyMap, "member_address_line1", parsedAddress.getAddressLine1(), addressLine1Obj);

                // Only update and copy dependent fields if the field was empty
                if (cityWasEmpty) {
                    updateList(predictionKeyMap, "member_city", parsedAddress.getCity(), addressLine1Obj);
                } else {
                    updateList(predictionKeyMap, "member_city", city, null);
                }

                if (stateWasEmpty) {
                    updateList(predictionKeyMap, "member_state", parsedAddress.getState(), addressLine1Obj);
                } else {
                    updateList(predictionKeyMap, "member_state", state, null);
                }

                if (zipcodeWasEmpty) {
                    updateList(predictionKeyMap, "member_zipcode", parsedAddress.getZipcode(), addressLine1Obj);
                } else {
                    updateList(predictionKeyMap, "member_zipcode", zipcode, null);
                }

                logger.info(logPrefix + "Address fields successfully mapped and updated.");
            } else {
                logger.warn(logPrefix + "Address parsing failed for ''member_address_line1''. Keeping original address.");
                updateList(predictionKeyMap, "member_address_line1", inputAddress, addressLine1Obj);
                updateList(predictionKeyMap, "member_city", city, null);
                updateList(predictionKeyMap, "member_state", state, null);
                updateList(predictionKeyMap, "member_zipcode", zipcode, null);
            }
        } else {
            logger.info(logPrefix + "Input ''member_address_line1'' field is empty or null. Setting address_line1 to empty.");
            String valueToSet = (inputAddress == null) ? null : "";
            updateList(predictionKeyMap, "member_address_line1", valueToSet, addressLine1Obj);
            updateList(predictionKeyMap, "member_city", city, null);
            updateList(predictionKeyMap, "member_state", state, null);
            updateList(predictionKeyMap, "member_zipcode", zipcode, null);
        }

        return new MappingResult(predictionKeyMap);
    }

    private void updateList(Map map,
                            String key,
                            String value,
                            PostProcessingFieldsInput source) {

        Object obj = map.get(key);
        if (!(obj instanceof List)) return;

        List list = (List) obj;

        for (int i = 0; i < list.size(); i++) {
            PostProcessingFieldsInput in =
                    (PostProcessingFieldsInput) list.get(i);

            in.setAnswer(value);

            if (value == null || isEmpty(value)) {
                if (value == null) {
                } else {
                    clearDependentFields(in);
                }
            } else if (source != null) {
                copyDependentFields(source, in);
            }
        }
        map.put(key, list);
    }

    private void clearDependentFields(PostProcessingFieldsInput in) {
        in.setLabel("");
        in.setSectionAlias("");
        in.setBBox("");
    }
    private void copyDependentFields(PostProcessingFieldsInput from,
                                     PostProcessingFieldsInput to) {

        to.setLabel(from.getLabel());
        to.setSectionAlias(from.getSectionAlias());
        to.setBBox(from.getBBox());
    }

    private PostProcessingFieldsInput getFirst(Map map, String key) {
        Object obj = map.get(key);
        if (obj instanceof List) {
            List list = (List) obj;
            if (!list.isEmpty()) {
                return (PostProcessingFieldsInput) list.get(0);
            }
        }
        return null;
    }

    private boolean hasValue(PostProcessingFieldsInput in) {
        return in != null && !isEmpty(in.getAnswer());
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public AddressComponents parseAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.trim().isEmpty()) {
            return new AddressComponents("", "", "", "");
        }

        String cleanedAddress = fullAddress.trim()
                .replaceAll("\\s+,", ",")
                .replaceAll(",\\s*", ",")
                .replaceAll("\\s+\\.", ".")
                .replaceAll("\\.\\s*", ".")
                .replaceAll("\\s+", " ")
                .replaceAll(">\\s*", "")
                .replaceAll("\\(City\\)", "")
                .replaceAll("\\(c\\)", "")
                .replaceAll("P\\.O\\. BOX", "PO BOX")
                .trim();

        if (cleanedAddress.isEmpty()) {
            return new AddressComponents("", "", "", "");
        }

        // Check for valid ZIP code at the end
        Matcher zipMatcher = ZIP_CODE_PATTERN.matcher(cleanedAddress);
        if (!zipMatcher.find()) {
            logger.info("No valid ZIP code found at the end of address: " + cleanedAddress);
            return new AddressComponents(cleanedAddress, "", "", "");
        }
        String zipCode = zipMatcher.group(0);
        String addressWithoutZip = cleanedAddress.substring(0, zipMatcher.start()).trim();

        // Try space-separated format FIRST (before other patterns that might match incorrectly)
        AddressComponents spaceSeparated = parseSpaceSeparated(cleanedAddress);
        if (spaceSeparated != null) {
            return spaceSeparated;
        }

        Matcher matcher = ENDING_ADDRESS_PATTERN.matcher(cleanedAddress);
        if (matcher.find()) {
            String city = matcher.group(1).trim().replaceAll("\\(City\\)", "").trim();
            String state = matcher.group(2).trim();
            String parsedZip = matcher.group(3).trim();

            String addressLine1 = cleanedAddress.substring(0, matcher.start()).trim();
            if (addressLine1.endsWith(",")) {
                addressLine1 = addressLine1.substring(0, addressLine1.length() - 1).trim();
            }
            addressLine1 = addressLine1.replaceAll(",", ", ").replaceAll("\\s+", " ").trim();

            return new AddressComponents(addressLine1, city, state, parsedZip);
        }

        Matcher simpleMatcher = SIMPLE_CITY_STATE_ZIP_PATTERN.matcher(cleanedAddress);
        if (simpleMatcher.find()) {
            String city = simpleMatcher.group(1).trim().replaceAll("\\(City\\)", "").trim();
            String state = simpleMatcher.group(2).trim();
            String parsedZip = simpleMatcher.group(3).trim();
            return new AddressComponents("", city, state, parsedZip);
        }

        Matcher cityStateMatcher = CITY_STATE_PATTERN.matcher(addressWithoutZip);
        if (cityStateMatcher.find()) {
            String potentialState = cityStateMatcher.group(3).trim();
            if (VALID_US_STATES.contains(potentialState.toUpperCase())) {
                String city = cityStateMatcher.group(2).trim().replaceAll("\\(City\\)", "").trim();
                String addressLine1 = cityStateMatcher.group(1).trim();
                if (addressLine1.endsWith(",")) {
                    addressLine1 = addressLine1.substring(0, addressLine1.length() - 1).trim();
                }
                addressLine1 = addressLine1.replaceAll(",", ", ").replaceAll("\\s+", " ").trim();
                return new AddressComponents(addressLine1, city, potentialState, zipCode);
            }
        }

        Pattern stateZipAtEndPattern = Pattern.compile("(.+?)(?:,\\s*|/\\s*|\\.\\s*|\\s+)([A-Za-z]{2})(?:,\\s*|/\\s*|\\.\\s*|\\s+)(\\d{5}(?:-\\d{4})?)\\s*$");
        Matcher stateZipMatcher = stateZipAtEndPattern.matcher(cleanedAddress);
        if (stateZipMatcher.find()) {
            String state = stateZipMatcher.group(2).trim();
            String parsedZip = stateZipMatcher.group(3).trim();
            String potentialAddressAndCity = stateZipMatcher.group(1).trim();

            String city = "";
            String addressLine1 = potentialAddressAndCity;

            int lastCommaIndex = potentialAddressAndCity.lastIndexOf(',');
            if (lastCommaIndex != -1) {
                String partAfterComma = potentialAddressAndCity.substring(lastCommaIndex + 1).trim();
                if (!partAfterComma.matches("\\d+.*") &&
                        !partAfterComma.matches("(?i).*(st|ave|rd|ln|ct|pl|dr).*") &&
                        partAfterComma.length() > 2) {
                    city = partAfterComma.replaceAll("\\(City\\)", "").trim();
                    addressLine1 = potentialAddressAndCity.substring(0, lastCommaIndex).trim();
                }
            }

            addressLine1 = addressLine1.replaceAll(",", ", ").replaceAll("\\s+", " ").trim();

            return new AddressComponents(addressLine1, city, state, parsedZip);
        }

        String addressLine1 = cleanedAddress.endsWith(",") ?
                cleanedAddress.substring(0, cleanedAddress.length() - 1).trim() : cleanedAddress;
        return new AddressComponents(addressLine1, "", "", "");
    }

    private AddressComponents parseSpaceSeparated(String addressStr) {
        String[] parts = addressStr.split(" ");
        if (parts.length < 4) {
            return null;
        }

        String zipcode = "";
        String state = "";
        String city = "";
        String addressLine1 = "";

        int lastIndex = parts.length - 1;

        Pattern zipPattern = Pattern.compile("^\\d{5}(-\\d{4})?$");
        String potentialZip = parts[lastIndex];
        if (!zipPattern.matcher(potentialZip).matches()) {
            return null;
        }
        zipcode = potentialZip;
        lastIndex--;

        if (lastIndex < 0) {
            return null;
        }

        Pattern statePattern = Pattern.compile("^[A-Z]{2}$");
        String potentialState = parts[lastIndex];
        if (!statePattern.matcher(potentialState).matches() || !VALID_US_STATES.contains(potentialState.toUpperCase())) {
            return null;
        }
        state = potentialState;
        lastIndex--;

        if (lastIndex < 0) {
            return null;
        }

        // Find where city starts by going backwards from lastIndex
        // Look for street indicators (st, ave, rd, etc.) or numeric patterns
        // Check full words first, then abbreviations to avoid false matches (e.g., "Boston" contains "st")
        int cityStartIndex = -1;
        Pattern streetFullWordPattern = Pattern.compile("(?i).*(street|avenue|road|lane|court|place|drive|boulevard|parkway).*");
        Pattern streetAbbrPattern = Pattern.compile("(?i).*\\b(st|ave|rd|ln|ct|pl|dr)\\b.*|.*\\b(st|ave|rd|ln|ct|pl|dr)$");
        Pattern numericPattern = Pattern.compile("^\\d+.*");

        for (int i = lastIndex; i >= 0; i--) {
            String part = parts[i];
            // Check full words first (more specific) - e.g., "Street", "Avenue"
            if (streetFullWordPattern.matcher(part).matches()) {
                cityStartIndex = i + 1;
                break;
            }
            // Then check abbreviations at word boundaries or end of word - e.g., "Main St", "Oak Ave"
            // This avoids matching "st" inside words like "Boston"
            if (streetAbbrPattern.matcher(part).matches() || numericPattern.matcher(part).matches()) {
                cityStartIndex = i + 1;
                break;
            }
        }

        // If no street indicator found, assume city starts at the last part before state
        if (cityStartIndex == -1) {
            // If we have at least one part before state, that's the city
            if (lastIndex >= 0) {
                cityStartIndex = lastIndex + 1;
            } else {
                cityStartIndex = 0;
            }
        }

        // Build city from cityStartIndex to lastIndex
        if (cityStartIndex <= lastIndex && cityStartIndex >= 0) {
            StringBuilder cityBuilder = new StringBuilder();
            for (int i = cityStartIndex; i <= lastIndex; i++) {
                if (i > cityStartIndex) {
                    cityBuilder.append(" ");
                }
                cityBuilder.append(parts[i]);
            }
            city = cityBuilder.toString().trim().replaceAll("\\(City\\)", "").trim();
        }

        // Build address from start up to cityStartIndex
        StringBuilder addressBuilder = new StringBuilder();
        for (int i = 0; i < cityStartIndex; i++) {
            if (i > 0) {
                addressBuilder.append(" ");
            }
            addressBuilder.append(parts[i]);
        }
        addressLine1 = addressBuilder.toString().trim();
        addressLine1 = addressLine1.replaceAll(",", ", ").replaceAll("\\s+", " ").trim();

        // Only return if we have valid components (zipcode and state are required)
        if (!zipcode.isEmpty() && !state.isEmpty()) {
            return new AddressComponents(addressLine1, city, state, zipcode);
        }

        return null;
    }

    private class AddressComponents {
        private String addressLine1;
        private String city;
        private String state;
        private String zipcode;

        public AddressComponents(String addressLine1, String city, String state, String zipcode) {
            this.addressLine1 = addressLine1 != null ? addressLine1.replaceAll("[\"\\n\\r]", "") : "";
            this.city = city != null ? city : "";
            this.state = state != null ? state : "";
            this.zipcode = zipcode != null ? zipcode : "";
        }

        public String getAddressLine1() { return addressLine1; }
        public String getCity() { return city; }
        public String getState() { return state; }
        public String getZipcode() { return zipcode; }
    }

    public static class MappingResult {
        private Map mappedData;

        public MappingResult(Map mappedData) {
            this.mappedData = mappedData != null ? mappedData : new java.util.HashMap();
        }

        public Map getMappedData() {
            return mappedData;
        }
    }
}