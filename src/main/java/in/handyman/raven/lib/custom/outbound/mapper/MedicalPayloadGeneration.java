package in.handyman.raven.lib.custom.outbound.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lib.custom.outbound.dao.MetadataContext;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import in.handyman.raven.lib.custom.outbound.model.*;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class MedicalPayloadGeneration {

    private final Logger log;
    public MedicalPayloadGeneration(Logger log) {
        this.log = log;
    }
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String DEFAULT_VALUE = "";
    private static final double DEFAULT_DOUBLE_VALUE = 0.0;

    // SOR Item Name Constants
    private static final String MEMBER_ID_SOR_ITEM_NAME = "member_id";
    private static final String MEDICAID_ID_SOR_ITEM_NAME = "medicaid_id";
    private static final String MEMBER_LAST_NAME_SOR_ITEM_NAME = "member_last_name";
    private static final String MEMBER_FIRST_NAME_SOR_ITEM_NAME = "member_first_name";
    private static final String MEMBER_DATE_OF_BIRTH_SOR_ITEM_NAME = "member_date_of_birth";
    private static final String MEMBER_GROUP_ID_SOR_ITEM_NAME = "member_group_id";
    private static final String MEMBER_GENDER_SOR_ITEM_NAME = "member_gender";
    private static final String MEMBER_ADDRESS_LINE_1_SOR_ITEM_NAME = "member_address_line1";
    private static final String MEMBER_CITY_SOR_ITEM_NAME = "member_city";
    private static final String MEMBER_ZIPCODE_SOR_ITEM_NAME = "member_zipcode";
    private static final String MEMBER_STATE_SOR_ITEM_NAME = "member_state";
    private static final String LEVEL_OF_SERVICE_SOR_ITEM_NAME = "level_of_service";
    private static final String SERVICE_FROM_DATE_SOR_ITEM_NAME = "service_from_date";
    private static final String SERVICE_TO_DATE_SOR_ITEM_NAME = "service_to_date";
    private static final String NOTIFICATION_TYPE_SOR_ITEM_NAME = "notification_type";
    private static final String AUTH_ADMIT_DATE_SOR_ITEM_NAME = "auth_admit_date";
    private static final String AUTH_DISCHARGE_DATE_SOR_ITEM_NAME = "auth_discharge_date";
    private static final String AUTH_ID_SOR_ITEM_NAME = "auth_id";
    private static final String DIAGNOSIS_CODE_SOR_ITEM_NAME = "diagnosis_code";
    private static final String DIAGNOSIS_DESCRIPTION_SOR_ITEM_NAME = "diagnosis_description";
    private static final String CODE_POINTER_SOR_ITEM_NAME = "code_pointer";
    private static final String SERVICE_CODE_SOR_ITEM_NAME = "service_code";
    private static final String SERVICE_MODIFIER_SOR_ITEM_NAME = "service_code_modifier";
    private static final String SERVICE_DESCRIPTION_SOR_ITEM_NAME = "service_description";
    private static final String LINE_NUMBER_SOR_ITEM_NAME = "line_number";
    private static final String FAX_RECEIVED_DATE_SOR_ITEM_NAME = "fax_received_date";
    private static final String TOTAL_SERVICE_DAYS_SOR_ITEM_NAME = "total_service_days";
    private static final String SERVICE_QUANTITY_VISITS_SOR_ITEM_NAME = "service_quantity_visits";
    private static final String SERVICE_QUANTITY_UNIT_SOR_ITEM_NAME = "service_quantity_units";

    // Newborn information
    private static final String MULTIPLE_MEMBER_SOR_ITEM_NAME = "multiple_member_indicator";
    private static final String NEWBORN_REQUEST_SOR_ITEM_NAME = "newborn_request";
    private static final String NEWBORN_LAST_NAME_SOR_ITEM_NAME = "newborn_last_name";
    private static final String NEWBORN_FIRST_NAME_SOR_ITEM_NAME = "newborn_first_name";
    private static final String NEWBORN_DATE_OF_BIRTH_SOR_ITEM_NAME = "newborn_date_of_birth";
    private static final String NEWBORN_GENDER_SOR_ITEM_NAME = "newborn_gender";

    // Provider suffixes
    private static final String FIRST_NAME_SUFFIX = "_first_name";
    private static final String LAST_NAME_SUFFIX = "_last_name";
    private static final String ADDRESS_LINE1_SUFFIX = "_address_line1";
    private static final String ADDRESS_LINE2_SUFFIX = "_address_line2";
    private static final String CITY_SUFFIX = "_city";
    private static final String STATE_SUFFIX = "_state";
    private static final String ZIPCODE_SUFFIX = "_zipcode";
    private static final String SPECIALTY_SUFFIX = "_specialty";

    public MedicalOutboundResponse buildMedicalOutboundResponse(
            List<PredictionDTO> predictions,
            Map<String, String> configMap,
            String metadataContextString
            ) throws JsonProcessingException {

        MetadataContext metadataContext = mapper.readValue(metadataContextString, MetadataContext.class);

        log.info("Building complete medical outbound response");

        // Build metadata
        OutboundJsonMetaData metadata = buildMetadata(metadataContext, configMap);

        // Build medical payload
        MedicalPayload payload = buildMedicalPayload(predictions, configMap);


        MedicalOutboundResponse response = MedicalOutboundResponse.builder()
                .requestTxnId(metadataContext.getRequestTxnId())
                .status(metadataContext.getUploadStatus())
                .errorMessage(metadataContext.getErrorMessage())
                .errorMessageDetail(metadataContext.getErrorMessageDetail())
                .errorCd(metadataContext.getErrorCode())
                .documentId(metadataContext.getDocumentId())
                .inboundTransactionId(metadataContext.getInboundTransactionId())
                .metadata(metadata)
                .aumipayload(payload)
                .build();
        return response;
        }

    private OutboundJsonMetaData buildMetadata(MetadataContext context, Map<String, String> configMap) {
        log.info("Building metadata section");

        Integer overallConfidence = 0; // Can be calculated from predictions if needed

        return OutboundJsonMetaData.builder()
                .documentExtension(context.getDocumentExtension())
                .transactionId(context.getTransactionId())
                .inboundDocumentName(context.getInboundDocumentName())
                .documentType(context.getDocumentType())
                .processStartTime(context.getProcessStartTime())
                .processEndTime(context.getProcessEndTime())
                .processingTimeMs(0L) // Can be calculated if needed
                .processedAt(context.getProcessedAt())
                .pageCount(context.getCandidatePapers().size())
                .candidatePaper(context.getCandidatePapers())
                .overallConfidence(overallConfidence)
                .build();
    }


    private MedicalPayload buildMedicalPayload(List<PredictionDTO> predictions, Map<String, String> configMap) {
        log.info("Building medical payload from {} predictions", predictions.size());

        //filter predictions having line item Type is multi_value



        List<PredictionDTO> singleEntityPredictions =  predictions.stream().filter(predictionDTO -> !predictionDTO.getIsMultiEntityEnabled()).collect(Collectors.toList());

        List<PredictionDTO> multiEntityPredictions =  predictions.stream().filter(PredictionDTO::getIsMultiEntityEnabled).collect(Collectors.toList());
        MedicalPayload.MedicalPayloadBuilder builder = MedicalPayload.builder();

        getSingleEntityMedicalPayload(singleEntityPredictions, configMap, builder);
        getMultiEntityMedicalPayload(multiEntityPredictions, configMap, builder);


        return builder.build();
    }


    private void getSingleEntityMedicalPayload(List<PredictionDTO> predictions, Map<String, String> configMap, MedicalPayload.MedicalPayloadBuilder builder) {
        // Split predictions by line_item_type
        Map<String, List<PredictionDTO>> predictionsByType = predictions.stream()
                .collect(Collectors.groupingBy(
                        pred -> pred.getLineItemType() != null ? pred.getLineItemType()
                                : "single_value",
                        Collectors.toList()));

        List<PredictionDTO> singleValuePredictions = predictionsByType.getOrDefault("single_value",
                Collections.emptyList());
        List<PredictionDTO> multiValuePredictions = predictionsByType.getOrDefault("multi_value",
                Collections.emptyList());

        // Build field map from single_value predictions
        Map<String, ExtractedField> singleValueFieldMap = buildSingleValueFieldMap(singleValuePredictions,
                configMap);

        // Get configuration values
        boolean cleanStatus = Boolean
                .parseBoolean(configMap.getOrDefault("CUSTOM_MEDICAL_OUTBOUND_CLEANER", "false"));
        boolean memberEnabler = Boolean
                .parseBoolean(configMap.getOrDefault("NEWBORN_REQUEST_MEMBER_ENABLER", "false"));

        // Build payload


        // Build member section
        buildMemberSection(builder, singleValueFieldMap, memberEnabler, cleanStatus);

        // Build authorization section
        buildAuthorizationSection(builder, singleValueFieldMap, cleanStatus);

        // Build service modifiers from multi-value
        List<ServiceModifier> serviceModifiers = buildServiceModifiers(multiValuePredictions, configMap);
        if (!serviceModifiers.isEmpty()) {
            builder.service(serviceModifiers);
        }

        // Build diagnosis from multi-value
        List<Diagonsis> diagnosisList = buildDiagnosisList(multiValuePredictions, singleValueFieldMap,
                configMap,
                cleanStatus);
        if (!diagnosisList.isEmpty()) {
            builder.diagnosis(diagnosisList);
        }

        // Build providers
        List<Provider> providers = buildProvidersFromSingleValue(singleValueFieldMap, cleanStatus);
        if (!providers.isEmpty()) {
            builder.provider(providers);
        }

        // Build additional properties
        List<AdditionalProperties> additionalProperties = buildAdditionalProperties(singleValueFieldMap,
                configMap);
        if (!additionalProperties.isEmpty()) {
            builder.additionalProperties(additionalProperties);
        }

        // Build member additional properties (newborn)
        List<AdditionalProperties> memberAdditionalProperties = buildMemberAdditionalProperties(
                singleValueFieldMap);
        if (!memberAdditionalProperties.isEmpty()) {
            builder.memberAdditionalProperties(memberAdditionalProperties);
        }

    }


    private void getMultiEntityMedicalPayload(List<PredictionDTO> predictions, Map<String, String> configMap, MedicalPayload.MedicalPayloadBuilder builder) {
        // Split predictions by line_item_type
        Map<String, List<PredictionDTO>> predictionsByType = predictions.stream()
                .collect(Collectors.groupingBy(
                        pred -> pred.getLineItemType() != null ? pred.getLineItemType()
                                : "single_value",
                        Collectors.toList()));

        List<PredictionDTO> singleValuePredictions = predictionsByType.getOrDefault("single_value",
                Collections.emptyList());

        // Build field map from single_value predictions
        Map<String, ExtractedField> singleValueFieldMap = buildSingleValueFieldMap(singleValuePredictions,
                configMap);

        // Get configuration values
        boolean cleanStatus = Boolean
                .parseBoolean(configMap.getOrDefault("CUSTOM_MEDICAL_OUTBOUND_CLEANER", "false"));


        // Build providers
        List<Provider> providers = buildProvidersFromSingleValue(singleValueFieldMap, cleanStatus);
        if (!providers.isEmpty()) {
            builder.provider(providers);
        }

    }

    private Map<String, ExtractedField> buildSingleValueFieldMap(List<PredictionDTO> singleValuePredictions,
                                                                 Map<String, String> configMap) {
        if (singleValuePredictions == null || singleValuePredictions.isEmpty()) {
            log.info("No single_value predictions to process");
            return new HashMap<>();
        }

        log.info("Building field map from {} single_value predictions", singleValuePredictions.size());

        Map<String, ExtractedField> fieldMap = new HashMap<>();

        int confidenceMultiplier = Integer
                .parseInt(configMap.getOrDefault("CONFIDENCE_SCORE_MULTIPLY_VARIABLE", "100"));
        int scaledWidth = Integer.parseInt(configMap.getOrDefault("AUMI_BBOX_SCALAR_WIDTH", "1000"));
        int scaledHeight = Integer.parseInt(configMap.getOrDefault("AUMI_BBOX_SCALAR_HEIGHT", "1000"));
        int roundingPrecision = Integer.parseInt(configMap.getOrDefault("FLOAT_VALUE_ROUNDING_PRECISION", "2"));
        boolean reorderPaperNumber = Boolean
                .parseBoolean(configMap.getOrDefault("aumi.reorder.paper.number", "false"));

        for (PredictionDTO prediction : singleValuePredictions) {
            String itemName = prediction.getSorItemName();
            String value = prediction.getPredictedValue();

            if (itemName == null || value == null || value.trim().isEmpty()) {
                continue;
            }

            ExtractedField field = createExtractedFieldFromPrediction(
                    prediction, confidenceMultiplier, scaledWidth, scaledHeight,
                    roundingPrecision, reorderPaperNumber);

            fieldMap.put(itemName, field);
            log.debug("Added single_value field: {} = {}", itemName, value);
        }

        log.info("Built field map with {} single_value entries", fieldMap.size());
        return fieldMap;
    }

    private List<ServiceModifier> buildServiceModifiers(List<PredictionDTO> multiValuePredictions,
                                                        Map<String, String> configMap) {
        log.info("Building service modifiers from multi-value fields");

        Map<String, Map<String, ExtractedField>> groupedByInstance = groupAndExtractFields(
                multiValuePredictions,
                configMap);
        List<ServiceModifier> serviceModifiers = new ArrayList<>();

        for (Map<String, ExtractedField> fieldMap : groupedByInstance.values()) {
            ExtractedField serviceCode = fieldMap.getOrDefault(SERVICE_CODE_SOR_ITEM_NAME,
                    getDefaultExtractedField());
            // Filter only valid service entries if needed, or if code is mandatory
            if ((serviceCode.getValue() == null || serviceCode.getValue().isEmpty())
                    && !fieldMap.containsKey(SERVICE_CODE_SOR_ITEM_NAME)) {
                // Check if any service related fields exist, if so maybe we should keep it?
                // For now, if no service code, we might skip or use empty.
                // Let's assume strictness: if no service code, skip.
                if (fieldMap.isEmpty())
                    continue;
            }

            // More robust check: if it's not a service container instance (could be
            // diagnosis), we might want to skip?
            // Actually, we are iterating over ALL multi-value predictions.
            // We need to differentiate Service instances from Diagnosis instances.
            // The groupedByInstance mixes them if we don't filter.
            // The best way is to check if the instance contains service keys.
            if (!fieldMap.containsKey(SERVICE_CODE_SOR_ITEM_NAME)
                    && !fieldMap.containsKey(SERVICE_MODIFIER_SOR_ITEM_NAME)
                    && !fieldMap.containsKey(SERVICE_QUANTITY_UNIT_SOR_ITEM_NAME)
                    && !fieldMap.containsKey(SERVICE_QUANTITY_VISITS_SOR_ITEM_NAME)) {
                continue;
            }

            ExtractedField modifier = fieldMap.getOrDefault(SERVICE_MODIFIER_SOR_ITEM_NAME,
                    getDefaultExtractedField());
            List<ServiceModifierWrapper> modifierList = Collections.singletonList(
                    ServiceModifierWrapper.builder().cd(modifier).build());

            List<ServiceQuantity> quantities = buildServiceQuantities(fieldMap);

            ServiceModifier serviceModifier = ServiceModifier.builder()
                    .cd(serviceCode)
                    .modifier(modifierList)
                    .serviceQuantity(quantities)
                    .build();

            serviceModifiers.add(serviceModifier);
        }

        log.info("Built {} service modifiers", serviceModifiers.size());
        return serviceModifiers;
    }

    private List<ServiceQuantity> buildServiceQuantities(Map<String, ExtractedField> fieldMap) {
        List<ServiceQuantity> quantities = new ArrayList<>();

        ExtractedField unitField = fieldMap.getOrDefault(SERVICE_QUANTITY_UNIT_SOR_ITEM_NAME,
                getDefaultExtractedField());
        quantities.add(ServiceQuantity.builder()
                .quantityType(SimpleValueField.builder().value("Units").build())
                .quantityUnits(unitField)
                .build());

        ExtractedField visitField = fieldMap.getOrDefault(SERVICE_QUANTITY_VISITS_SOR_ITEM_NAME,
                getDefaultExtractedField());
        quantities.add(ServiceQuantity.builder()
                .quantityType(SimpleValueField.builder().value("Visits").build())
                .quantityUnits(visitField)
                .build());

        return quantities;
    }

    private Map<String, Map<String, ExtractedField>> groupAndExtractFields(List<PredictionDTO> predictions,
                                                                           Map<String, String> configMap) {
        int confidenceMultiplier = Integer
                .parseInt(configMap.getOrDefault("CONFIDENCE_SCORE_MULTIPLY_VARIABLE", "100"));
        int scaledWidth = Integer.parseInt(configMap.getOrDefault("AUMI_BBOX_SCALAR_WIDTH", "1000"));
        int scaledHeight = Integer.parseInt(configMap.getOrDefault("AUMI_BBOX_SCALAR_HEIGHT", "1000"));
        int roundingPrecision = Integer.parseInt(configMap.getOrDefault("FLOAT_VALUE_ROUNDING_PRECISION", "2"));
        boolean reorderPaperNumber = Boolean
                .parseBoolean(configMap.getOrDefault("aumi.reorder.paper.number", "false"));

        // Group by sorContainerInstance
        Map<String, List<PredictionDTO>> byInstance = predictions.stream()
                .collect(Collectors
                        .groupingBy(p -> p.getSorContainerInstance() != null
                                ? p.getSorContainerInstance()
                                : "1"));

        Map<String, Map<String, ExtractedField>> result = new TreeMap<>(); // Sorted by instance ID

        for (Map.Entry<String, List<PredictionDTO>> entry : byInstance.entrySet()) {
            Map<String, ExtractedField> fieldMap = new HashMap<>();
            for (PredictionDTO p : entry.getValue()) {
                ExtractedField field = createExtractedFieldFromPrediction(
                        p, confidenceMultiplier, scaledWidth, scaledHeight,
                        roundingPrecision, reorderPaperNumber);
                fieldMap.put(p.getSorItemName(), field);
            }
            result.put(entry.getKey(), fieldMap);
        }
        return result;
    }

    private ExtractedField createExtractedFieldFromPrediction(PredictionDTO prediction,
                                                              int confidenceMultiplier,
                                                              int scaledWidth, int scaledHeight,
                                                              int roundingPrecision,
                                                              boolean reorderPaperNumber) {
        int confidence = (int) (Math.round((prediction.getPrecision() * confidenceMultiplier) / 10.0) * 10);

        int paperNumber = prediction.getPaperNo();
        if (reorderPaperNumber && paperNumber >= 1) {
            paperNumber = paperNumber - 1;
        }

        double[] boundingBox = new double[] {
                prediction.getLeftPos(),
                prediction.getUpperPos(),
                prediction.getRightPos(),
                prediction.getLowerPos()
        };

        double[] rescaledBox = rescaleBoundingBox(
                boundingBox,
                prediction.getImageWidth(),
                prediction.getImageHeight(),
                scaledWidth,
                scaledHeight,
                roundingPrecision);

        Map<String, Double> bboxMap = new HashMap<>();
        bboxMap.put("x", rescaledBox[0]);
        bboxMap.put("y", rescaledBox[1]);
        bboxMap.put(WIDTH, rescaledBox[2]);
        bboxMap.put(HEIGHT, rescaledBox[3]);

        JsonNode boundingBoxNode = mapper.valueToTree(bboxMap);

        return ExtractedField.builder()
                .value(prediction.getPredictedValue())
                .confidence(confidence)
                .page(paperNumber)
                .boundingBox(boundingBoxNode)
                .build();
    }

    private double[] rescaleBoundingBox(double[] boundingBox, int originalWidth, int originalHeight,
                                        int scaledWidth, int scaledHeight, int precision) {
        double xScale = (double) originalWidth / scaledWidth;
        double yScale = (double) originalHeight / scaledHeight;

        return new double[] {
                round(boundingBox[0] * xScale, precision),
                round(boundingBox[1] * yScale, precision),
                round(boundingBox[2] * xScale, precision),
                round(boundingBox[3] * yScale, precision)
        };
    }

    private double round(double value, int places) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void buildMemberSection(MedicalPayload.MedicalPayloadBuilder builder,
                                    Map<String, ExtractedField> fieldMap,
                                    boolean memberEnabler,
                                    boolean cleanStatus) {
        log.info("Building member section");

        if (cleanStatus) {
            putIfPresent(builder::hcid, fieldMap, MEMBER_ID_SOR_ITEM_NAME);
            putIfPresent(builder::medicaidId, fieldMap, MEDICAID_ID_SOR_ITEM_NAME);
            putIfPresent(builder::groupId, fieldMap, MEMBER_GROUP_ID_SOR_ITEM_NAME);
            putIfPresent(builder::memberAddressLine1, fieldMap, MEMBER_ADDRESS_LINE_1_SOR_ITEM_NAME);
            putIfPresent(builder::memberCity, fieldMap, MEMBER_CITY_SOR_ITEM_NAME);
            putIfPresent(builder::memberZipCode, fieldMap, MEMBER_ZIPCODE_SOR_ITEM_NAME);
            putIfPresent(builder::memberState, fieldMap, MEMBER_STATE_SOR_ITEM_NAME);

            if (memberEnabler) {
                String newbornRequest = getFieldValue(fieldMap, NEWBORN_REQUEST_SOR_ITEM_NAME);
                if (!"Y".equals(newbornRequest)) {
                    buildMemberDetailsIfPresent(builder, fieldMap);
                }
            } else {
                buildMemberDetailsIfPresent(builder, fieldMap);
            }
        } else {
            builder.hcid(fieldMap.getOrDefault(MEMBER_ID_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .medicaidId(fieldMap.getOrDefault(MEDICAID_ID_SOR_ITEM_NAME,
                            getDefaultExtractedField()))
                    .groupId(fieldMap.getOrDefault(MEMBER_GROUP_ID_SOR_ITEM_NAME,
                            getDefaultExtractedField()))
                    .memberAddressLine1(
                            fieldMap.getOrDefault(MEMBER_ADDRESS_LINE_1_SOR_ITEM_NAME,
                                    getDefaultExtractedField()))
                    .memberCity(fieldMap.getOrDefault(MEMBER_CITY_SOR_ITEM_NAME,
                            getDefaultExtractedField()))
                    .memberZipCode(fieldMap.getOrDefault(MEMBER_ZIPCODE_SOR_ITEM_NAME,
                            getDefaultExtractedField()))
                    .memberState(fieldMap.getOrDefault(MEMBER_STATE_SOR_ITEM_NAME,
                            getDefaultExtractedField()));

            if (memberEnabler) {
                String newbornRequest = getFieldValue(fieldMap, NEWBORN_REQUEST_SOR_ITEM_NAME);
                if (!"Y".equals(newbornRequest)) {
                    buildMemberDetails(builder, fieldMap);
                }
            } else {
                buildMemberDetails(builder, fieldMap);
            }
        }
    }

    private void buildMemberDetailsIfPresent(MedicalPayload.MedicalPayloadBuilder builder,
                                             Map<String, ExtractedField> fieldMap) {
        putIfPresent(builder::memberLastName, fieldMap, MEMBER_LAST_NAME_SOR_ITEM_NAME);
        putIfPresent(builder::memberFirstName, fieldMap, MEMBER_FIRST_NAME_SOR_ITEM_NAME);
        putIfPresent(builder::memberDOB, fieldMap, MEMBER_DATE_OF_BIRTH_SOR_ITEM_NAME);
        putIfPresent(builder::memberGender, fieldMap, MEMBER_GENDER_SOR_ITEM_NAME);
    }

    private void buildMemberDetails(MedicalPayload.MedicalPayloadBuilder builder,
                                    Map<String, ExtractedField> fieldMap) {
        builder.memberLastName(
                        fieldMap.getOrDefault(MEMBER_LAST_NAME_SOR_ITEM_NAME, getDefaultExtractedField()))
                .memberFirstName(fieldMap.getOrDefault(MEMBER_FIRST_NAME_SOR_ITEM_NAME,
                        getDefaultExtractedField()))
                .memberDOB(fieldMap.getOrDefault(MEMBER_DATE_OF_BIRTH_SOR_ITEM_NAME,
                        getDefaultExtractedField()))
                .memberGender(fieldMap.getOrDefault(MEMBER_GENDER_SOR_ITEM_NAME,
                        getDefaultExtractedField()));
    }

    private void buildAuthorizationSection(MedicalPayload.MedicalPayloadBuilder builder,
                                           Map<String, ExtractedField> fieldMap,
                                           boolean cleanStatus) {
        log.info("Building authorization section");

        if (cleanStatus) {
            putIfPresent(builder::authId, fieldMap, AUTH_ID_SOR_ITEM_NAME);
            putIfPresent(builder::levelOfService, fieldMap, LEVEL_OF_SERVICE_SOR_ITEM_NAME);
            putIfPresent(builder::serviceFromDate, fieldMap, SERVICE_FROM_DATE_SOR_ITEM_NAME);
            putIfPresent(builder::serviceToDate, fieldMap, SERVICE_TO_DATE_SOR_ITEM_NAME);
            putIfPresent(builder::notificationType, fieldMap, NOTIFICATION_TYPE_SOR_ITEM_NAME);
            putIfPresent(builder::authAdmitDate, fieldMap, AUTH_ADMIT_DATE_SOR_ITEM_NAME);
            putIfPresent(builder::authDischargeDate, fieldMap, AUTH_DISCHARGE_DATE_SOR_ITEM_NAME);
            putIfPresent(builder::faxReceivedDate, fieldMap, FAX_RECEIVED_DATE_SOR_ITEM_NAME);
            putIfPresent(builder::totalServiceDays, fieldMap, TOTAL_SERVICE_DAYS_SOR_ITEM_NAME);
        } else {
            builder.authId(fieldMap.getOrDefault(AUTH_ID_SOR_ITEM_NAME, getDefaultExtractedField()))
                    .levelOfService(fieldMap.getOrDefault(LEVEL_OF_SERVICE_SOR_ITEM_NAME,
                            getDefaultExtractedField()))
                    .serviceFromDate(fieldMap.getOrDefault(SERVICE_FROM_DATE_SOR_ITEM_NAME,
                            getDefaultExtractedField()))
                    .serviceToDate(fieldMap.getOrDefault(SERVICE_TO_DATE_SOR_ITEM_NAME,
                            getDefaultExtractedField()))
                    .notificationType(
                            fieldMap.getOrDefault(NOTIFICATION_TYPE_SOR_ITEM_NAME,
                                    getDefaultExtractedField()))
                    .authAdmitDate(fieldMap.getOrDefault(AUTH_ADMIT_DATE_SOR_ITEM_NAME,
                            getDefaultExtractedField()))
                    .authDischargeDate(
                            fieldMap.getOrDefault(AUTH_DISCHARGE_DATE_SOR_ITEM_NAME,
                                    getDefaultExtractedField()))
                    .faxReceivedDate(fieldMap.getOrDefault(FAX_RECEIVED_DATE_SOR_ITEM_NAME,
                            getDefaultExtractedField()))
                    .totalServiceDays(
                            fieldMap.getOrDefault(TOTAL_SERVICE_DAYS_SOR_ITEM_NAME,
                                    getDefaultExtractedField()));
        }
    }

    private List<Diagonsis> buildDiagnosisList(List<PredictionDTO> multiValuePredictions,
                                               Map<String, ExtractedField> singleValueMap,
                                               Map<String, String> configMap,
                                               boolean cleanStatus) {
        log.info("Building diagnosis list from multi-value fields");

        Map<String, Map<String, ExtractedField>> groupedByInstance = groupAndExtractFields(
                multiValuePredictions,
                configMap);

        if (groupedByInstance.isEmpty() && !cleanStatus) {
            // Fallback for single value logic if needed, though usually multi-value is
            // expected now
            ExtractedField desc = singleValueMap.getOrDefault(DIAGNOSIS_DESCRIPTION_SOR_ITEM_NAME,
                    getDefaultExtractedField());
            ExtractedField ptr = singleValueMap.getOrDefault(CODE_POINTER_SOR_ITEM_NAME,
                    getDefaultExtractedField());
            // Make a default entry if desired, or return empty. Original logic returned a
            // default if not cleanStatus.
            return Collections.singletonList(
                    Diagonsis.builder()
                            .cd(getDefaultExtractedField())
                            .desc(desc)
                            .codePointer(ptr)
                            .build());
        }

        List<Diagonsis> diagnosisList = new ArrayList<>();

        for (Map<String, ExtractedField> fieldMap : groupedByInstance.values()) {
            ExtractedField code = fieldMap.getOrDefault(DIAGNOSIS_CODE_SOR_ITEM_NAME,
                    getDefaultExtractedField());

            if (cleanStatus && (code.getValue() == null || code.getValue().isEmpty())) {
                continue;
            }

            if (!fieldMap.containsKey(DIAGNOSIS_CODE_SOR_ITEM_NAME)
                    && !fieldMap.containsKey(DIAGNOSIS_DESCRIPTION_SOR_ITEM_NAME)
                    && !fieldMap.containsKey(CODE_POINTER_SOR_ITEM_NAME)) {
                continue;
            }

            ExtractedField desc = fieldMap.getOrDefault(DIAGNOSIS_DESCRIPTION_SOR_ITEM_NAME,
                    getDefaultExtractedField());
            ExtractedField pointer = fieldMap.getOrDefault(CODE_POINTER_SOR_ITEM_NAME,
                    getDefaultExtractedField());

            Diagonsis diagnosis = Diagonsis.builder()
                    .cd(code)
                    .desc(desc)
                    .codePointer(pointer)
                    .build();

            diagnosisList.add(diagnosis);
        }

        log.info("Built {} diagnosis entries", diagnosisList.size());
        return diagnosisList;
    }

    private List<Provider> buildProvidersFromSingleValue(Map<String, ExtractedField> fieldMap,
                                                         boolean cleanStatus) {
        log.info("Building providers from single-value fields");

        List<Provider> providers = new ArrayList<>();

        Provider servicingProvider = buildProviderIfPresent("Servicing Provider", "servicing_provider",
                fieldMap,
                cleanStatus);
        if (servicingProvider != null) {
            providers.add(servicingProvider);
        }

        Provider servicingFacility = buildProviderIfPresent("Servicing Facility", "servicing_facility",
                fieldMap,
                cleanStatus);
        if (servicingFacility != null) {
            providers.add(servicingFacility);
        }

        Provider referringProvider = buildProviderIfPresent("Requesting Provider", "referring_provider",
                fieldMap,
                cleanStatus);
        if (referringProvider != null) {
            providers.add(referringProvider);
        }

        Provider orderingProvider = buildProviderIfPresent("Ordering Provider", "ordering_provider", fieldMap,
                cleanStatus);
        if (orderingProvider != null) {
            providers.add(orderingProvider);
        }

        log.info("Built {} providers", providers.size());
        return providers;
    }

    private Provider buildProviderIfPresent(String category, String prefix,
                                            Map<String, ExtractedField> fieldMap,
                                            boolean cleanStatus) {
        Provider.ProviderBuilder builder = Provider.builder();

        boolean hasData = false;

        if (cleanStatus) {
            hasData |= putIfPresentProvider(builder::providerNPI, fieldMap, prefix + "_npi");
            hasData |= putIfPresentProvider(builder::providerTIN, fieldMap, prefix + "_tin");
            hasData |= putIfPresentProvider(builder::providerFirstName, fieldMap,
                    prefix + FIRST_NAME_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerLastName, fieldMap, prefix + LAST_NAME_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerAddressLine1, fieldMap,
                    prefix + ADDRESS_LINE1_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerAddressLine2, fieldMap,
                    prefix + ADDRESS_LINE2_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerCity, fieldMap, prefix + CITY_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerState, fieldMap, prefix + STATE_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerZipCode, fieldMap, prefix + ZIPCODE_SUFFIX);
            hasData |= putIfPresentProvider(builder::providerSpeciality, fieldMap,
                    prefix + SPECIALTY_SUFFIX);

            if (!hasData) {
                log.debug("Skipping {} - no data present", category);
                return null;
            }
        } else {
            builder.providerNPI(fieldMap.getOrDefault(prefix + "_npi", getDefaultExtractedField()))
                    .providerTIN(fieldMap.getOrDefault(prefix + "_tin", getDefaultExtractedField()))
                    .providerFirstName(fieldMap.getOrDefault(prefix + FIRST_NAME_SUFFIX,
                            getDefaultExtractedField()))
                    .providerLastName(fieldMap.getOrDefault(prefix + LAST_NAME_SUFFIX,
                            getDefaultExtractedField()))
                    .providerAddressLine1(
                            fieldMap.getOrDefault(prefix + ADDRESS_LINE1_SUFFIX,
                                    getDefaultExtractedField()))
                    .providerAddressLine2(
                            fieldMap.getOrDefault(prefix + ADDRESS_LINE2_SUFFIX,
                                    getDefaultExtractedField()))
                    .providerCity(fieldMap.getOrDefault(prefix + CITY_SUFFIX,
                            getDefaultExtractedField()))
                    .providerState(fieldMap.getOrDefault(prefix + STATE_SUFFIX,
                            getDefaultExtractedField()))
                    .providerZipCode(fieldMap.getOrDefault(prefix + ZIPCODE_SUFFIX,
                            getDefaultExtractedField()))
                    .providerSpeciality(fieldMap.getOrDefault(prefix + SPECIALTY_SUFFIX,
                            getDefaultExtractedField()));
        }

        builder.providerCategory(CategoryField.builder().value(category).build());
        return builder.build();
    }

    private boolean putIfPresentProvider(Consumer<ExtractedField> setter,
                                         Map<String, ExtractedField> fieldMap,
                                         String key) {
        ExtractedField value = fieldMap.get(key);
        if (value != null && value.getValue() != null && !value.getValue().isEmpty()) {
            setter.accept(value);
            return true;
        }
        return false;
    }

    private List<AdditionalProperties> buildAdditionalProperties(Map<String, ExtractedField> fieldMap,
                                                                 Map<String, String> configMap) {
        log.info("Building additional properties");

        List<AdditionalProperties> properties = new ArrayList<>();

        int clinicalPresentPages = Integer
                .parseInt(configMap.getOrDefault("AUMI_CLINICAL_PRESENT_PAGE_COUNT", "5"));
        int totalPages = 10;
        String clinicalValue = totalPages > clinicalPresentPages ? "Y" : "N";

        ExtractedField defaultField = getDefaultExtractedField();
        properties.add(AdditionalProperties.builder()
                .propName("CLINICAL_PRESENT")
                .propValue(clinicalValue)
                .page(defaultField.getPage())
                .confidence(Double.valueOf(defaultField.getConfidence()))
                .boundingBox(defaultField.getBoundingBox())
                .build());

        ExtractedField levelOfCare = fieldMap.get(LEVEL_OF_SERVICE_SOR_ITEM_NAME);
        if (levelOfCare != null && levelOfCare.getValue() != null && !levelOfCare.getValue().isEmpty()) {
            String[] values = levelOfCare.getValue().contains(",")
                    ? levelOfCare.getValue().split(",")
                    : new String[] { levelOfCare.getValue() };

            for (String value : values) {
                value = value.trim();
                if (!value.isEmpty()) {
                    properties.add(AdditionalProperties.builder()
                            .propName("AUTH_KEYWORD")
                            .propValue(value)
                            .page(levelOfCare.getPage())
                            .confidence(Double.valueOf(levelOfCare.getConfidence()))
                            .boundingBox(levelOfCare.getBoundingBox())
                            .build());
                }
            }
        }

        log.info("Built {} additional properties", properties.size());
        return properties;
    }

    private List<AdditionalProperties> buildMemberAdditionalProperties(Map<String, ExtractedField> fieldMap) {
        log.info("Building member additional properties");

        List<AdditionalProperties> properties = new ArrayList<>();

        ExtractedField multipleMember = fieldMap.get(MULTIPLE_MEMBER_SOR_ITEM_NAME);
        if (multipleMember != null && multipleMember.getValue() != null
                && !multipleMember.getValue().isEmpty()) {
            properties.add(AdditionalProperties.builder()
                    .propName("MULTIPLE_MEMBER")
                    .propValue(multipleMember.getValue())
                    .page(multipleMember.getPage())
                    .confidence(Double.valueOf(multipleMember.getConfidence()))
                    .boundingBox(multipleMember.getBoundingBox())
                    .build());
        }

        ExtractedField newbornRequest = fieldMap.get(NEWBORN_REQUEST_SOR_ITEM_NAME);
        String newbornValue = (newbornRequest != null && "Y".equalsIgnoreCase(newbornRequest.getValue())) ? "Y"
                : "N";

        ExtractedField baseField = newbornRequest != null ? newbornRequest : getDefaultExtractedField();
        properties.add(AdditionalProperties.builder()
                .propName("NEWBORN_REQUEST")
                .propValue(newbornValue)
                .page(baseField.getPage())
                .confidence(Double.valueOf(baseField.getConfidence()))
                .boundingBox(baseField.getBoundingBox())
                .build());

        if ("Y".equals(newbornValue)) {
            addNewbornPropertyIfPresent(properties, "NEWBORN_FIRSTNAME",
                    fieldMap.get(NEWBORN_FIRST_NAME_SOR_ITEM_NAME));
            addNewbornPropertyIfPresent(properties, "NEWBORN_LASTNAME",
                    fieldMap.get(NEWBORN_LAST_NAME_SOR_ITEM_NAME));
            addNewbornPropertyIfPresent(properties, "NEWBORN_GENDER",
                    fieldMap.get(NEWBORN_GENDER_SOR_ITEM_NAME));

            ExtractedField newbornDOB = getNewbornDOB(
                    fieldMap.get(NEWBORN_DATE_OF_BIRTH_SOR_ITEM_NAME),
                    fieldMap.get(FAX_RECEIVED_DATE_SOR_ITEM_NAME),
                    fieldMap.get(MEMBER_DATE_OF_BIRTH_SOR_ITEM_NAME));
            addNewbornPropertyIfPresent(properties, "NEWBORN_DOB", newbornDOB);

            for (AdditionalProperties prop : properties) {
                if ("MULTIPLE_MEMBER".equals(prop.getPropName())) {
                    prop.setPropValue("N");
                    log.info("Overriding MULTIPLE_MEMBER to N due to NEWBORN_REQUEST=Y");
                }
            }
        }

        log.info("Built {} member additional properties", properties.size());
        return properties;
    }

    private void addNewbornPropertyIfPresent(List<AdditionalProperties> properties,
                                             String propName,
                                             ExtractedField field) {
        if (field != null && field.getValue() != null && !field.getValue().isEmpty()) {
            properties.add(AdditionalProperties.builder()
                    .propName(propName)
                    .propValue(field.getValue())
                    .page(field.getPage())
                    .confidence(Double.valueOf(field.getConfidence()))
                    .boundingBox(field.getBoundingBox())
                    .build());
        }
    }

    private ExtractedField getNewbornDOB(ExtractedField newbornDOB,
                                         ExtractedField faxReceivedDate,
                                         ExtractedField memberDOB) {
        Date faxDate = parseDateSafe(faxReceivedDate);
        Date newbornDobDate = parseDateSafe(newbornDOB);
        Date memberDobDate = parseDateSafe(memberDOB);

        if (faxDate == null) {
            faxDate = new Date();
            log.info("fax_received_date missing, using current date");
        }

        if (newbornDobDate != null && isWithin30Days(newbornDobDate, faxDate)) {
            log.info("Using newborn DOB as valid");
            return newbornDOB;
        } else if (memberDobDate != null && isWithin30Days(memberDobDate, faxDate)) {
            log.info("Using member DOB as newborn DOB");
            return memberDOB;
        } else {
            log.info("Both DOBs invalid, returning empty");
            return getDefaultExtractedField();
        }
    }

    private Date parseDateSafe(ExtractedField field) {
        if (field == null || field.getValue() == null)
            return null;

        try {
            String ymd = toYMD(field.getValue());
            if (ymd == null)
                return null;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            return sdf.parse(ymd);
        } catch (Exception e) {
            return null;
        }
    }

    private String toYMD(String input) {
        if (input == null || input.trim().isEmpty())
            return null;

        String datePart = input.trim().split(" ")[0];
        String cleaned = datePart.replaceAll("[^0-9/\\-]", "");

        if (cleaned.matches("\\d{1,2}[-/]\\d{1,2}[-/]\\d{4}")) {
            String[] p = cleaned.split("[-/]");
            return p[2] + "-" +
                    (p[0].length() == 1 ? "0" + p[0] : p[0]) + "-" +
                    (p[1].length() == 1 ? "0" + p[1] : p[1]);
        }

        if (cleaned.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
            return cleaned;
        }

        return "";
    }

    private boolean isWithin30Days(Date dob, Date faxDate) {
        long diffDays = (faxDate.getTime() - dob.getTime()) / (1000L * 60 * 60 * 24);
        return diffDays >= 0 && diffDays <= 30;
    }

    private void putIfPresent(Consumer<ExtractedField> setter,
                              Map<String, ExtractedField> fieldMap,
                              String key) {
        ExtractedField value = fieldMap.get(key);
        if (value != null && value.getValue() != null && !value.getValue().isEmpty()) {
            setter.accept(value);
        }
    }

    private String getFieldValue(Map<String, ExtractedField> fieldMap, String key) {
        ExtractedField field = fieldMap.get(key);
        return (field != null && field.getValue() != null) ? field.getValue() : null;
    }

    private ExtractedField getDefaultExtractedField() {
        Map<String, Double> boundingBoxMap = new HashMap<>();
        boundingBoxMap.put("x", DEFAULT_DOUBLE_VALUE);
        boundingBoxMap.put("y", DEFAULT_DOUBLE_VALUE);
        boundingBoxMap.put(WIDTH, DEFAULT_DOUBLE_VALUE);
        boundingBoxMap.put(HEIGHT, DEFAULT_DOUBLE_VALUE);
        JsonNode boundingBoxJsonNode = mapper.valueToTree(boundingBoxMap);

        return ExtractedField.builder()
                .value(DEFAULT_VALUE)
                .page(0)
                .confidence(0)
                .boundingBox(boundingBoxJsonNode)
                .build();
    }

}
